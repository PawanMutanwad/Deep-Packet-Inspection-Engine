package com.pawan.dpi.parser;

import com.pawan.dpi.model.IPv4Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IPv4ParserTest {

    private IPv4Parser ipv4Parser;

    @BeforeEach
    public void setUp() {
        ipv4Parser = new IPv4Parser();
    }

    @Test
    public void testParseValidIPv4Header() {
        // Mock Ethernet (14 bytes) + IPv4 Header (20 bytes)
        byte[] data = new byte[34];

        int ipOffset = 14;

        // Version = 4, Header Length = 5 (20 bytes) -> 0x45
        data[ipOffset] = 0x45;

        // TTL = 64
        data[ipOffset + 8] = 64;

        // Protocol = 6 (TCP)
        data[ipOffset + 9] = 6;

        // Source IP: 192.168.1.10 (192 = 0xC0, 168 = 0xA8, 1 = 0x01, 10 = 0x0A)
        data[ipOffset + 12] = (byte) 192;
        data[ipOffset + 13] = (byte) 168;
        data[ipOffset + 14] = (byte) 1;
        data[ipOffset + 15] = (byte) 10;

        // Destination IP: 10.0.0.1
        data[ipOffset + 16] = (byte) 10;
        data[ipOffset + 17] = (byte) 0;
        data[ipOffset + 18] = (byte) 0;
        data[ipOffset + 19] = (byte) 1;

        IPv4Packet ipv4 = ipv4Parser.parse(data);

        assertNotNull(ipv4);
        assertEquals(4, ipv4.getVersion());
        assertEquals(20, ipv4.getHeaderLength());
        assertEquals(64, ipv4.getTtl());
        assertEquals(6, ipv4.getProtocol());
        assertEquals("192.168.1.10", ipv4.getSourceIp());
        assertEquals("10.0.0.1", ipv4.getDestinationIp());
    }
}
