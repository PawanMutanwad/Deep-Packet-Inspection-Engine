package com.pawan.dpi.report;

import com.pawan.dpi.model.ApplicationType;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class DPIReport {

    private final LongAdder totalPackets = new LongAdder();
    private final LongAdder allowedPackets = new LongAdder();
    private final LongAdder blockedPackets = new LongAdder();

    private final Map<ApplicationType, LongAdder> applicationStatistics =
            new ConcurrentHashMap<>();

    public void incrementTotalPackets() {
        totalPackets.increment();
    }

    public void incrementAllowedPackets() {
        allowedPackets.increment();
    }

    public void incrementBlockedPackets() {
        blockedPackets.increment();
    }

    public void incrementApplication(ApplicationType applicationType) {

        applicationStatistics.computeIfAbsent(applicationType, k -> new LongAdder()).increment();
    }

    public long getTotalPackets() {
        return totalPackets.sum();
    }

    public long getAllowedPackets() {
        return allowedPackets.sum();
    }

    public long getBlockedPackets() {
        return blockedPackets.sum();
    }

    public void printReport(int totalFlows) {

        System.out.println();
        System.out.println("========== DPI REPORT ==========");
        System.out.println();

        System.out.println("Total Packets   : " + totalPackets.sum());
        System.out.println("Allowed Packets : " + allowedPackets.sum());
        System.out.println("Blocked Packets : " + blockedPackets.sum());
        System.out.println("Total Flows     : " + totalFlows);

        System.out.println();
        System.out.println("Applications");
        System.out.println("------------------------------");

        for (ApplicationType type : ApplicationType.values()) {

            LongAdder adder = applicationStatistics.get(type);

            if (adder != null && adder.sum() > 0) {

                System.out.printf("%-12s : %d%n",
                        type,
                        adder.sum());
            }
        }

        System.out.println();
        System.out.println("===============================");
    }
}