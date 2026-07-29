package com.pawan.dpi.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DNSParserTest {

    private DNSParser dnsParser;

    @BeforeEach
    public void setUp() {
        dnsParser = new DNSParser();
    }

    @Test
    public void testExtractNullOrShortPayload() {
        assertTrue(dnsParser.extractDomain(null).isEmpty());
        assertTrue(dnsParser.extractDomain(new byte[10]).isEmpty());
    }

    @Test
    public void testExtractValidDnsQueryDomain() {
        // Construct a mock DNS Query for "google.com"
        byte[] payload = new byte[30];

        payload[0] = 0x12; // Transaction ID
        payload[1] = 0x34;
        payload[2] = 0x01; // Flags: Standard Query
        payload[3] = 0x00;
        payload[4] = 0x00; // QDCOUNT: 1 question
        payload[5] = 0x01;

        int idx = 12; // QNAME offset

        // "google" label (length 6)
        payload[idx++] = 0x06;
        byte[] google = "google".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(google, 0, payload, idx, google.length);
        idx += google.length;

        // "com" label (length 3)
        payload[idx++] = 0x03;
        byte[] com = "com".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(com, 0, payload, idx, com.length);
        idx += com.length;

        // Zero byte end of QNAME
        payload[idx] = 0x00;

        Optional<String> domain = dnsParser.extractDomain(payload);
        assertTrue(domain.isPresent());
        assertEquals("google.com", domain.get());
    }
}
