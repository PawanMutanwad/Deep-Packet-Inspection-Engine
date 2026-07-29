package com.pawan.dpi.parser;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class DNSParser {

    /**
     * Extracts the requested domain name from a raw UDP DNS Query payload (RFC 1035).
     *
     * @param payload raw UDP payload bytes
     * @return Optional containing the queried domain name, or Optional.empty()
     */
    public Optional<String> extractDomain(byte[] payload) {

        if (payload == null || payload.length < 13) {
            return Optional.empty();
        }

        // Check QDCOUNT (Number of questions in byte 4-5)
        int qdCount = ((payload[4] & 0xFF) << 8) | (payload[5] & 0xFF);

        if (qdCount <= 0) {
            return Optional.empty();
        }

        // QNAME starts at offset 12
        int offset = 12;

        StringBuilder domain = new StringBuilder();

        while (offset < payload.length) {

            int labelLength = payload[offset] & 0xFF;

            // Zero length indicates end of QNAME
            if (labelLength == 0) {
                break;
            }

            // Pointer/compression or invalid label length
            if (labelLength >= 192 || offset + 1 + labelLength > payload.length) {
                break;
            }

            if (domain.length() > 0) {
                domain.append(".");
            }

            String label = new String(payload, offset + 1, labelLength, StandardCharsets.UTF_8);

            domain.append(label);

            offset += (labelLength + 1);
        }

        if (domain.length() == 0) {
            return Optional.empty();
        }

        return Optional.of(domain.toString());
    }
}
