package com.pawan.dpi.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SNIExtractorTest {

    private SNIExtractor sniExtractor;

    @BeforeEach
    public void setUp() {
        sniExtractor = new SNIExtractor();
    }

    @Test
    public void testExtractNullOrShortPayload() {
        assertTrue(sniExtractor.extract(null).isEmpty());
        assertTrue(sniExtractor.extract(new byte[10]).isEmpty());
    }

    @Test
    public void testExtractNonTLSHandshakePayload() {
        byte[] payload = new byte[60];
        payload[0] = 0x17; // Application Data record, not Handshake (0x16)
        assertTrue(sniExtractor.extract(payload).isEmpty());
    }

    @Test
    public void testExtractValidSNI() {
        String domain = "example.com";
        byte[] domainBytes = domain.getBytes(StandardCharsets.UTF_8);

        // Build a mock TLS Client Hello payload with SNI extension
        byte[] payload = new byte[100];
        int idx = 0;

        payload[idx++] = 0x16; // TLS Handshake record
        payload[idx++] = 0x03; // Version 3.1
        payload[idx++] = 0x01;
        payload[idx++] = 0x00; // Length
        payload[idx++] = 0x50;

        payload[idx++] = 0x01; // Client Hello
        payload[idx++] = 0x00; // Length
        payload[idx++] = 0x00;
        payload[idx++] = 0x4C;

        idx += 2; // Version
        idx += 32; // Random
        payload[idx++] = 0x00; // Session ID length (0)

        payload[idx++] = 0x00; // Cipher suites length
        payload[idx++] = 0x02;
        payload[idx++] = 0x00;
        payload[idx++] = 0x2F;

        payload[idx++] = 0x01; // Compression methods length
        payload[idx++] = 0x00;

        // Extensions length
        payload[idx++] = 0x00;
        payload[idx++] = (byte) (domainBytes.length + 9);

        // Extension: SNI (0x0000)
        payload[idx++] = 0x00;
        payload[idx++] = 0x00;
        payload[idx++] = 0x00;
        payload[idx++] = (byte) (domainBytes.length + 5);

        // Server Name List Length
        payload[idx++] = 0x00;
        payload[idx++] = (byte) (domainBytes.length + 3);

        // Server Name Type: host_name (0)
        payload[idx++] = 0x00;

        // Hostname length
        payload[idx++] = 0x00;
        payload[idx++] = (byte) domainBytes.length;

        // Hostname string
        System.arraycopy(domainBytes, 0, payload, idx, domainBytes.length);

        Optional<String> result = sniExtractor.extract(payload);
        assertTrue(result.isPresent());
        assertEquals("example.com", result.get());
    }
}
