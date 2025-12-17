package net.vaulk.server.channels;

import io.netty.channel.Channel;
import net.vaulk.server.channels.group.Group;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GroupLifecycleManager<T extends Group> {

    private final ConcurrentMap<String, T> groups;

    private static final int MAX_GROUPS = 10;
    private final ConcurrentMap<Channel, List<String>> memberships;

    public GroupLifecycleManager() {
        this.groups = new ConcurrentHashMap<>();
        this.memberships = new ConcurrentHashMap<>();
    }

    public UUID joinGroup(String id, Channel ch) {
        final T group;
        final List<String> ids;
        final UUID uuid;

        group = groups.get(id);
        if(group == null) return null;

        ids = memberships.computeIfAbsent(ch,
                m -> new ArrayList<>()
        );
        synchronized (ids) {
            if(ids.contains(id))
                return group.getUuid(ch);

            if(ids.size() >= MAX_GROUPS)
                evictChannel(ids.get(0), ch);

            uuid = UUID.randomUUID();
            group.add(ch, uuid);
            ids.add(id);

            return uuid;
        }
    }

    public void leaveGroup(String id, Channel ch) {
        final List<String> ids;

        ids = memberships.get(ch);
        if (ids != null)
            synchronized (ids) {
                ids.remove(id);
                if (ids.isEmpty()) memberships.remove(ch);
            }
        evictChannel(id, ch);
    }

    public void addGroupSession(String id, T group) {
        groups.putIfAbsent(id, group);
    }

    public void removeGroupSession(String id) {
        final T group;
        List<String> ids;

        group = groups.remove(id);
        if(group == null) return;

        for (Channel ch : new ArrayList<>(group.getChannels())) {
            ids = memberships.get(ch);
            if (ids != null)
                synchronized (ids) {
                    ids.remove(id);
                    if (ids.isEmpty()) memberships.remove(ch);
                }
            group.remove(ch);
        }
        group.terminate();
    }

    public void onChannelDisconnect(Channel ch) {
        final List<String> ids;

        ids = memberships.remove(ch);
        if (ids != null)
            for (String id : ids) evictChannel(id, ch);
    }

    public T getGroup(String id) {
        return groups.get(id);
    }

    private void evictChannel(String id, Channel ch) {
        final T group;

        group = groups.get(id);
        if(group == null) return;

        group.remove(ch);

        if(!group.isEmpty()) return;

        groups.remove(id);
        group.terminate();
    }
}
