package com.pawan.dpi.model;

public class UDPHeader {

    private final int sourcePort;
    private final int destinationPort;
    private final int length;
    private final int checksum;

    public UDPHeader(int sourcePort,
                     int destinationPort,
                     int length,
                     int checksum) {

        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.length = length;
        this.checksum = checksum;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public int getLength() {
        return length;
    }

    public int getChecksum() {
        return checksum;
    }

    @Override
    public String toString() {

        return """
                UDP Header
                ------------------------
                Source Port      : %d
                Destination Port : %d
                Length           : %d
                Checksum         : 0x%04X
                """.formatted(
                sourcePort,
                destinationPort,
                length,
                checksum
        );
    }
}