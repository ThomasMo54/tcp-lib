package com.motompro.tcplib.server;

public interface ClientListener {

    void onClientConnect(Client client);

    void onClientDisconnect(Client client);

    void onClientMessage(Client client, String[] message);
}
