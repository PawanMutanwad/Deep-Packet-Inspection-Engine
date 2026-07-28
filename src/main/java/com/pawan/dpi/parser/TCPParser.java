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
}