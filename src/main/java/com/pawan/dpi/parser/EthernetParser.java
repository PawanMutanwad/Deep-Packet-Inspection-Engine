package com.pawan.dpi.parser;

import com.pawan.dpi.model.EthernetFrame;

public class EthernetParser {

    public EthernetFrame parse(byte[] data) {

        String destinationMac = formatMacAddress(data, 0);

        String sourceMac = formatMacAddress(data, 6);

        int etherType = ((data[12] & 0xFF) << 8)
                | (data[13] & 0xFF);

        return new EthernetFrame(
                destinationMac,
                sourceMac,
                etherType
        );
    }

    private String formatMacAddress(byte[] data, int startIndex) {

        StringBuilder mac = new StringBuilder();

        for (int i = startIndex; i < startIndex + 6; i++) {

            mac.append(String.format("%02X", data[i]));

            if (i < startIndex + 5) {
                mac.append(":");
            }
        }

        return mac.toString();
    }
}