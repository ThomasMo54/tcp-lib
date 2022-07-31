package com.motompro.tcplib.server;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class represents a room. A room an easy way to create a group of client and to communicate between those clients.
 * @param <SSC> An object extending {@link ServerSideClient}
 */
public class Room<SSC extends ServerSideClient> {

    protected final UUID uuid;
    protected final Set<SSC> clients = new HashSet<>();
    private final List<RoomListener<SSC>> roomListeners = new CopyOnWriteArrayList<>();

    public Room() {
        this.uuid = UUID.randomUUID();
    }

    /**
     * This method returns the {@link UUID} of this room
     * @return The {@link UUID} of this {@link Room}
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Register a {@link RoomListener} to this room.
     * @param roomListener
     */
    public void addRoomListener(RoomListener<SSC> roomListener) {
        this.roomListeners.add(roomListener);
    }

    /**
     * Unregister a {@link RoomListener} from this room.
     * @param roomListener
     */
    public void removeRoomListener(RoomListener<SSC> roomListener) {
        this.roomListeners.remove(roomListener);
    }

    /**
     * Get all registered {@link RoomListener}
     * @return A {@link List} of {@link RoomListener}
     */
    public List<RoomListener<SSC>> getRoomListeners() {
        return roomListeners;
    }

    /**
     * Add a client to this room
     * @param client The {@link SSC} which will be added
     */
    public void addClient(SSC client) {
        this.clients.add(client);
        client.setRoom(this);
    }

    /**
     * Add multiple clients to this room
     * @param collection The {@link Collection} of {@link SSC} which will be added
     */
    public void addClients(Collection<SSC> collection) {
        this.clients.addAll(collection);
        collection.forEach(client -> client.setRoom(this));
    }

    /**
     * Remove a client from this room
     * @param client The {@link SSC} which will be removed
     */
    public void removeClient(SSC client) {
        this.clients.remove(client);
        client.setRoom(null);
    }

    /**
     * Add multiple clients to this room
     * @param collection The {@link Collection} of {@link SSC} which will be added
     */
    public void removeClients(Collection<SSC> collection) {
        this.clients.removeAll(collection);
        collection.forEach(client -> client.setRoom(null));
    }

    /**
     * Get all clients of this room
     * @return A {@link Set} of {@link SSC}
     */
    public Set<SSC> getClients() {
        return clients;
    }

    /**
     * Get the amount of client of this room
     * @return An integer representing the amount
     */
    public int getClientNumber() {
        return clients.size();
    }

    /**
     * Check if the passed client is inside this room
     * @param client The {@link SSC} which will be checked
     * @return A boolean, <code>true</code> if inside <code>false</code> otherwise
     */
    public boolean isInside(SSC client) {
        return clients.contains(client);
    }

    /**
     * Send a message to every client of this room
     * @param message The {@link String} message
     */
    public void broadcast(String message) {
        broadcast(Collections.emptySet(), message);
    }

    /**
     * Send a message to every client of this room excepted the clients passed in blacklist
     * @param blacklist A {@link Set} of {@link SSC} representing the excepted clients
     * @param message The {@link String} message
     */
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
