package com.motompro.tcplib.server;

public interface ClientListener<SSB extends ServerSideClient> {

    void onClientConnect(SSB client);

    void onClientDisconnect(SSB client);

    void onClientMessage(SSB client, String message);
}
