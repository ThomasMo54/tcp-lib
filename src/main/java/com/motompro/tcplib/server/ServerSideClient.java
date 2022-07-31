package com.motompro.tcplib.server;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

/**
 * This class represents a client containing every server-side needed data and methods
 */
public class ServerSideClient {

    protected final UUID uuid;
    protected final Socket socket;
    private final PrintWriter output;
    protected Room room;

    /**
     * Create an instance of {@link ServerSideClient} with a specified {@link UUID} and the client's {@link Socket}
     * @param uuid The client's {@link UUID}
     * @param socket The client's {@link Socket}
     * @throws IOException
     */
    protected ServerSideClient(UUID uuid, Socket socket) throws IOException {
        this.uuid = uuid;
        this.socket = socket;
        this.output = new PrintWriter(socket.getOutputStream());
    }

    /**
     * This method returns the UUID of this client
     * @return The {@link UUID} of this {@link ServerSideClient}
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * This method returns the socket of this client
     * @return The {@link Socket} of this {@link ServerSideClient}
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * This method returns the IPv4 address of this client
     * @return A {@link String} representing the IPv4 address
     */
    public String getIp() {
        return socket.getRemoteSocketAddress().toString();
    }

    /**
     * Get whether this client is still connected to the server or not
     * @return A {@link Boolean}, <code>true</code> if connected, <code>false</code> if disconnected
     */
    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     * Specify the client's room. It <strong>MUST NOT</strong> be used, use {@link Room#addClient(ServerSideClient)} instead.
     * @param room The client's {@link Room}
     */
    protected void setRoom(Room room) {
        this.room = room;
    }

    /**
     * Get the client's room
     * @return An {@link Optional} containing the client's {@link Room}
     */
    public Optional<Room> getRoom() {
        return Optional.ofNullable(room);
    }

    /**
     * Send a message to the client
     * @param message The {@link String} message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        output.println(message);
        output.flush();
    }

    /**
     * Close the connection
     * @throws IOException
     */
    public void close() throws IOException {
        output.close();
        socket.close();
    }

    /**
     * Properly kick the client from the server. It <strong>MUST NOT</strong> be used, use {@link Server#kick(ServerSideClient)} instead.
     * @throws IOException
     */
    protected void kick() throws IOException {
        output.println(Server.INTERNAL_MESSAGE_PREFIX + " " + Server.DISCONNECT_MESSAGE);
        output.flush();
        close();
    }

    /**
     * Send ping message to the client. It <strong>MUST NOT</strong> be used, use {@link Server#getPing(ServerSideClient)} instead.
     * @throws IOException
     */
    protected void ping() throws IOException {
        output.println(Server.INTERNAL_MESSAGE_PREFIX + " " + Server.PING_MESSAGE);
        output.flush();
    }
}
