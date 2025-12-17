package net.vaulk.server.routing;

import io.nats.client.*;
import net.vaulk.server.channels.group.Group;
import net.vaulk.server.util.SessionLogger;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public final class PubSubService {

    private Connection natsConnection;
    private Options options;

    private final ConcurrentHashMap<String, Dispatcher> activeDispatchers;

    private NATSConfig config;
    private final String SUBJECT_PREFIX = "channel."; //replace it with a config field

    public PubSubService(NATSConfig config) {
        this.config = config;
        activeDispatchers = new ConcurrentHashMap<>();
    }

    public PubSubService initialize() {
        options = new Options.Builder()
                .servers(null) //add server from config
                .connectionName("")
                .reconnectWait(Duration.ofMillis(500))
                .maxReconnects(-1)
                .reconnectBufferSize(0)
                .pingInterval(Duration.ofSeconds(30))
                .requestCleanupInterval(Duration.ofSeconds(5))
                .connectionListener((conn, type) -> {
                    switch (type) {
                        case CONNECTED -> SessionLogger.log(
                                "Connected to NATS: "+conn.getConnectedUrl(),
                                SessionLogger.LogType.INFO,
                                null
                        );
                        case RECONNECTED -> resubscribe();
                    }
                })
                .errorListener(null)
                .build();

        return this;
    }

    public void start() throws IOException, InterruptedException {
        natsConnection = Nats.connect(options);
    }

    public void subscribe(Group group) {
        final String groupId;

        groupId = group.getGroupId();
        if(activeDispatchers.containsKey(groupId))
            return;

        final Dispatcher dispatcher;
        try {
            dispatcher = natsConnection.createDispatcher(msg -> {
                handleIncomingData(group, msg);
            });
            dispatcher.subscribe(SUBJECT_PREFIX + groupId);

            activeDispatchers.put(groupId, dispatcher);

        } catch (Exception e) {
            SessionLogger.log(
                    String.format("Failed to subscribe to group subject '%s'", groupId),
                    SessionLogger.LogType.CRITICAL, e
            );
        }
    }

    private void unsubscribe(String groupId) {
        final Dispatcher dispatcher;

        dispatcher = activeDispatchers.remove(groupId);
        if(dispatcher == null) return;

        try {
            dispatcher.unsubscribe(SUBJECT_PREFIX + groupId);
        } catch (Exception e) {
            SessionLogger.log(
                    String.format("Failed to unsubscribe to group subject '%s'", groupId),
                    SessionLogger.LogType.CRITICAL, e
            );
        }
    }

    private void handleIncomingData(Group group, Message msg) {
        try{
            if(group.isEmpty()) return;

            group.broadcast(msg.getData());

        } catch (Exception e) {
            SessionLogger.log(
                    String.format("Unable to handle incoming data to group '%s'",
                            group.getGroupId()),
                    SessionLogger.LogType.IMPORTANT, e
            );
        }
    }

    private void resubscribe() {

    }

    public boolean isConnected() {
        return natsConnection != null &&
                natsConnection.getStatus() == Connection.Status.CONNECTED;
    }

    public int getTotalSubscriptionCount() {
        return activeDispatchers.size();
    }

    public void shutdown() {
        for(String channelId : activeDispatchers.keySet())
            unsubscribe(channelId);

        try {
            if(natsConnection != null) natsConnection.close();
        } catch (InterruptedException e) {
            SessionLogger.log("Interrupted during service shutdown",
                    SessionLogger.LogType.IMPORTANT, e
            );
            Thread.currentThread().interrupt();
        }
    }
}