package com.pawan.dpi.model;

public class Packet {

    private final PacketHeader header;
    private final byte[] data;

    public Packet(PacketHeader header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public PacketHeader getHeader() {
        return header;
    }

    public PacketHeader getPacketHeader() {
        return header;
    }

    public byte[] getData() {
        return data;
    }
}