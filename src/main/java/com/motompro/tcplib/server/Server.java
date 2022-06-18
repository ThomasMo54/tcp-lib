package com.motompro.tcplib.server;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {

    private final ServerSocket serverSocket;

    public Server() throws IOException {
        this.serverSocket = new ServerSocket(0);
    }

    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}
