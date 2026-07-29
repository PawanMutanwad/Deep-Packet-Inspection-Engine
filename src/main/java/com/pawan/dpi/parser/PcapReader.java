package com.pawan.dpi.parser;

import com.pawan.dpi.model.Packet;
import com.pawan.dpi.model.PacketHeader;
import com.pawan.dpi.model.PcapGlobalHeader;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

public class PcapReader {

    private DataInputStream input;

    // Open the PCAP file
    public void open(String filePath) throws IOException {
        input = new DataInputStream(new FileInputStream(filePath));
    }

    // Close the PCAP file
    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
    }

    // Read Global Header (24 Bytes)
    public PcapGlobalHeader readGlobalHeader() throws IOException {

        int magicNumber = Integer.reverseBytes(input.readInt());
        short versionMajor = Short.reverseBytes(input.readShort());
        short versionMinor = Short.reverseBytes(input.readShort());
        int thisZone = Integer.reverseBytes(input.readInt());
        int sigFigs = Integer.reverseBytes(input.readInt());
        int snapLen = Integer.reverseBytes(input.readInt());
        int network = Integer.reverseBytes(input.readInt());

        return new PcapGlobalHeader(
                magicNumber,
                versionMajor,
                versionMinor,
                thisZone,
                sigFigs,
                snapLen,
                network
        );
    }

    // Read Packet Header (16 Bytes)
    public PacketHeader readPacketHeader() throws IOException {

        try {

            int timestampSeconds = Integer.reverseBytes(input.readInt());
            int timestampMicroseconds = Integer.reverseBytes(input.readInt());
            int capturedLength = Integer.reverseBytes(input.readInt());
            int originalLength = Integer.reverseBytes(input.readInt());

            return new PacketHeader(
                    timestampSeconds,
                    timestampMicroseconds,
                    capturedLength,
                    originalLength
            );

        } catch (EOFException e) {

            // End of PCAP file reached
            return null;
        }
    }

    // Read Complete Packet
    public Packet readPacket() throws IOException {

        PacketHeader header = readPacketHeader();

        if (header == null) {
            return null;
        }

        byte[] data = new byte[header.getCapturedLength()];

        input.readFully(data);

        return new Packet(header, data);
    }
}