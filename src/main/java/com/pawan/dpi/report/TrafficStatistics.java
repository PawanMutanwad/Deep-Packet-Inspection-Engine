package com.pawan.dpi.report;

public class TrafficStatistics {

    private int totalPackets;
    private int ipv4Packets;
    private int ipv6Packets;
    private int tcpPackets;
    private int udpPackets;
    private int otherPackets;

    public void incrementTotalPackets() {
        totalPackets++;
    }

    public void incrementIPv4Packets() {
        ipv4Packets++;
    }

    public void incrementIPv6Packets() {
        ipv6Packets++;
    }

    public void incrementTCPPackets() {
        tcpPackets++;
    }

    public void incrementUDPPackets() {
        udpPackets++;
    }

    public void incrementOtherPackets() {
        otherPackets++;
    }

    @Override
    public String toString() {

        return """
                
                ==============================
                PCAP ANALYSIS SUMMARY
                ==============================
                Total Packets : %d
                IPv4 Packets  : %d
                IPv6 Packets  : %d
                TCP Packets   : %d
                UDP Packets   : %d
                Other Packets : %d
                """.formatted(
                totalPackets,
                ipv4Packets,
                ipv6Packets,
                tcpPackets,
                udpPackets,
                otherPackets
        );
    }
}