package net.vaulk.server.socket.event;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.vaulk.server.channels.GroupLifecycleManager;
import net.vaulk.server.channels.group.Group;

@ChannelHandler.Sharable
public class ChannelDisconnect extends ChannelInboundHandlerAdapter {

    private final GroupLifecycleManager<? extends Group> lifecycleManager;

    public ChannelDisconnect(GroupLifecycleManager<? extends Group> lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        lifecycleManager.onChannelDisconnect(ctx.channel());
        super.channelInactive(ctx);
    }
}
