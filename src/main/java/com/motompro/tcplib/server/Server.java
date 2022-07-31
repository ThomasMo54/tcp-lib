package com.motompro.tcplib.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class represents a TCP server. It handles client connection and communication.
 * @param <SSC> An object extending {@link ServerSideClient}
 */
public abstract class Server<SSC extends ServerSideClient> {

    // This String is used to know if a message is internal to the lib
    public static final String INTERNAL_MESSAGE_PREFIX = "&internal&";
    public static final String DISCONNECT_MESSAGE = "disconnect";
    public static final String PING_MESSAGE = "ping";

    private final ServerSocket serverSocket;
    private final Map<UUID, SSC> clients = new HashMap<>();
    private final List<ClientListener<SSC>> clientListeners = new CopyOnWriteArrayList<>();
    private final Map<UUID, Room<SSC>> rooms = new HashMap<>();
    private boolean allowConnection = true;
    private final Map<UUID, Ping> pings = new HashMap<>();

    /**
     * Create an instance of {@link Server} which will listen to an unknown free port
     * @throws IOException
     */
    public Server() throws IOException {
        this.serverSocket = new ServerSocket(0);
        startConnectionThread();
    }

    /**
     * Create an instance of {@link Server} with a specified port
     * @param port The wanted port
     * @throws IOException
     */
    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        startConnectionThread();
    }

    /**
     * Get the port the server is listening to
     * @return An integer representing the port
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Get the clients connected to this server
     * @return A {@link Map} with client's {@link UUID} as key and {@link SSC} associated to the clients as value
     */
    public Map<UUID, SSC> getClients() {
        return clients;
    }

    /**
     * Get the amount of connected clients
     * @return An integer representing the amount
     */
    public int getClientNumber() {
        return clients.size();
    }

    /**
     * Register a {@link ClientListener} to the server
     * @param clientListener
     */
    public void addClientListener(ClientListener<SSC> clientListener) {
        this.clientListeners.add(clientListener);
    }

    /**
     * Unregister a {@link ClientListener} from the server
     * @param clientListener
     */
    public void removeClientListener(ClientListener<SSC> clientListener) {
        this.clientListeners.remove(clientListener);
    }

    /**
     * Get all registered {@link ClientListener}
     * @return A {@link List} of {@link ClientListener}
     */
    public List<ClientListener<SSC>> getClientListeners() {
        return clientListeners;
    }

    /**
     * This method kick all connected clients and then close server connection
     * @throws IOException
     */
    public void close() throws IOException {
        // Properly kick all connected clients
        clients.values().forEach(client -> {
            try {
                client.kick();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // Close socket
        serverSocket.close();
    }

    /**
     * Get whether this client is still connected to the server or not
     * @return A {@link Boolean}, <code>true</code> if closed, <code>false</code> if opened
     */
    public boolean isClosed() {
        return serverSocket.isClosed();
    }

    /**
     * Kick a client from the server
     * @param client The {@link SSC} which will be kicked
     * @throws IOException
     */
    public void kick(SSC client) throws IOException {
        client.kick();
        clients.remove(client.getUuid());
        rooms.values().stream().filter(room -> room.isInside(client)).findFirst().ifPresent(room -> room.removeClient(client));
    }

    /**
     * Register a room to the server
     * @param room The {@link Room} which will be registered
     */
    public void addRoom(Room<SSC> room) {
        rooms.put(room.getUuid(), room);
    }

    /**
     * Unregister a room from the server
     * @param room The {@link Room} which will be unregistered
     */
    public void removeRoom(Room<SSC> room) {
        room.getClients().forEach(client -> client.setRoom(null));
        rooms.remove(room.getUuid());
    }

    /**
     * Get all registered rooms
     * @return A {@link Map} with rooms' {@link UUID} as key and {@link Room} as value
     */
    public Map<UUID, Room<SSC>> getRooms() {
        return rooms;
    }

    /**
     * Set if the server should accept new client connection.<br>
     * This parameter is set at <code>true</code> by default
     * @param allowConnection A {@link Boolean}, <code>true</code> if it accepts, <code>false</code> if not
     */
    public void setAllowConnection(boolean allowConnection) {
        this.allowConnection = allowConnection;
    }

    /**
     * Get if the server accepts new client connection.<br>
     * This parameter is set at <code>true</code> by default
     * @return A {@link Boolean}, <code>true</code> if it accepts, <code>false</code> if not
     */
    public boolean getAllowConnection() {
        return allowConnection;
    }

    /**
     * Send a message to every connected client
     * @param message The {@link String} message
     */
    public void broadcast(String message) {
        broadcast(Collections.emptySet(), message);
    }

    /**
     * Send a message to every client of the server excepted the clients passed in blacklist
     * @param blacklist A {@link Set} of {@link SSC} representing the excepted clients
     * @param message The {@link String} message
     */
    public void broadcast(Set<SSC> blacklist, String message) {
        clients.values().stream().filter(client -> !blacklist.contains(client)).forEach(client -> {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get the delay (in millisecond) of a client.
     * @param client The {@link SSC} we want to check
     * @return A {@link CompletableFuture} containing the delay, it is completed when the ping is back to the server
     */
    public CompletableFuture<Long> getPing(SSC client) {
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

    /**
     * The connection thread
     */
    private void startConnectionThread() {
        new Thread(() -> {
            // Accept new connections while the server is opened
            while(!serverSocket.isClosed()) {
                // Do not accept connection if the server does not allow it
                if(!allowConnection)
                    continue;
                try {
                    // Create the new connected client socket
                    Socket socket = serverSocket.accept();
                    if(socket == null)
                        continue;
                    // Close new client's socket if the server does not allow connections
                    if(!allowConnection) {
                        socket.close();
                        return;
                    }
                    // Generate a random UUID for the client
                    UUID uuid = UUID.randomUUID();
                    // Instantiate the client object
                    ServerSideClient client = new ServerSideClient(uuid, socket);
                    // Generate the generic type associated with the client
                    SSC generatedClient = generateClient(client);
                    clients.put(uuid, generatedClient);
                    clientListeners.forEach(clientListener -> clientListener.onClientConnect(generatedClient));
                    // Start client's input connection thread
                    startClientInputThread(generatedClient, socket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * The client commuinication thread
     * @param client The {@link SSC} object associated with the client
     * @param socket The client's {@link Socket}
     */
    private void startClientInputThread(SSC client, Socket socket) {
        new Thread(() -> {
            // Create client's buffered input stream
            BufferedReader input;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Get input messages while the client is connected
            while(!socket.isClosed()) {
                String completeMessage = null;
                // Read incoming client's message
                try {
                    completeMessage = input.readLine();
                } catch (IOException ignored) {}
                // Check if the message is null, a null message from the client represents a disconnection
                if(completeMessage == null) {
                    disconnectClient(client);
                    break;
                }
                // Split message
                String[] splitMessage = completeMessage.split(" ");
                // Check if the first part of the message to know if it is an internal message
                if(splitMessage[0].equals(INTERNAL_MESSAGE_PREFIX) && splitMessage.length > 1) {
                    String message = splitMessage[1];
                    // Ping message
                    if(message.equals(PING_MESSAGE) && pings.containsKey(client.getUuid())) {
                        pings.get(client.getUuid()).complete();
                        pings.remove(client.getUuid());
                    }
                    // Disconnect message
                    if(message.equals(DISCONNECT_MESSAGE)) {
                        try {
                            client.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        disconnectClient(client);
                        break;
                    }
                    continue;
                }
                String finalMessage = completeMessage;
                // Call client message method
                clientListeners.forEach(clientListener -> clientListener.onClientMessage(client, finalMessage));
                client.getRoom().ifPresent(room -> {
                    ((Room<SSC>) room).getRoomListeners().forEach(roomListener -> roomListener.onClientMessage(client, finalMessage));
                });
            }
            // Close IO streams
            try {
                input.close();
            } catch (IOException ignored) {}
        }).start();
    }

    /**
     * This method properly closes client connection
     * @param client The {@link SSC} object associated with the client
     */
    private void disconnectClient(SSC client) {
        // Call client disconnected method
        clientListeners.forEach(clientListener -> clientListener.onClientDisconnect(client));
        clients.remove(client.getUuid());
        rooms.values().stream().filter(room -> room.isInside(client)).findFirst().ifPresent(room -> {
            room.removeClient(client);
            room.getRoomListeners().forEach(roomListener -> roomListener.onClientDisconnect(client));
        });
    }

    /**
     * Generate the {@link SSC} object when a new client just connected
     * @param client The {@link ServerSideClient} object associated with the newly connected client
     * @return The generated {@link SSC} associated with the newly connected client
     */
    protected abstract SSC generateClient(ServerSideClient client);
}
