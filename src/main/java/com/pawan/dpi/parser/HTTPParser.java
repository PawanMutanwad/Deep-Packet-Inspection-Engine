package com.pawan.dpi.parser;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class HTTPParser {

    private static final String[] HTTP_METHODS = {
            "GET ", "POST ", "PUT ", "DELETE ", "HEAD ", "OPTIONS ", "PATCH ", "HTTP/1."
    };

    /**
     * Inspects a TCP payload and extracts the Host header if it is an HTTP request.
     *
     * @param payload raw TCP payload bytes
     * @return Optional containing the Host header domain if found, or Optional.empty()
     */
    public Optional<String> extractHost(byte[] payload) {

        if (payload == null || payload.length < 10) {
            return Optional.empty();
        }

        String content = new String(payload, 0, Math.min(payload.length, 2048), StandardCharsets.UTF_8);

        boolean isHttp = false;

        for (String method : HTTP_METHODS) {
            if (content.startsWith(method)) {
                isHttp = true;
                break;
            }
        }

        if (!isHttp) {
            return Optional.empty();
        }

        String[] lines = content.split("\r\n");

        for (String line : lines) {

            if (line.toLowerCase().startsWith("host:")) {

                String host = line.substring(5).trim();

                // Strip port number if present (e.g. example.com:80 -> example.com)
                int colonIndex = host.indexOf(':');

                if (colonIndex != -1) {
                    host = host.substring(0, colonIndex);
                }

                return Optional.of(host);
            }
        }

        return Optional.empty();
    }
}
