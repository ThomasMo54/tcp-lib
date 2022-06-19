package com.motompro.tcplib.server;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class Client {

    private final Server server;
    private final UUID uuid;
    private final Socket socket;
    private final BufferedWriter output;

    protected Client(Server server, UUID uuid, Socket socket) throws IOException {
        this.server = server;
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

    public void sendMessage(String... message) throws IOException {
        StringBuilder messageBuilder = new StringBuilder();
        for(String s : message)
            messageBuilder.append(s).append(" ");
        output.write(messageBuilder.toString());
        output.flush();
    }
}
