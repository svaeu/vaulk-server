package net.vaulk.server.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.vaulk.protocol.PacketProcessor;
import net.vaulk.server.channels.GroupLifecycleManager;
import net.vaulk.server.channels.group.Group;

import java.util.concurrent.TimeUnit;

public class NettyWebSocketServer {

    private EventLoopGroup connectionGroup;
    private EventLoopGroup workerGroup;

    private final WebSocketConfig config;

    private final GroupLifecycleManager<? extends Group> lifecycleManager;

    private final PacketProcessor packetProcessor;

    public NettyWebSocketServer(final WebSocketConfig config,
                                GroupLifecycleManager<? extends Group> lifecycleManager,
                                PacketProcessor packetProcessor) {
        this.config = config;
        this.lifecycleManager = lifecycleManager;
        this.packetProcessor = packetProcessor;
    }

    public void start() throws InterruptedException {
        final ServerBootstrap b;
        final ChannelFuture f;

        connectionGroup = new MultiThreadIoEventLoopGroup(
                config.bossThreads,
                EpollIoHandler.newFactory()
        );
        workerGroup = new MultiThreadIoEventLoopGroup(EpollIoHandler.newFactory());

        try {
            b = new ServerBootstrap()
                    .group(connectionGroup, workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new PipelineInitializer(lifecycleManager, packetProcessor))
                    .option(ChannelOption.SO_BACKLOG, config.backlog)
                    .option(ChannelOption.SO_REUSEADDR, config.reuseAddress)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, config.keepAlive)
                    .childOption(ChannelOption.TCP_NODELAY, config.tcpNoDelay)
                    .childOption(ChannelOption.SO_RCVBUF, config.recvBuffer)
                    .childOption(ChannelOption.SO_SNDBUF, config.sendBuffer);

            f = b.bind(config.host, config.port).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            shutdown();
            throw e;
        }
    }

    public void shutdown() {
        if(connectionGroup != null)
            connectionGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS)
                    .syncUninterruptibly();

        if(workerGroup != null)
            workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS)
                    .syncUninterruptibly();
    }
}
