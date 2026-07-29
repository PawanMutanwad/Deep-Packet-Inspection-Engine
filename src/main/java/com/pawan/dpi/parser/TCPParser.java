package com.pawan.dpi.parser;

import com.pawan.dpi.model.TCPHeader;

public class TCPParser {

    public TCPHeader parse(byte[] data) {

        int ipOffset = 14;

        int ipHeaderLength = (data[ipOffset] & 0x0F) * 4;

        int tcpOffset = ipOffset + ipHeaderLength;

        int sourcePort =
                ((data[tcpOffset] & 0xFF) << 8)
                        | (data[tcpOffset + 1] & 0xFF);

        int destinationPort =
                ((data[tcpOffset + 2] & 0xFF) << 8)
                        | (data[tcpOffset + 3] & 0xFF);

        long sequenceNumber =
                ((long)(data[tcpOffset + 4] & 0xFF) << 24)
                        | ((long)(data[tcpOffset + 5] & 0xFF) << 16)
                        | ((long)(data[tcpOffset + 6] & 0xFF) << 8)
                        | (data[tcpOffset + 7] & 0xFF);

        long acknowledgementNumber =
                ((long)(data[tcpOffset + 8] & 0xFF) << 24)
                        | ((long)(data[tcpOffset + 9] & 0xFF) << 16)
                        | ((long)(data[tcpOffset + 10] & 0xFF) << 8)
                        | (data[tcpOffset + 11] & 0xFF);

        int headerLength =
                ((data[tcpOffset + 12] >> 4) & 0x0F) * 4;

        return new TCPHeader(
                sourcePort,
                destinationPort,
                sequenceNumber,
                acknowledgementNumber,
                headerLength
        );
    }

    /**
     * Extracts the TCP payload from a raw packet (assumes IPv4).
     */
    public byte[] extractPayload(byte[] data) {

        return extractPayload(data, 4);
    }

    /**
     * Extracts the TCP payload from a raw packet.
     *
     * @param data      full packet bytes (Ethernet + IP + TCP + payload)
     * @param ipVersion 4 for IPv4, 6 for IPv6
     * @return the TCP payload bytes, or empty array if none
     */
    public byte[] extractPayload(byte[] data, int ipVersion) {

        int ipOffset = 14;

        int ipHeaderLength;

        if (ipVersion == 6) {
            ipHeaderLength = 40;
        } else {
            ipHeaderLength = (data[ipOffset] & 0x0F) * 4;
        }

        int tcpOffset = ipOffset + ipHeaderLength;

        if (tcpOffset + 13 > data.length) {
            return new byte[0];
        }

        int tcpHeaderLength =
                ((data[tcpOffset + 12] >> 4) & 0x0F) * 4;

        int payloadOffset = tcpOffset + tcpHeaderLength;

        if (payloadOffset >= data.length) {
            return new byte[0];
        }

        byte[] payload = new byte[data.length - payloadOffset];

        System.arraycopy(data, payloadOffset,
                payload, 0, payload.length);

        return payload;
    }
}