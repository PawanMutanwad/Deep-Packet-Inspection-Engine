package com.pawan.dpi.service;

import com.pawan.dpi.model.ApplicationType;

public class ApplicationClassifier {

    public ApplicationType classify(String hostname) {

        if (hostname == null || hostname.isBlank()) {
            return ApplicationType.UNKNOWN;
        }

        hostname = hostname.toLowerCase();

        if (hostname.contains("youtube")) {
            return ApplicationType.YOUTUBE;
        }

        if (hostname.contains("facebook")) {
            return ApplicationType.FACEBOOK;
        }

        if (hostname.contains("instagram")) {
            return ApplicationType.INSTAGRAM;
        }

        if (hostname.contains("whatsapp")) {
            return ApplicationType.WHATSAPP;
        }

        if (hostname.contains("github")) {
            return ApplicationType.GITHUB;
        }

        if (hostname.contains("google")) {
            return ApplicationType.GOOGLE;
        }

        if (hostname.contains("netflix")) {
            return ApplicationType.NETFLIX;
        }

        if (hostname.contains("amazon")) {
            return ApplicationType.AMAZON;
        }

        if (hostname.contains("twitter") || hostname.contains("x.com")) {
            return ApplicationType.TWITTER;
        }

        if (hostname.contains("linkedin")) {
            return ApplicationType.LINKEDIN;
        }

        return ApplicationType.UNKNOWN;
    }
}