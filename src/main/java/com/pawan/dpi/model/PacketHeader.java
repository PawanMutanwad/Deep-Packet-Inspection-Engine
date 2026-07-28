package com.pawan.dpi.model;

public class PacketHeader {

    private final int timestampSeconds;
    private final int timestampMicroseconds;
    private final int capturedLength;
    private final int originalLength;

    public PacketHeader(int timestampSeconds,
                        int timestampMicroseconds,
                        int capturedLength,
                        int originalLength) {

        this.timestampSeconds = timestampSeconds;
        this.timestampMicroseconds = timestampMicroseconds;
        this.capturedLength = capturedLength;
        this.originalLength = originalLength;
    }

    public int getTimestampSeconds() {
        return timestampSeconds;
    }

    public int getTimestampMicroseconds() {
        return timestampMicroseconds;
    }

    public int getCapturedLength() {
        return capturedLength;
    }

    public int getOriginalLength() {
        return originalLength;
    }

    @Override
    public String toString() {

        return """
                Packet Header
                ------------------------
                Timestamp : %d.%06d
                Captured  : %d bytes
                Original  : %d bytes
                """.formatted(
                timestampSeconds,
                timestampMicroseconds,
                capturedLength,
                originalLength
        );
    }
}