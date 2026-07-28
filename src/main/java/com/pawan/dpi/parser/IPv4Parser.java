package com.pawan.dpi.parser;

import com.pawan.dpi.model.IPv4Packet;

public class IPv4Parser {

    public IPv4Packet parse(byte[] data) {

        // IPv4 header starts after the 14-byte Ethernet header
        int offset = 14;

        // First byte contains Version and Header Length
        int version = (data[offset] >> 4) & 0x0F;

        int headerLength = (data[offset] & 0x0F) * 4;

        // TTL is at byte 8 of the IPv4 header
        int ttl = data[offset + 8] & 0xFF;

        // Protocol is at byte 9 of the IPv4 header
        int protocol = data[offset + 9] & 0xFF;

        // Source IP starts at byte 12
        String sourceIp = formatIpAddress(data, offset + 12);

        // Destination IP starts at byte 16
        String destinationIp = formatIpAddress(data, offset + 16);

        return new IPv4Packet(
                version,
                headerLength,
                ttl,
                protocol,
                sourceIp,
                destinationIp
        );
    }

    private String formatIpAddress(byte[] data, int startIndex) {

        return (data[startIndex] & 0xFF) + "." +
                (data[startIndex + 1] & 0xFF) + "." +
                (data[startIndex + 2] & 0xFF) + "." +
                (data[startIndex + 3] & 0xFF);
    }
}