package com.pawan.dpi.model;

public class IPv6Packet {

    private final int version;
    private final int trafficClass;
    private final int flowLabel;
    private final int payloadLength;
    private final int nextHeader;
    private final int hopLimit;
    private final String sourceIp;
    private final String destinationIp;

    public IPv6Packet(int version,
                      int trafficClass,
                      int flowLabel,
                      int payloadLength,
                      int nextHeader,
                      int hopLimit,
                      String sourceIp,
                      String destinationIp) {

        this.version = version;
        this.trafficClass = trafficClass;
        this.flowLabel = flowLabel;
        this.payloadLength = payloadLength;
        this.nextHeader = nextHeader;
        this.hopLimit = hopLimit;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
    }

    public int getNextHeader() {
        return nextHeader;
    }

    @Override
    public String toString() {

        return """
                IPv6 Header
                ------------------------
                Version         : %d
                Traffic Class   : %d
                Flow Label      : %d
                Payload Length  : %d
                Next Header     : %d
                Hop Limit       : %d
                Source IP       : %s
                Destination IP  : %s
                """.formatted(
                version,
                trafficClass,
                flowLabel,
                payloadLength,
                nextHeader,
                hopLimit,
                sourceIp,
                destinationIp
        );
    }
}