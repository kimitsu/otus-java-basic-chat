package ru.otus.java.basic.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int port;
    private final List<ClientHandler> clients = new ArrayList<>();

    /**
     * Creates the server
     *
     * @param port a port number for the server socket
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Opens the server socket on the specified port number
     * Listens to connections and creates a ClientHandler object for each accepted socket connection
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(this, socket);
                } catch (IOException e) {
                    System.out.println("Failed to establish connection with a client");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Network error occurred");
            e.printStackTrace();
        }
    }

    /**
     * Adds a ClientHandler to the list of clients and broadcasts a message to announce the new user
     *
     * @param clientHandler the ClientHandler to add to the list
     */
    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage("В чат зашел " + clientHandler.getUsername());
    }

    /**
     * Removes a ClientHandler from the list of clients and broadcasts a message to announce the departure of the user
     *
     * @param clientHandler the ClientHandler to remove
     */
    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел " + clientHandler.getUsername());
    }

    /**
     * Broadcasts a message to all ClientHandlers in the clients list
     *
     * @param message the message to broadcast
     */
    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
