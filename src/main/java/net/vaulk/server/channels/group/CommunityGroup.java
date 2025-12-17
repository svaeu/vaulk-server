package net.vaulk.server.channels.group;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommunityGroup implements Group {

    private final ChannelGroup channels;

    private final ConcurrentMap<Channel, UUID> uuids;
    private final ConcurrentMap<UUID, String> ipMappings;

    public CommunityGroup(String groupId) {
        this.channels = new DefaultChannelGroup(groupId, GlobalEventExecutor.INSTANCE);

        this.uuids = new ConcurrentHashMap<>();
        this.ipMappings = new ConcurrentHashMap<>();
    }

    @Override
    public void whisper(UUID uuid, byte[] payload) {

    }

    @Override
    public void broadcast(byte[] payload) {

    }

    @Override
    public void add(Channel ch, UUID uuid) {

    }

    @Override
    public void remove(Channel ch) {

    }

    @Override
    public UUID getUuid(Channel ch) {
        return uuids.get(ch);
    }

    @Override
    public String getGroupId() {
        return channels.name();
    }

    @Override
    public void terminate() {
        uuids.clear();
        ipMappings.clear();
    }

    @Override
    public boolean isEmpty() {
        return channels.isEmpty();
    }

    @Override
    public ChannelGroup getChannels() {
        return channels;
    }
}
