package com.pawan.dpi.service;

import com.pawan.dpi.model.ApplicationType;
import com.pawan.dpi.model.FiveTuple;
import com.pawan.dpi.parser.SNIExtractor;
import com.pawan.dpi.report.DPIReport;
import com.pawan.dpi.rules.RuleManager;
import com.pawan.dpi.tracker.ConnectionTracker;
import com.pawan.dpi.tracker.Flow;

import java.util.Optional;

public class PacketProcessor {

    private final ConnectionTracker connectionTracker;
    private final RuleManager ruleManager;
    private final ApplicationClassifier applicationClassifier;
    private final SNIExtractor sniExtractor;
    private final DPIReport report;

    public PacketProcessor() {

        connectionTracker = new ConnectionTracker();
        ruleManager = new RuleManager();
        applicationClassifier = new ApplicationClassifier();
        sniExtractor = new SNIExtractor();
        report = new DPIReport();

        // Example rule
        ruleManager.blockApplication(ApplicationType.YOUTUBE);
    }

    public boolean process(FiveTuple fiveTuple,
                           byte[] tcpPayload) {

        report.incrementTotalPackets();

        Flow flow = connectionTracker.getOrCreateFlow(fiveTuple);

        flow.incrementPacketCount();

        if (flow.isBlocked()) {

            report.incrementBlockedPackets();
            return false;
        }

        Optional<String> hostname = sniExtractor.extract(tcpPayload);

        hostname.ifPresent(flow::setServerName);

        ApplicationType application =
                applicationClassifier.classify(flow.getServerName());

        flow.setApplication(application.name());

        report.incrementApplication(application);

        boolean blocked = ruleManager.isBlocked(
                fiveTuple.getSourceIp(),
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