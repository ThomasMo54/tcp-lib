package com.motompro.tcplib.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Server {

    public static final String INTERNAL_MESSAGE_PREFIX = "internal";
    public static final String EXTERNAL_MESSAGE_PREFIX = "external";
    public static final String DISCONNECT_MESSAGE = "disconnect";
    public static final String PING_MESSAGE = "ping";

    private final ServerSocket serverSocket;
    private final Map<UUID, ServerSideClient> clients = new HashMap<>();
    private final Set<ClientListener> clientListeners = new HashSet<>();
    private final Map<UUID, Room> rooms = new HashMap<>();
    private boolean allowConnection = true;
    private final Map<UUID, Ping> pings = new HashMap<>();

    public Server() throws IOException {
        this.serverSocket = new ServerSocket(0);
        startConnectionThread();
    }

    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        startConnectionThread();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public Map<UUID, ServerSideClient> getClients() {
        return clients;
    }

    public int getClientNumber() {
        return clients.size();
    }

    public void addClientListener(ClientListener clientListener) {
        this.clientListeners.add(clientListener);
    }

    public void removeClientListener(ClientListener clientListener) {
        this.clientListeners.remove(clientListener);
    }

    public void close() {
        clients.values().forEach(client -> {
            try {
                client.kick();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isClosed() {
        return serverSocket.isClosed();
    }

    public void kick(ServerSideClient client) {
        try {
            client.kick();
            clients.remove(client.getUuid());
            rooms.values().stream().filter(room -> room.isInside(client)).findFirst().ifPresent(room -> room.removeClient(client));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Room createRoom() {
        UUID uuid = UUID.randomUUID();
        Room room = new Room(uuid);
        rooms.put(uuid, room);
        return room;
    }

    public void removeRoom(Room room) {
        room.getClients().forEach(client -> client.setRoom(null));
        rooms.remove(room.getUuid());
    }

    public Map<UUID, Room> getRooms() {
        return rooms;
    }

    public void setAllowConnection(boolean allowConnection) {
        this.allowConnection = allowConnection;
    }

    public boolean getAllowConnection() {
        return allowConnection;
    }

    public void broadcast(String... message) {
        broadcast(Collections.emptySet(), message);
    }

    public void broadcast(Set<ServerSideClient> blacklist, String... message) {
        clients.values().stream().filter(client -> !blacklist.contains(client)).forEach(client -> {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Long> getPing(ServerSideClient client) {
        Ping ping = new Ping();
        pings.put(client.getUuid(), ping);
        ping.start();
        try {
            client.ping();
            return ping.getTime();
        } catch (IOException e) {
            pings.remove(client.getUuid());
            throw new RuntimeException(e);
        }
    }

    private void startConnectionThread() {
        new Thread(() -> {
            while(!serverSocket.isClosed()) {
                if(!allowConnection)
                    continue;
                try {
                    Socket socket = serverSocket.accept();
                    if(socket == null)
                        continue;
                    if(!allowConnection) {
                        socket.close();
                        return;
                    }
                    UUID uuid = UUID.randomUUID();
                    ServerSideClient client = new ServerSideClient(uuid, socket);
                    clients.put(uuid, client);
                    clientListeners.forEach(clientListener -> clientListener.onClientConnect(client));
                    startClientInputThread(client, socket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void startClientInputThread(ServerSideClient client, Socket socket) {
        new Thread(() -> {
            BufferedReader input;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while(!socket.isClosed()) {
                try {
                    String messagePrefix = input.readLine();
                    if(messagePrefix == null) {
                        clients.remove(client.getUuid());
                        rooms.values().stream().filter(room -> room.isInside(client)).findFirst().ifPresent(room -> room.removeClient(client));
                        break;
                    }
                    if(messagePrefix.equals(INTERNAL_MESSAGE_PREFIX)) {
                        String message = input.readLine();
                        if(message.equals(PING_MESSAGE) && pings.containsKey(client.getUuid()))
                            pings.get(client.getUuid()).complete();
                        if(message.equals(DISCONNECT_MESSAGE)) {
                            client.close();
                            clientListeners.forEach(clientListener -> clientListener.onClientDisconnect(client));
                            clients.remove(client.getUuid());
                            rooms.values().stream().filter(room -> room.isInside(client)).findFirst().ifPresent(room -> room.removeClient(client));
                            break;
                        }
                        continue;
                    }
                    if(!messagePrefix.equals(EXTERNAL_MESSAGE_PREFIX))
                        continue;
                    String message = input.readLine();
                    clientListeners.forEach(clientListener -> clientListener.onClientMessage(client, message));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                input.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
