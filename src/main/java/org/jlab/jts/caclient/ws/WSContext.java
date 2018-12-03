package org.jlab.jts.caclient.ws;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 *
 * @author slominskir
 */
public class WSContext implements MessageHandler.Whole<String>, AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(WSContext.class.getName());
    private final Session session;
    private final ConcurrentHashMap<String, WSChannel> monitoredChannelsMap = new ConcurrentHashMap<>();

    public WSContext(Session session) {
        this.session = session;

        session.addMessageHandler(this);
    }

    public synchronized void send(String json) {
        System.out.println("Writing Message: " + json);
        try {
            Future<Void> future = session.getAsyncRemote().sendText(json);
            future.get();
        } catch (ExecutionException | IllegalStateException | InterruptedException ee) {
            LOGGER.log(Level.SEVERE, "Unable to transmit message", ee);
        }
    }

    public WSChannel create(String name) {
        return new WSChannel(this, name);
    }

    CompletableFuture<?> connectAsync(WSChannel channel) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        JsonArrayBuilder array = Json.createArrayBuilder();
        array.add(channel.getName());
        json.add("type", "monitor");
        json.add("pvs", array);

        WSChannel existing = monitoredChannelsMap.putIfAbsent(channel.getName(), channel);
        if (existing != null) {
            throw new RuntimeException("Already monitoring channel: " + channel.getName());
        }

        send(json.build().toString());

        return channel.connectFuture;
    }

    CompletableFuture<?> connectMultipleAsync(List<WSChannel> channels) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        JsonArrayBuilder array = Json.createArrayBuilder();
        List<CompletableFuture<?>> futureList = new ArrayList<>();

        int count = 0;

        for (WSChannel channel : channels) {

            WSChannel existing = monitoredChannelsMap.putIfAbsent(channel.getName(), channel);
            if (existing != null) {
                throw new RuntimeException("Already monitoring channel: " + channel.getName());
            }

            array.add(channel.getName());
            futureList.add(channel.connectFuture);

            // You'll get close reason 1009 'message too big' if you're not careful so we batch send 400 pv names at a time.
            count++;

            if (count > 400) {
                json.add("type", "monitor");
                json.add("pvs", array);

                String msg = json.build().toString();

                send(msg);

                count = 0;
                json = Json.createObjectBuilder();
                array = Json.createArrayBuilder();
            }

        }

        if (count > 0) {
            json.add("type", "monitor");
            json.add("pvs", array);

            String msg = json.build().toString();

            send(msg);
        }

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture<?>[0]));
    }

    void closeMultiple(List<WSChannel> channels) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        JsonArrayBuilder array = Json.createArrayBuilder();

        int count = 0;

        for (WSChannel channel : channels) {

            array.add(channel.getName());

            WSChannel existing = monitoredChannelsMap.remove(channel.getName());
            if (existing == null) {
                LOGGER.log(Level.INFO, "Channel is already closed: {0}", channel.getName());
                continue;
            }

            // You'll get close reason 1009 'message too big' if you're not careful so we batch send 400 pv names at a time.
            count++;

            if (count > 400) {
                json.add("type", "clear");
                json.add("pvs", array);

                send(json.build().toString());

                count = 0;
                json = Json.createObjectBuilder();
                array = Json.createArrayBuilder();
            }
        }

        if (count > 0) {
            json.add("type", "clear");
            json.add("pvs", array);

            send(json.build().toString());
        }
    }

    void close(WSChannel channel) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        JsonArrayBuilder array = Json.createArrayBuilder();
        array.add(channel.getName());
        json.add("type", "clear");
        json.add("pvs", array);

        WSChannel existing = monitoredChannelsMap.remove(channel.getName());
        if (existing == null) {
            LOGGER.log(Level.INFO, "Channel is already closed: {0}", channel.getName());
            return;
        }

        send(json.build().toString());
    }

    @Override
    public void onMessage(String msg) {
        //System.out.println("Message Received: " + msg);
        JsonReader reader = Json.createReader(new StringReader(msg));
        JsonObject obj = reader.readObject();
        String type = obj.getString("type");
        String name;
        WSChannel channel;
        switch (type) {
            case "info":
                name = obj.getString("pv");
                channel = monitoredChannelsMap.get(name);
                if (channel == null) {
                    LOGGER.log(Level.INFO, "Info dropped: channel is already closed: {0}", name);
                    return;
                }
                boolean connected = obj.getBoolean("connected");
                if (connected) {
                    channel.connectFuture.complete(null);
                }
                break;
            case "update":
                name = obj.getString("pv");
                channel = monitoredChannelsMap.get(name);
                if (channel == null) {
                    //LOGGER.log(Level.INFO, "Update dropped: channel is already closed: {0}", name);
                    return;
                }
                Object value = toValue(obj.get("value"));
                channel.updateValueMonitors(value);
                break;
            default:
                LOGGER.log(Level.WARNING, "Unrecognized message type: {0}", type);
        }
    }

    private Object toValue(JsonValue value) {
        Object obj;
        switch (value.getValueType()) {
            case NUMBER:
                obj = ((JsonNumber) value).bigDecimalValue();
                break;
            case STRING:
                obj = ((JsonString) value).getString();
                break;
            case TRUE:
                obj = Boolean.TRUE;
                break;
            case FALSE:
                obj = Boolean.FALSE;
                break;
            case NULL:
                obj = null;
                break;
            default:
                throw new RuntimeException("Unexpected type: " + value.getValueType());
        }

        return obj;
    }

    @Override
    public void close() throws Exception {
        // Check if channels are closed?
    }
}
