package com.motompro.tcplib.client;

import com.motompro.tcplib.server.ClientListener;
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

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void addServerListener(ServerListener serverListener) {
        this.serverListeners.add(serverListener);
    }

    public void removeServerListener(ServerListener serverListener) {
        this.serverListeners.remove(serverListener);
    }

    public void sendMessage(String... message) throws IOException {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(Server.EXTERNAL_MESSAGE_PREFIX);
        for(String s : message)
            messageBuilder.append(" ").append(s);
        output.write(messageBuilder.toString());
        output.flush();
    }

    private void startServerInputThread() {
        new Thread(() -> {
            while(!socket.isClosed()) {
                try {
                    String message = input.readLine();
                    if(message == null)
                        break;
                    String[] splitMessage = message.split(" ");
                    if(splitMessage.length == 0)
                        continue;
                    if(splitMessage[0].equals(Server.INTERNAL_MESSAGE_PREFIX) && splitMessage.length > 1) {
                        if(splitMessage[1].equals(Server.DISCONNECT_MESSAGE)) {
                            socket.close();
                            output.close();
                            serverListeners.forEach(ServerListener::onServerDisconnect);
                            break;
                        }
                        continue;
                    }
                    serverListeners.forEach(serverListener -> serverListener.onServerMessage(splitMessage));
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
