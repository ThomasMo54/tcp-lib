package com.motompro.tcplib.client;

import com.motompro.tcplib.server.Server;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class represents a TCP client.<br>
 * It handles communication with a distant server.
 */
public class Client {

    private final Socket socket;
    private final BufferedReader input;
    private final PrintWriter output;
    private final List<ServerListener> serverListeners = new CopyOnWriteArrayList<>();

    /**
     * @param ip The IP address the client will connect to
     * @param port The port the client will connect to
     * @throws IOException
     */
    public Client(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream());
        startServerInputThread();
    }

    /**
     * Register a {@link ServerListener} to this client
     * @param serverListener
     */
    public void addServerListener(ServerListener serverListener) {
        this.serverListeners.add(serverListener);
    }

    /**
     * Unregister a {@link ServerListener} from this client
     * @param serverListener
     */
    public void removeServerListener(ServerListener serverListener) {
        this.serverListeners.remove(serverListener);
    }

    /**
     * Get all registered {@link ServerListener}
     * @return A {@link List} of {@link ServerListener}
     */
    public List<ServerListener> getServerListeners() {
        return serverListeners;
    }

    /**
     * Close the connection between this client and the connected server
     * @throws IOException
     */
    public void close() throws IOException {
        output.write(Server.INTERNAL_MESSAGE_PREFIX + " " + Server.DISCONNECT_MESSAGE);
        output.flush();
        input.close();
        output.close();
        socket.close();
    }

    /**
     * Get whether this client is still connected to the server or not
     * @return A {@link Boolean}, <code>true</code> if connected, <code>false</code> if disconnected
     */
    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     * Send a {@link String} message to the server
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        output.println(message);
        output.flush();
    }

    private void startServerInputThread() {
        new Thread(() -> {
            // Get server's output while the connection is open
            while(!socket.isClosed()) {
                String completeMessage = null;
                // Read incoming server's message
                try {
                    completeMessage = input.readLine();
                } catch (IOException ignored) {}
                // Check if the message is null, a null message from the server represents a disconnection
                if(completeMessage == null) {
                    disconnectFromServer();
                    break;
                }
                // Split message
                String[] splitMessage = completeMessage.split(" ");
                // Check if the first part of the message to know if it is an internal message
                if(splitMessage[0].equals(Server.INTERNAL_MESSAGE_PREFIX) && splitMessage.length > 1) {
                    String message = splitMessage[1];
                    // Disconnect message
                    if(message.equals(Server.DISCONNECT_MESSAGE)) {
                        disconnectFromServer();
                        break;
                    }
                    continue;
                }
                String finalMessage = completeMessage;
                serverListeners.forEach(serverListener -> serverListener.onServerMessage(finalMessage));
            }
            // Close IO streams
            try {
                input.close();
                output.close();
            } catch (IOException ignored) {}
        }).start();
    }

    private void disconnectFromServer() {
        try {
            socket.close();
            output.close();
        } catch (IOException ignored) {}
        serverListeners.forEach(ServerListener::onServerDisconnect);
    }
}
