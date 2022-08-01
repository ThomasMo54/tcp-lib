# tcp-lib

A very basic Java library which handles TCP/IP communication.

## Information

<ul>
    <li>Java 8</li>
    <li>Maven</li>
</ul>

## Getting started

### Installation

Using Maven, copy this into your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/ThomasMo54/tcp-lib</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.motompro</groupId>
        <artifactId>tcp-lib</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### Example

<p>This example shows how to establish a connection between a server and a client and how to send and receive data.</p> 

`MyServer.java`
```java
public class MyServer extends Server<ServerSideClient> implements ClientListener<ServerSideClient> {

    private static final int PORT = 11111;

    public MyServer() throws IOException {
        // Make the server listen to port 11111
        super(PORT);
        // Register the client listener to receive clients messages
        this.addClientListener(this);
    }
    
    /*
     This method is used when you want to use your own ServerSideClient class
     In this example we use the default ServerSideClient.
     */
    @Override
    protected ServerSideClient generateClient(ServerSideClient client) {
        return client;
    }
    
    // This method is called when a client is connecting to the server.
    @Override
    public void onClientConnect(ServerSideClient client) {
        System.out.println("A client has connected!");
    }

    // This method is called when a client is disconnecting from the server
    @Override
    public void onClientDisconnect(ServerSideClient client) {
        System.out.println("A client has disconnected!");
    }
    
    // This method is called when a client send a message to the server
    @Override
    public void onClientMessage(ServerSideClient client, String message) {
        System.out.println("A client sent a message: " + message);
        try {
            // Send a message back to the client
            client.sendMessage("Hi!");
        } catch (IOException e) {
            System.out.println("Failed to answer");
        }
    }
}
```

`MyClient.java`
```java
public class MyClient implements ServerListener {

    public MyClient() {
        try {
            // Connect to the server giving the IP address (localhost in this example) and the port
            Client client = new Client("127.0.0.1", 11111);
            // Register the server listener to receive server messages
            client.addServerListener(this);
            // Send a message to the server
            client.sendMessage("Hello world!");
        } catch (IOException e) {
            System.out.println("Failed to connect to server");
        }
    }
    
    // This method is called when the server disconnects
    @Override
    public void onServerDisconnect() {
        System.out.println("Server disconnected!");
    }
    
    // This method is called when the server sends a message
    @Override
    public void onServerMessage(String message) {
        System.out.println("The server sent a message: " + message);
    }
}
```

## Author

- [@ThomasMo54](https://www.github.com/ThomasMo54)