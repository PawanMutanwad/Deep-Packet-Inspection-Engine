package com.pawan.dpi.model;

public class IPv4Packet {

    private final int version;
    private final int headerLength;
    private final int ttl;
    private final int protocol;
    private final String sourceIp;
    private final String destinationIp;

    public IPv4Packet(int version,
                      int headerLength,
                      int ttl,
                      int protocol,
                      String sourceIp,
                      String destinationIp) {

        this.version = version;
        this.headerLength = headerLength;
        this.ttl = ttl;
        this.protocol = protocol;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
    }

    public int getVersion() {
        return version;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public int getTtl() {
        return ttl;
    }

    public int getProtocol() {
        return protocol;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    @Override
    public String toString() {

        return """
                IPv4 Header
                ------------------------
                Version        : %d
                Header Length  : %d bytes
                TTL            : %d
                Protocol       : %d
                Source IP      : %s
                Destination IP : %s
                """.formatted(
                version,
                headerLength,
                ttl,
                protocol,
                sourceIp,
                destinationIp
        );
    }
}