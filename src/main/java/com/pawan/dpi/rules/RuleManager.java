package com.pawan.dpi.rules;

import com.pawan.dpi.model.ApplicationType;

import java.util.HashSet;
import java.util.Set;

public class RuleManager {

    private final Set<String> blockedIps = new HashSet<>();
    private final Set<ApplicationType> blockedApplications = new HashSet<>();
    private final Set<String> blockedDomains = new HashSet<>();

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