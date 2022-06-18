package com.motompro.tcplib.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Server {

    private final ServerSocket serverSocket;
    private final Map<UUID, Client> clients = new HashMap<>();

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

    public Map<UUID, Client> getClients() {
        return clients;
    }

    private void startConnectionThread() {
        new Thread(() -> {
            while(!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    if(socket == null)
                        continue;
                    UUID uuid = UUID.randomUUID();
                    clients.put(uuid, new Client(uuid, socket));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
