package com.pawan.dpi.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawan.dpi.model.ApplicationType;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RuleManager {

    private final Set<String> blockedIps = ConcurrentHashMap.newKeySet();
    private final Set<ApplicationType> blockedApplications = ConcurrentHashMap.newKeySet();
    private final Set<String> blockedDomains = ConcurrentHashMap.newKeySet();

    /**
     * Dynamically loads firewall rules from a JSON configuration file.
     *
     * @param jsonFilePath path to the rules.json file
     * @throws IOException if the file cannot be read
     */
    public void loadRulesFromJson(String jsonFilePath) throws IOException {

        File file = new File(jsonFilePath);

        if (!file.exists()) {
            System.err.println("[RuleManager] Config file not found: " + jsonFilePath);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(file);

        if (root.has("blockedIps")) {
            for (JsonNode ipNode : root.get("blockedIps")) {
                blockIp(ipNode.asText());
            }
        }

        if (root.has("blockedApplications")) {
            for (JsonNode appNode : root.get("blockedApplications")) {
                try {
                    ApplicationType type = ApplicationType.valueOf(appNode.asText().toUpperCase());
                    blockApplication(type);
                } catch (IllegalArgumentException e) {
                    System.err.println("[RuleManager] Unknown application type: " + appNode.asText());
                }
            }
        }

        if (root.has("blockedDomains")) {
            for (JsonNode domainNode : root.get("blockedDomains")) {
                blockDomain(domainNode.asText());
            }
        }

        System.out.println("[RuleManager] Dynamic rules loaded successfully from " + jsonFilePath);
    }

    public void blockIp(String ip) {
        blockedIps.add(ip);
    }

    public void blockApplication(ApplicationType applicationType) {
        blockedApplications.add(applicationType);
    }

    public void blockDomain(String domain) {
        blockedDomains.add(domain.toLowerCase());
    }

    public boolean isBlocked(String sourceIp,
                             ApplicationType applicationType,
                             String serverName) {

        if (sourceIp != null && blockedIps.contains(sourceIp)) {
            return true;
        }

        if (applicationType != null &&
                blockedApplications.contains(applicationType)) {
            return true;
        }

        if (serverName != null) {

            String host = serverName.toLowerCase();

            for (String blockedDomain : blockedDomains) {

                if (host.contains(blockedDomain)) {
                    return true;
                }
            }
        }

        return false;
    }

    public Set<String> getBlockedIps() {
        return blockedIps;
    }

    public Set<ApplicationType> getBlockedApplications() {
        return blockedApplications;
    }

    public Set<String> getBlockedDomains() {
        return blockedDomains;
    }
}