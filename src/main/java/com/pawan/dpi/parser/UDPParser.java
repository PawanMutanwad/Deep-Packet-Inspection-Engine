package com.pawan.dpi.parser;

import com.pawan.dpi.model.UDPHeader;

public class UDPParser {

    public UDPHeader parse(byte[] data) {

        int ipOffset = 14;

        int version = (data[ipOffset] >> 4) & 0x0F;

        int udpOffset;

        if (version == 4) {

            int ipHeaderLength = (data[ipOffset] & 0x0F) * 4;
            udpOffset = ipOffset + ipHeaderLength;

        } else if (version == 6) {

            udpOffset = ipOffset + 40;

        } else {

            throw new IllegalArgumentException("Unsupported IP Version");
        }

        int sourcePort =
                ((data[udpOffset] & 0xFF) << 8)
                        | (data[udpOffset + 1] & 0xFF);

        int destinationPort =
                ((data[udpOffset + 2] & 0xFF) << 8)
                        | (data[udpOffset + 3] & 0xFF);

        int length =
                ((data[udpOffset + 4] & 0xFF) << 8)
                        | (data[udpOffset + 5] & 0xFF);

        int checksum =
                ((data[udpOffset + 6] & 0xFF) << 8)
                        | (data[udpOffset + 7] & 0xFF);

        return new UDPHeader(
                sourcePort,
                destinationPort,
                length,
                checksum
        );
    }
}