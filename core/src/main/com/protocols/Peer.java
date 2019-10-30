package com.protocols;

import java.io.Serializable;

public class Peer implements Serializable {
    private String name;
    private String sourceHost;
    private int sourcePort;

    public void setSourcePort(int sourcePort) { this.sourcePort = sourcePort; }

    public void setSourceHost(String sourceHost) { this.sourceHost = sourceHost; }

    public void setName(String name) { this.name = name; }

    public int getSourcePort() { return sourcePort; }

    public String getSourceHost() { return sourceHost; }

    public String getName() { return name; }
}
