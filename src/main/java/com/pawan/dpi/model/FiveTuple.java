package com.pawan.dpi.model;

import java.util.Objects;

public class FiveTuple {

    private final String sourceIp;
    private final String destinationIp;
    private final int sourcePort;
    private final int destinationPort;
    private final int protocol;

    public FiveTuple(String sourceIp,
                     String destinationIp,
                     int sourcePort,
                     int destinationPort,
                     int protocol) {

        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.protocol = protocol;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public int getProtocol() {
        return protocol;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        FiveTuple other = (FiveTuple) obj;

        return sourcePort == other.sourcePort &&
                destinationPort == other.destinationPort &&
                protocol == other.protocol &&
                Objects.equals(sourceIp, other.sourceIp) &&
                Objects.equals(destinationIp, other.destinationIp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                sourceIp,
                destinationIp,
                sourcePort,
                destinationPort,
                protocol
        );
    }

    @Override
    public String toString() {

        return "FiveTuple{" +
                "sourceIp='" + sourceIp + '\'' +
                ", destinationIp='" + destinationIp + '\'' +
                ", sourcePort=" + sourcePort +
                ", destinationPort=" + destinationPort +
                ", protocol=" + protocol +
                '}';
    }
}