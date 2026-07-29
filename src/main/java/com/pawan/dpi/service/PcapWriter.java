package com.pawan.dpi.service;

import com.pawan.dpi.model.Packet;
import com.pawan.dpi.model.PacketHeader;
import com.pawan.dpi.model.PcapGlobalHeader;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PcapWriter implements Closeable {

    private final DataOutputStream output;

    public PcapWriter(String outputFile,
                      PcapGlobalHeader globalHeader) throws IOException {

        output = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(outputFile)));

        writeGlobalHeader(globalHeader);
    }

    private void writeGlobalHeader(PcapGlobalHeader header)
            throws IOException {

        writeIntLE(header.getMagicNumber());
        writeShortLE(header.getVersionMajor());
        writeShortLE(header.getVersionMinor());
        writeIntLE(header.getThisZone());
        writeIntLE(header.getSigFigs());
        writeIntLE(header.getSnapLen());
        writeIntLE(header.getNetwork());
    }

    public synchronized void writePacket(Packet packet) throws IOException {

        PacketHeader h = packet.getPacketHeader();

        writeIntLE(h.getTimestampSeconds());
        writeIntLE(h.getTimestampMicroseconds());
        writeIntLE(h.getCapturedLength());
        writeIntLE(h.getOriginalLength());

        output.write(packet.getData());
    }

    private void writeShortLE(int value) throws IOException {

        output.writeByte(value & 0xFF);
        output.writeByte((value >>> 8) & 0xFF);
    }

    private void writeIntLE(int value) throws IOException {

        output.writeByte(value & 0xFF);
        output.writeByte((value >>> 8) & 0xFF);
        output.writeByte((value >>> 16) & 0xFF);
        output.writeByte((value >>> 24) & 0xFF);
    }

    @Override
    public void close() throws IOException {

        output.close();
    }
}