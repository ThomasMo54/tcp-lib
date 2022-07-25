package com.motompro.tcplib.server;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room<SSC extends ServerSideClient> {

    private final UUID uuid;
    private final Set<SSC> clients = new HashSet<>();
    private final List<RoomListener<SSC>> roomListeners = new CopyOnWriteArrayList<>();

    public Room() {
        this.uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void addRoomListener(RoomListener<SSC> roomListener) {
        this.roomListeners.add(roomListener);
    }

    public void removeRoomListener(RoomListener<SSC> roomListener) {
        this.roomListeners.remove(roomListener);
    }

    public List<RoomListener<SSC>> getRoomListeners() {
        return roomListeners;
    }

    public void addClient(SSC client) {
        this.clients.add(client);
        client.setRoom(this);
    }

    public void addClients(Collection<SSC> collection) {
        this.clients.addAll(collection);
        collection.forEach(client -> client.setRoom(this));
    }

    public void removeClient(SSC client) {
        this.clients.remove(client);
        client.setRoom(null);
    }

    public void removeClients(Collection<SSC> collection) {
        this.clients.removeAll(collection);
        collection.forEach(client -> client.setRoom(null));
    }

    public Set<SSC> getClients() {
        return clients;
    }

    public int getClientNumber() {
        return clients.size();
    }

    public boolean isInside(SSC client) {
        return clients.contains(client);
    }

    public void broadcast(String message) {
        broadcast(Collections.emptySet(), message);
    }

    public void broadcast(Set<SSC> blacklist, String message) {
        clients.stream().filter(client -> !blacklist.contains(client)).forEach(client -> {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
