package com.motompro.tcplib.server;

import java.io.IOException;
import java.util.*;

public class Room {

    private final UUID uuid;
    private final Set<Client> clients = new HashSet<>();

    protected Room(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void addClient(Client client) {
        this.clients.add(client);
        client.setRoom(this);
    }

    public void addClients(Collection<Client> collection) {
        this.clients.addAll(collection);
        collection.forEach(client -> client.setRoom(this));
    }

    public void removeClient(Client client) {
        this.clients.remove(client);
        client.setRoom(null);
    }

    public void removeClients(Collection<Client> collection) {
        this.clients.removeAll(collection);
        collection.forEach(client -> client.setRoom(null));
    }

    public Set<Client> getClients() {
        return clients;
    }

    public boolean isInside(Client client) {
        return clients.contains(client);
    }

    public void broadcast(String... message) {
        broadcast(Collections.emptySet(), message);
    }

    public void broadcast(Set<Client> blacklist, String... message) {
        clients.stream().filter(client -> !blacklist.contains(client)).forEach(client -> {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
