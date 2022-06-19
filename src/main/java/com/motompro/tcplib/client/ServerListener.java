package com.motompro.tcplib.client;

public interface ServerListener {

    void onServerDisconnect();

    void onServerMessage(String[] message);
}
