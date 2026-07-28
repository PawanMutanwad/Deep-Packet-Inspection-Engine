package com.pawan.dpi.parser;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SNIExtractor {

    private static final int TLS_HANDSHAKE = 0x16;
    private static final int CLIENT_HELLO = 0x01;
    private static final int SNI_EXTENSION = 0x0000;

    public Optional<String> extract(byte[] payload) {

        if (payload == null || payload.length < 50) {
            return Optional.empty();
        }

        int offset = 0;

        // TLS Record Header

        if ((payload[offset] & 0xFF) != TLS_HANDSHAKE) {
            return Optional.empty();
        }

        offset += 5;

        if (offset >= payload.length) {
            return Optional.empty();
        }

        // Client Hello

        if ((payload[offset] & 0xFF) != CLIENT_HELLO) {
            return Optional.empty();
        }

        offset += 4;

        // TLS Version
        offset += 2;

        // Random
        offset += 32;

        if (offset >= payload.length) {
            return Optional.empty();
        }

        // Session ID

        int sessionLength = payload[offset] & 0xFF;

        offset++;

        offset += sessionLength;

        if (offset + 2 > payload.length) {
            return Optional.empty();
        }

        // Cipher Suites

        int cipherLength = readUint16(payload, offset);

        offset += 2;

        offset += cipherLength;

        if (offset >= payload.length) {
            return Optional.empty();
        }

        // Compression Methods

        int compressionLength = payload[offset] & 0xFF;

        offset++;

        offset += compressionLength;

        if (offset + 2 > payload.length) {
            return Optional.empty();
        }

        // Extensions Length

        int extensionsLength = readUint16(payload, offset);

        offset += 2;

        int extensionsEnd = offset + extensionsLength;

        while (offset + 4 <= extensionsEnd && offset + 4 <= payload.length) {

            int extensionType = readUint16(payload, offset);

            int extensionSize = readUint16(payload, offset + 2);

            offset += 4;

            if (extensionType == SNI_EXTENSION) {

                if (offset + 5 > payload.length) {
                    return Optional.empty();
                }

                // Skip Server Name List Length
                offset += 2;

                // Name Type
                offset++;

                int hostnameLength = readUint16(payload, offset);

                offset += 2;

                if (offset + hostnameLength > payload.length) {
                    return Optional.empty();
                }

                String hostname =
                        new String(payload,
                                offset,
                                hostnameLength,
                                StandardCharsets.UTF_8);

                return Optional.of(hostname);
            }

            offset += extensionSize;
        }

        return Optional.empty();
    }

    private int readUint16(byte[] data, int offset) {

        return ((data[offset] & 0xFF) << 8)
                | (data[offset + 1] & 0xFF);
    }

}