package com.pawan.dpi.parser;

import com.pawan.dpi.model.IPv6Packet;

public class IPv6Parser {

    public IPv6Packet parse(byte[] data) {

        int offset = 14;

        int version = (data[offset] >> 4) & 0x0F;

        int trafficClass =
                ((data[offset] & 0x0F) << 4)
                        | ((data[offset + 1] >> 4) & 0x0F);

        int flowLabel =
                ((data[offset + 1] & 0x0F) << 16)
                        | ((data[offset + 2] & 0xFF) << 8)
                        | (data[offset + 3] & 0xFF);

        int payloadLength =
                ((data[offset + 4] & 0xFF) << 8)
                        | (data[offset + 5] & 0xFF);

        int nextHeader = data[offset + 6] & 0xFF;

        int hopLimit = data[offset + 7] & 0xFF;

        String sourceIp = formatIPv6(data, offset + 8);

        String destinationIp = formatIPv6(data, offset + 24);

        return new IPv6Packet(
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

    private String formatIPv6(byte[] data, int offset) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 16; i += 2) {

            int value =
                    ((data[offset + i] & 0xFF) << 8)
                            | (data[offset + i + 1] & 0xFF);

            sb.append(String.format("%04X", value));

            if (i < 14) {
                sb.append(":");
            }
        }

        return sb.toString();
    }
}