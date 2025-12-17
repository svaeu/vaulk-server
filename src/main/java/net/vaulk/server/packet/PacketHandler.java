package net.vaulk.server.packet;

import io.netty.channel.Channel;
import net.vaulk.protocol.PacketExecutor;
import net.vaulk.server.VaulkEdgeServer;

public class PacketHandler extends PacketExecutor {

    private final VaulkEdgeServer edgeServer;

    public PacketHandler(VaulkEdgeServer edgeServer) {
        this.edgeServer = edgeServer;
    }

    @Override
    protected void handlePlaceholder(byte[] payload, Channel ch) {

    }
}
