package com.pawan.dpi.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HTTPParserTest {

    private HTTPParser httpParser;

    @BeforeEach
    public void setUp() {
        httpParser = new HTTPParser();
    }

    @Test
    public void testExtractNullOrShortPayload() {
        assertTrue(httpParser.extractHost(null).isEmpty());
        assertTrue(httpParser.extractHost(new byte[5]).isEmpty());
    }

    @Test
    public void testExtractNonHttpPayload() {
        byte[] payload = "INVALID PROTOCOL DATA".getBytes(StandardCharsets.UTF_8);
        assertTrue(httpParser.extractHost(payload).isEmpty());
    }

    @Test
    public void testExtractValidHttpHostHeader() {
        String httpRequest = "GET /index.html HTTP/1.1\r\n" +
                "Host: www.github.com\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "Accept: */*\r\n\r\n";

        byte[] payload = httpRequest.getBytes(StandardCharsets.UTF_8);
        Optional<String> host = httpParser.extractHost(payload);

        assertTrue(host.isPresent());
        assertEquals("www.github.com", host.get());
    }

    @Test
    public void testExtractHttpHostHeaderWithPort() {
        String httpRequest = "POST /api/v1/data HTTP/1.1\r\n" +
                "Host: api.example.com:8080\r\n" +
                "Content-Type: application/json\r\n\r\n";

        byte[] payload = httpRequest.getBytes(StandardCharsets.UTF_8);
        Optional<String> host = httpParser.extractHost(payload);

        assertTrue(host.isPresent());
        assertEquals("api.example.com", host.get());
    }
}
