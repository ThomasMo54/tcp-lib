package com.motompro.tcplib.server;

public interface ClientListener {

    void onClientConnect(ServerSideClient client);

    void onClientDisconnect(ServerSideClient client);

    void onClientMessage(ServerSideClient client, String message);
}
