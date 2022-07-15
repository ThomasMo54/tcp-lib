package com.motompro.tcplib.server;

public interface ClientListener<SSC extends ServerSideClient> {

    void onClientConnect(SSC client);

    void onClientDisconnect(SSC client);

    void onClientMessage(SSC client, String message);
}
