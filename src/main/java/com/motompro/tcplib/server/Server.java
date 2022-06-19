package com.motompro.tcplib.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static final String INTERNAL_MESSAGE_PREFIX = "internal";
    private static final String DISCONNECT_MESSAGE = "disconnect";

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
                    clients.put(uuid, client);
                    clientListeners.forEach(clientListener -> clientListener.onClientConnect(client));
                    startClientInputThread(client, socket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void startClientInputThread(Client client, Socket socket) {
        new Thread(() -> {
            BufferedReader input;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while(!socket.isClosed()) {
                try {
                    String message = input.readLine();
                    if(message == null)
                        continue;
                    String[] splitMessage = message.split(" ");
                    if(splitMessage.length == 0)
                        continue;
                    if(splitMessage[0].equals(INTERNAL_MESSAGE_PREFIX) && splitMessage.length > 1) {
                        if(splitMessage[1].equals(DISCONNECT_MESSAGE)) {
                            client.close();
                            clientListeners.forEach(clientListener -> clientListener.onClientDisconnect(client));
                            return;
                        }
                        continue;
                    }
                    clientListeners.forEach(clientListener -> clientListener.onClientMessage(client, splitMessage));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
