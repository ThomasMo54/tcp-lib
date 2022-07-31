package com.motompro.tcplib.server;

/**
 * This interface is used to get the output data of a room.<br>
 * To get it working you must implement it in a class and then register it in a {@link Room}
 * instance using the {@link Room#addRoomListener(RoomListener)} method.
 * @param <SSC> An object extending {@link ServerSideClient}
 */
public interface RoomListener<SSC extends ServerSideClient> {

    /**
     * This method is called when a room's client disconnect from the server
     * @param client The {@link SSC} object associated to the disconnecting client
     */
    void onClientDisconnect(SSC client);

    /**
     * This method is called when a message is received from a room's client
     * @param client The {@link SSC} object associated to the client who sent the message
     * @param message The {@link String} message
     */
    void onClientMessage(SSC client, String message);
}
