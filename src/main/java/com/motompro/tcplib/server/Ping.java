package com.motompro.tcplib.server;

import java.util.concurrent.CompletableFuture;

/**
 * This class represents a TCP ping. It is used to store the necessary data and methods to get the latency of a client.
 */
public class Ping {

    private long startTime;
    private final CompletableFuture<Long> futureTime = new CompletableFuture<>();

    /**
     * This method set the time when the ping has been sent
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * This method set the time when the ping back to the server
     */
    public void complete() {
        futureTime.complete(System.currentTimeMillis() - startTime);
    }

    /**
     * This method is used to get the elapsed time between the ping launch and the ping back.<br>
     * The returned {@link CompletableFuture} is completed when the ping is back.
     * @return A {@link CompletableFuture} containing the elapsed time.
     */
    public CompletableFuture<Long> getTime() {
        return futureTime;
    }
}
