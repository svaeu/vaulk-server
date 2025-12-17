package net.vaulk.server;

import net.vaulk.protocol.PacketProcessor;
import net.vaulk.server.channels.GroupLifecycleManager;
import net.vaulk.server.channels.group.Group;
import net.vaulk.server.config.ConfigManager;
import net.vaulk.server.config.dto.ServerConfig;
import net.vaulk.server.packet.PacketHandler;
import net.vaulk.server.socket.NettyWebSocketServer;

import java.io.IOException;

public class VaulkEdgeServer {

    private NettyWebSocketServer webSocketServer;

    private static final String CONFIG_PATH = "server.yml";

    private final PacketProcessor processor = new PacketProcessor(
            new PacketHandler(this)
    );
    private static final VaulkEdgeServer INSTANCE = new VaulkEdgeServer();

    private VaulkEdgeServer() {}

    public VaulkEdgeServer init() throws IOException {
        final ConfigManager configManager;
        final GroupLifecycleManager<? extends Group> lifecycleManager;
        final ServerConfig serverConfig;

        configManager = new ConfigManager(CONFIG_PATH);
        configManager.loadConfigurations();

        serverConfig = configManager.getConfig().getServer();

        lifecycleManager = new GroupLifecycleManager<>();

        webSocketServer = new NettyWebSocketServer(
                serverConfig.getWebsocket(),
                lifecycleManager,
                processor
        );
        return this;
    }

    public static VaulkEdgeServer getInstance() {
        return INSTANCE;
    }

    public void start() throws InterruptedException {
        webSocketServer.start();
    }
}