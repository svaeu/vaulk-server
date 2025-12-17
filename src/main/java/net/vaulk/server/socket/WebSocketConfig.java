package net.vaulk.server.socket;

public final class WebSocketConfig {
    public String host;
    public int port;
    public int bossThreads;
    public int backlog;
    public boolean reuseAddress;
    public boolean keepAlive;
    public boolean tcpNoDelay;
    public int recvBuffer;
    public int sendBuffer;
}
