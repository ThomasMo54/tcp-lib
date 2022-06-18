package com.motompro.tcplib.server;

import java.net.Socket;
import java.util.UUID;

public class Client {

    private final UUID uuid;
    private final Socket socket;

    public Client(UUID uuid, Socket socket) {
        this.uuid = uuid;
        this.socket = socket;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getIp() {
        return socket.getRemoteSocketAddress().toString();
    }
}
