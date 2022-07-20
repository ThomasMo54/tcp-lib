package com.motompro.tcplib.client;

import com.motompro.tcplib.server.Server;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Client {

    private final Socket socket;
    private final BufferedReader input;
    private final PrintWriter output;
    private final Set<ServerListener> serverListeners = new HashSet<>();

    public Client(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream());
        startServerInputThread();
    }

    public void addServerListener(ServerListener serverListener) {
        this.serverListeners.add(serverListener);
    }

    public void removeServerListener(ServerListener serverListener) {
        this.serverListeners.remove(serverListener);
    }

    public Set<ServerListener> getServerListeners() {
        return serverListeners;
    }

    public void close() {
        try {
            output.write(Server.INTERNAL_MESSAGE_PREFIX + " " + Server.DISCONNECT_MESSAGE);
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

    public void sendMessage(String message) throws IOException {
        output.println(message);
        output.flush();
    }

    private void startServerInputThread() {
        new Thread(() -> {
            while(!socket.isClosed()) {
                String completeMessage = null;
                try {
                    completeMessage = input.readLine();
                } catch (IOException ignored) {}
                if(completeMessage == null) {
                    try {
                        socket.close();
                        output.close();
                    } catch (IOException ignored) {}
                    serverListeners.forEach(ServerListener::onServerDisconnect);
                    break;
                }
                String[] splitMessage = completeMessage.split(" ");
                if(splitMessage[0].equals(Server.INTERNAL_MESSAGE_PREFIX) && splitMessage.length > 1) {
                    String message = splitMessage[1];
                    if(message.equals(Server.DISCONNECT_MESSAGE)) {
                        try {
                            socket.close();
                            output.close();
                        } catch (IOException ignored) {}
                        serverListeners.forEach(ServerListener::onServerDisconnect);
                        break;
                    }
                    continue;
                }
                String finalMessage = completeMessage;
                synchronized(serverListeners) {
                    for(Iterator<ServerListener> iterator = serverListeners.iterator(); iterator.hasNext();)
                        iterator.next().onServerMessage(finalMessage);
                }
            }
            try {
                input.close();
            } catch (IOException ignored) {}
        }).start();
    }
}
