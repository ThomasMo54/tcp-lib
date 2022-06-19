package com.motompro.tcplib.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private final ServerSocket serverSocket;
    private final Map<UUID, Client> clients = new HashMap<>();
    private final Set<ClientListener> clientListeners = new HashSet<>();

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

    public void addClientListener(ClientListener clientListener) {
        this.clientListeners.add(clientListener);
    }

    public void removeClientListener(ClientListener clientListener) {
        this.clientListeners.remove(clientListener);
    }

    private void startConnectionThread() {
        new Thread(() -> {
            while(!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    if(socket == null)
                        continue;
                    UUID uuid = UUID.randomUUID();
                    Client client = new Client(this, uuid, socket);
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    clients.put(uuid, client);
                    clientListeners.forEach(clientListener -> clientListener.onClientConnect(client));
                    startClientInputThread(client, input);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void startClientInputThread(Client client, BufferedReader input) {
        new Thread(() -> {
            try {
                String[] message = input.readLine().split(" ");
                clientListeners.forEach(clientListener -> clientListener.onClientMessage(client, message));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
