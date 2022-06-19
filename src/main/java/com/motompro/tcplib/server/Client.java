package com.motompro.tcplib.server;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

public class Client {

    private final UUID uuid;
    private final Socket socket;
    private final BufferedWriter output;
    private Room room;

    protected Client(UUID uuid, Socket socket) throws IOException {
        this.uuid = uuid;
        this.socket = socket;
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getIp() {
        return socket.getRemoteSocketAddress().toString();
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Optional<Room> getRoom() {
        return Optional.ofNullable(room);
    }

    public void sendMessage(String... message) throws IOException {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(Server.EXTERNAL_MESSAGE_PREFIX);
        for(String s : message)
            messageBuilder.append(" ").append(s);
        output.write(messageBuilder.toString());
        output.flush();
    }

    public void close() throws IOException {
        output.close();
        socket.close();
    }

    public void kick() throws IOException {
        output.write(Server.INTERNAL_MESSAGE_PREFIX + " " + Server.DISCONNECT_MESSAGE);
        output.flush();
        close();
    }
}
