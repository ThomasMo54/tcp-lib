package com.motompro.tcplib.client;

import com.motompro.tcplib.server.Server;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Client {

    private final Socket socket;
    private final BufferedReader input;
    private final BufferedWriter output;
    private final Set<ServerListener> serverListeners = new HashSet<>();

    public Client(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        startServerInputThread();
    }

    public void addServerListener(ServerListener serverListener) {
        this.serverListeners.add(serverListener);
    }

    public void removeServerListener(ServerListener serverListener) {
        this.serverListeners.remove(serverListener);
    }

    public void close() {
        try {
            output.write(Server.INTERNAL_MESSAGE_PREFIX);
            output.write(Server.DISCONNECT_MESSAGE);
            output.flush();
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void sendMessage(String... message) throws IOException {
        output.write(Server.EXTERNAL_MESSAGE_PREFIX);
        for(String s : message)
            output.write(s);
        output.flush();
    }

    private void startServerInputThread() {
        new Thread(() -> {
            while(!socket.isClosed()) {
                try {
                    String messagePrefix = input.readLine();
                    if(messagePrefix == null) {
                        socket.close();
                        output.close();
                        serverListeners.forEach(ServerListener::onServerDisconnect);
                        break;
                    }
                    if(messagePrefix.equals(Server.INTERNAL_MESSAGE_PREFIX)) {
                        String message = input.readLine();
                        if(message.equals(Server.DISCONNECT_MESSAGE)) {
                            socket.close();
                            output.close();
                            serverListeners.forEach(ServerListener::onServerDisconnect);
                            break;
                        }
                        continue;
                    }
                    if(!messagePrefix.equals(Server.EXTERNAL_MESSAGE_PREFIX))
                        continue;
                    String message = input.readLine();
                    serverListeners.forEach(serverListener -> serverListener.onServerMessage(message));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                input.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
