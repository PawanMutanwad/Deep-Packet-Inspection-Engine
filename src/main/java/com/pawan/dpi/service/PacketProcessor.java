package com.pawan.dpi.service;

import com.pawan.dpi.model.ApplicationType;
import com.pawan.dpi.model.EthernetFrame;
import com.pawan.dpi.model.FiveTuple;
import com.pawan.dpi.model.IPv4Packet;
import com.pawan.dpi.model.IPv6Packet;
import com.pawan.dpi.model.Packet;
import com.pawan.dpi.model.TCPHeader;
import com.pawan.dpi.model.UDPHeader;
import com.pawan.dpi.parser.DNSParser;
import com.pawan.dpi.parser.EthernetParser;
import com.pawan.dpi.parser.HTTPParser;
import com.pawan.dpi.parser.IPv4Parser;
import com.pawan.dpi.parser.IPv6Parser;
import com.pawan.dpi.parser.SNIExtractor;
import com.pawan.dpi.parser.TCPParser;
import com.pawan.dpi.parser.UDPParser;
import com.pawan.dpi.report.DPIReport;
import com.pawan.dpi.rules.RuleManager;
import com.pawan.dpi.tracker.ConnectionTracker;
import com.pawan.dpi.tracker.Flow;

import java.io.IOException;
import java.util.Optional;

public class PacketProcessor {

    private final EthernetParser ethernetParser;
    private final IPv4Parser ipv4Parser;
    private final IPv6Parser ipv6Parser;
    private final TCPParser tcpParser;
    private final UDPParser udpParser;
    private final HTTPParser httpParser;
    private final DNSParser dnsParser;
    private final ConnectionTracker connectionTracker;
    private final RuleManager ruleManager;
    private final ApplicationClassifier applicationClassifier;
    private final SNIExtractor sniExtractor;
    private final DPIReport report;

    public PacketProcessor() {

        ethernetParser = new EthernetParser();
        ipv4Parser = new IPv4Parser();
        ipv6Parser = new IPv6Parser();
        tcpParser = new TCPParser();
        udpParser = new UDPParser();
        httpParser = new HTTPParser();
        dnsParser = new DNSParser();
        connectionTracker = new ConnectionTracker();
        ruleManager = new RuleManager();
        applicationClassifier = new ApplicationClassifier();
        sniExtractor = new SNIExtractor();
        report = new DPIReport();

        // Try dynamic rule configuration from input/rules.json first
        try {
            ruleManager.loadRulesFromJson("input/rules.json");
        } catch (IOException e) {
            // Fallback default rule if JSON file is missing
            ruleManager.blockApplication(ApplicationType.YOUTUBE);
        }
    }

    /**
     * Processes a single packet through the full DPI pipeline.
     *
     * @param packet the raw packet read from the PCAP file
     * @return true if the packet should be forwarded, false if dropped
     */
    public boolean process(Packet packet) {

        report.incrementTotalPackets();

        byte[] data = packet.getData();

        // --- Step 1: Parse Ethernet Header ---

        EthernetFrame ethernet = ethernetParser.parse(data);

        int etherType = ethernet.getEtherType();

        // --- Step 2: Parse IP Layer ---

        String sourceIp;
        String destinationIp;
        int protocol;
        int ipVersion;

        if (etherType == 0x0800) {

            // IPv4
            IPv4Packet ipv4 = ipv4Parser.parse(data);

            sourceIp = ipv4.getSourceIp();
            destinationIp = ipv4.getDestinationIp();
            protocol = ipv4.getProtocol();
            ipVersion = 4;

        } else if (etherType == 0x86DD) {

            // IPv6
            IPv6Packet ipv6 = ipv6Parser.parse(data);

            sourceIp = ipv6.getSourceIp();
            destinationIp = ipv6.getDestinationIp();
            protocol = ipv6.getNextHeader();
            ipVersion = 6;

        } else {

            // Non-IP packet (ARP, etc.) — allow through
            report.incrementAllowedPackets();
            return true;
        }

        // --- Step 3: Parse Transport Layer ---

        int sourcePort = 0;
        int destinationPort = 0;
        byte[] tcpPayload = null;
        byte[] udpPayload = null;

        if (protocol == 6) {

            // TCP
            TCPHeader tcp = tcpParser.parse(data);

            sourcePort = tcp.getSourcePort();
            destinationPort = tcp.getDestinationPort();
            tcpPayload = tcpParser.extractPayload(data, ipVersion);

        } else if (protocol == 17) {

            // UDP
            UDPHeader udp = udpParser.parse(data);

            sourcePort = udp.getSourcePort();
            destinationPort = udp.getDestinationPort();
            udpPayload = udpParser.extractPayload(data, ipVersion);

        } else {

            // Non-TCP/UDP protocol — allow through
            report.incrementAllowedPackets();
            return true;
        }

        // --- Step 4: Build FiveTuple & Track Flow ---

        FiveTuple fiveTuple = new FiveTuple(
                sourceIp,
                destinationIp,
                sourcePort,
                destinationPort,
                protocol
        );

        Flow flow = connectionTracker.getOrCreateFlow(fiveTuple);

        flow.incrementPacketCount();

        if (flow.isBlocked()) {

            report.incrementBlockedPackets();
            return false;
        }

        // --- Step 5: Multi-Protocol L7 Hostname Extraction ---

        Optional<String> hostname = Optional.empty();

        if (tcpPayload != null && tcpPayload.length > 0) {

            // Try TLS SNI first (HTTPS / Port 443)
            hostname = sniExtractor.extract(tcpPayload);

            // If not TLS, try HTTP Host Header parsing (Port 80 / 8080)
            if (hostname.isEmpty()) {
                hostname = httpParser.extractHost(tcpPayload);
            }

        } else if (udpPayload != null && udpPayload.length > 0) {

            // Try DNS Query domain parsing (UDP Port 53)
            if (sourcePort == 53 || destinationPort == 53) {
                hostname = dnsParser.extractDomain(udpPayload);
            }
        }

        hostname.ifPresent(flow::setServerName);

        // --- Step 6: Application Classification ---

        ApplicationType application =
                applicationClassifier.classify(flow.getServerName());

        flow.setApplication(application.name());

        report.incrementApplication(application);

        // --- Step 7: Rule Enforcement ---

        boolean blocked = ruleManager.isBlocked(
                sourceIp,
                application,
                flow.getServerName()
        );

        if (blocked) {

            flow.setBlocked(true);
            report.incrementBlockedPackets();

            return false;
        }

        report.incrementAllowedPackets();

        return true;
    }

    public void printReport() {

        report.printReport(connectionTracker.getFlowCount());
    }

    public DPIReport getReport() {
        return report;
    }

    public ConnectionTracker getConnectionTracker() {
        return connectionTracker;
    }

    public RuleManager getRuleManager() {
        return ruleManager;
    }
}