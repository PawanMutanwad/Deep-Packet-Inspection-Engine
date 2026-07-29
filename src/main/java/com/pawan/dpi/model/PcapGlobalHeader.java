package com.pawan.dpi.model;

public class PcapGlobalHeader {

    private final int magicNumber;
    private final short versionMajor;
    private final short versionMinor;
    private final int thisZone;
    private final int sigFigs;
    private final int snapLen;
    private final int network;

    public PcapGlobalHeader(
            int magicNumber,
            short versionMajor,
            short versionMinor,
            int thisZone,
            int sigFigs,
            int snapLen,
            int network) {

        this.magicNumber = magicNumber;
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.thisZone = thisZone;
        this.sigFigs = sigFigs;
        this.snapLen = snapLen;
        this.network = network;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public short getVersionMajor() {
        return versionMajor;
    }

    public short getVersionMinor() {
        return versionMinor;
    }

    public int getThisZone() {
        return thisZone;
    }

    public int getSigFigs() {
        return sigFigs;
    }

    public int getSnapLen() {
        return snapLen;
    }

    public int getNetwork() {
        return network;
    }

    @Override
    public String toString() {
        return """
                PCAP Global Header
                -----------------------
                Magic Number : %08X
                Version      : %d.%d
                Time Zone    : %d
                Sig Figs     : %d
                Snap Length  : %d
                Network Type : %d
                """.formatted(
                magicNumber,
                versionMajor,
                versionMinor,
                thisZone,
                sigFigs,
                snapLen,
                network);
    }
}