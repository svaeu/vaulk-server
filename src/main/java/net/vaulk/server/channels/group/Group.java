package net.vaulk.server.channels.group;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.UUID;

public interface Group {
    void whisper(UUID uuid, byte[] payload);
    void broadcast(byte[] payload);

    void add(Channel ch, UUID uuid);
    void remove(Channel ch);

    UUID getUuid(Channel ch);
    String getGroupId();

    void terminate();

    boolean isEmpty();

    ChannelGroup getChannels();
}
