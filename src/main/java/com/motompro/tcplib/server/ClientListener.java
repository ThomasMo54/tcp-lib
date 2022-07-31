package com.motompro.tcplib.server;

/**
 * This interface is used to get the output data of a server.<br>
 * To get it working you must implement it in a class and then register it in a {@link Server}
 * instance using the {@link Server#addClientListener(ClientListener)} method.
 * @param <SSC> An object extending {@link ServerSideClient}
 */
public interface ClientListener<SSC extends ServerSideClient> {

    /**
     * This method is called when a new client connect to the server
     * @param client The {@link SSC} object associated to the connecting client
     */
    void onClientConnect(SSC client);

    /**
     * This method is called when a client disconnect from the server
     * @param client The {@link SSC} object associated to the disconnecting client
     */
    void onClientDisconnect(SSC client);

    /**
     * This method is called when a message is received from a client
     * @param client The {@link SSC} object associated to the client who sent the message
     * @param message The {@link String} message
     */
    void onClientMessage(SSC client, String message);
}
