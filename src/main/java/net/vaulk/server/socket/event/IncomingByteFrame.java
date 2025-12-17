package net.vaulk.server.socket.event;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.vaulk.protocol.PacketProcessor;
import net.vaulk.server.util.SessionLogger;

import static net.vaulk.protocol.binary.ByteBufProtocol.*;

@ChannelHandler.Sharable
public class IncomingByteFrame extends SimpleChannelInboundHandler<ByteBuf> {

    private final PacketProcessor packetProcessor;

    public IncomingByteFrame(PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {

        if (msg.readableBytes() < HEADER_SIZE || msg.getUnsignedShort
                (msg.readerIndex() + MAGIC_OFFSET) != MAGIC) return;
        try {
            packetProcessor.processSmart(msg, ctx.channel());
        } catch (Exception e) {
            ctx.fireExceptionCaught(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SessionLogger.log(cause.getMessage(), SessionLogger.LogType.CRITICAL, cause);

        super.exceptionCaught(ctx, cause);
    }
}
