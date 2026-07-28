package com.pawan.dpi.model;

public class EthernetFrame {

    private final String destinationMac;
    private final String sourceMac;
    private final int etherType;

    public EthernetFrame(String destinationMac, String sourceMac, int etherType) {
        this.destinationMac = destinationMac;
        this.sourceMac = sourceMac;
        this.etherType = etherType;
    }

    public String getDestinationMac() {
        return destinationMac;
    }

    public String getSourceMac() {
        return sourceMac;
    }

    public int getEtherType() {
        return etherType;
    }

    @Override
    public String toString() {

        return """
                Ethernet Header
                ------------------------
                Destination MAC : %s
                Source MAC      : %s
                EtherType       : 0x%04X
                """.formatted(
                destinationMac,
                sourceMac,
                etherType
        );
    }
}