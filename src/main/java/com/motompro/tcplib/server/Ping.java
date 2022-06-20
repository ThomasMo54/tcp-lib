package com.motompro.tcplib.server;

import java.util.concurrent.CompletableFuture;

public class Ping {

    private long startTime;
    private final CompletableFuture<Long> futureTime = new CompletableFuture<>();

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void complete() {
        futureTime.complete(System.currentTimeMillis() - startTime);
    }

    public CompletableFuture<Long> getTime() {
        return futureTime;
    }
}
