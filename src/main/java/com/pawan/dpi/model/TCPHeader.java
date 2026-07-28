package com.pawan.dpi.model;

public class TCPHeader {

    private final int sourcePort;
    private final int destinationPort;
    private final long sequenceNumber;
    private final long acknowledgementNumber;
    private final int headerLength;

    public TCPHeader(int sourcePort,
                     int destinationPort,
                     long sequenceNumber,
                     long acknowledgementNumber,
                     int headerLength) {

        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.sequenceNumber = sequenceNumber;
        this.acknowledgementNumber = acknowledgementNumber;
        this.headerLength = headerLength;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public long getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    @Override
    public String toString() {

        return """
                TCP Header
                ------------------------
                Source Port        : %d
                Destination Port   : %d
                Sequence Number    : %d
                Acknowledgement No : %d
                Header Length      : %d bytes
                """.formatted(
                sourcePort,
                destinationPort,
                sequenceNumber,
                acknowledgementNumber,
                headerLength
        );
    }
}