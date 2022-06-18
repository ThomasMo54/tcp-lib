package com.motompro.tcplib.server;

import java.net.Socket;

public class Client {

    private final Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
    }

    public String getIp() {
        return socket.getRemoteSocketAddress().toString();
    }
}
