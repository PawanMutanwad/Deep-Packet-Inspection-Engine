package com.pawan.dpi.tracker;

public class Flow {

    private String serverName;

    private String application;

    private boolean blocked;

    private long packetCount;

    public Flow() {

        this.serverName = "Unknown";
        this.application = "Unknown";
        this.blocked = false;
        this.packetCount = 0;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public long getPacketCount() {
        return packetCount;
    }

    public void incrementPacketCount() {
        packetCount++;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "serverName='" + serverName + '\'' +
                ", application='" + application + '\'' +
                ", blocked=" + blocked +
                ", packetCount=" + packetCount +
                '}';
    }
}