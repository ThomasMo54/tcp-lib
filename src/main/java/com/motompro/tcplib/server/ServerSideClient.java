package com.motompro.tcplib.server;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

public class ServerSideClient {

    protected final UUID uuid;
    protected final Socket socket;
    private final PrintWriter output;
    protected Room room;

    protected ServerSideClient(UUID uuid, Socket socket) throws IOException {
        this.uuid = uuid;
        this.socket = socket;
        this.output = new PrintWriter(socket.getOutputStream());
    }

    public UUID getUuid() {
        return uuid;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getIp() {
        return socket.getRemoteSocketAddress().toString();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void setRoom(Room room) {
        if(this.room != null)
            room.removeClient(this);
        this.room = room;
    }

    public Optional<Room> getRoom() {
        return Optional.ofNullable(room);
    }

    public void sendMessage(String message) throws IOException {
        output.println(message);
        output.flush();
    }

    public void close() throws IOException {
        output.close();
        socket.close();
    }

    protected void kick() throws IOException {
        output.println(Server.INTERNAL_MESSAGE_PREFIX + " " + Server.DISCONNECT_MESSAGE);
        output.flush();
        close();
    }

    protected void ping() throws IOException {
        output.println(Server.INTERNAL_MESSAGE_PREFIX + " " + Server.PING_MESSAGE);
        output.flush();
    }
}
