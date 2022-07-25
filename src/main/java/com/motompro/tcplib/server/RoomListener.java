package com.motompro.tcplib.server;

public interface RoomListener<SSC extends ServerSideClient> {

    void onClientDisconnect(SSC client);

    void onClientMessage(SSC client, String message);
}
