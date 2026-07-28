package com.pawan.dpi.rules;

public class RuleEngine {

    public void inspectTCP(int sourcePort, int destinationPort) {

        if (sourcePort == 80 || destinationPort == 80) {
            System.out.println("[INFO] HTTP Traffic Detected");
        }

        if (sourcePort == 443 || destinationPort == 443) {
            System.out.println("[INFO] HTTPS Traffic Detected");
        }

        if (sourcePort == 22 || destinationPort == 22) {
            System.out.println("[INFO] SSH Traffic Detected");
        }

        if (sourcePort == 21 || destinationPort == 21) {
            System.out.println("[INFO] FTP Traffic Detected");
        }

        if (sourcePort == 25 || destinationPort == 25) {
            System.out.println("[INFO] SMTP Traffic Detected");
        }
    }

    public void inspectUDP(int sourcePort, int destinationPort) {

        if (sourcePort == 53 || destinationPort == 53) {
            System.out.println("[INFO] DNS Traffic Detected");
        }

        if (sourcePort == 67 || destinationPort == 67 ||
                sourcePort == 68 || destinationPort == 68) {

            System.out.println("[INFO] DHCP Traffic Detected");
        }

        if (sourcePort == 123 || destinationPort == 123) {
            System.out.println("[INFO] NTP Traffic Detected");
        }
    }
}