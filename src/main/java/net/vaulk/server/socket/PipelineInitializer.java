package net.vaulk.server.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import net.vaulk.protocol.PacketProcessor;
import net.vaulk.server.channels.GroupLifecycleManager;
import net.vaulk.server.channels.group.Group;
import net.vaulk.server.socket.event.ChannelIdleState;
import net.vaulk.server.socket.event.ChannelDisconnect;
import net.vaulk.server.socket.event.IncomingByteFrame;

import java.util.concurrent.TimeUnit;

public class PipelineInitializer extends ChannelInitializer<SocketChannel> {

    private final GroupLifecycleManager<? extends Group> lifecycleManager;

    private final PacketProcessor packetProcessor;

    public PipelineInitializer(GroupLifecycleManager<? extends Group> lifecycleManager,
                               PacketProcessor packetProcessor) {
        this.lifecycleManager = lifecycleManager;
        this.packetProcessor = packetProcessor;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        final ChannelPipeline pipeline;

        pipeline = ch.pipeline();
        pipeline.addLast("http-codec", new HttpServerCodec());
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));

        pipeline.addLast("websocket-protocol", new WebSocketServerProtocolHandler
                ("", "", false, 64 * 1024));
        pipeline.addLast("idleState", new IdleStateHandler
                (5, 0, 0, TimeUnit.MINUTES));

        pipeline.addLast(new ChannelIdleState());
        pipeline.addLast(new ChannelDisconnect(lifecycleManager));

        pipeline.addLast("binary-frame", new IncomingByteFrame(packetProcessor));
    }
}
