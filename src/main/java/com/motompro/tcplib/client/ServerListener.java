package com.motompro.tcplib.client;

/**
 * This interface is used to get the output data of a server.<br>
 * To get it working you must implement it in a class and then register it in a {@link Client}
 * instance using the {@link Client#addServerListener(ServerListener)} method.
 */
public interface ServerListener {

    /**
     * This method is called when the server the client is connected to is closing
     */
    void onServerDisconnect();

    /**
     * This method is called when a message is received from the server the client is connected to
     * @param message The message
     */
    void onServerMessage(String message);
}
