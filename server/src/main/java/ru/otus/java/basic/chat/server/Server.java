package ru.otus.java.basic.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private final int port;
    private final Map<String, ClientHandler> clients = new HashMap<>();
    private final AuthenticationProvider authenticationProvider;

    /**
     * Creates the server
     *
     * @param port a port number for the server socket
     */
    public Server(int port) {
        this.port = port;
        this.authenticationProvider = new InMemoryAuthenticationProvider();
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
     * @throws UsernameAlreadyTakenException if the clients username is already present in the list
     */
    public synchronized void subscribe(ClientHandler clientHandler) throws UsernameAlreadyTakenException {
        if (clients.containsKey(clientHandler.getUsername())) {
            throw new UsernameAlreadyTakenException();
        }
        clients.put(clientHandler.getUsername(), clientHandler);
        broadcastMessage(clientHandler.getUsername() + " have entered the chat");
    }

    /**
     * Removes a ClientHandler from the list of clients and broadcasts a message to announce the departure of the user
     * Noop if the client's username is null, or if it is not on the clients list
     *
     * @param clientHandler the ClientHandler to remove
     */
    public synchronized void unsubscribe(ClientHandler clientHandler) {
        if (clientHandler.getUsername() != null && clients.containsKey(clientHandler.getUsername())) {
            broadcastMessage(clientHandler.getUsername() + " have left the chat");
            clients.remove(clientHandler.getUsername());
        }
    }

    /**
     * Broadcasts a message to all ClientHandlers in the clients list
     *
     * @param message the message to broadcast
     */
    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    /**
     * Sends a private message to a client with a specified username
     *
     * @param username the client's username
     * @param message  the message to send
     * @throws UsernameNotFoundException if the username not found in the clients lists
     */
    public synchronized void whisperMessage(String username, String message) throws UsernameNotFoundException {
        if (!clients.containsKey(username)) {
            throw new UsernameNotFoundException();
        }
        clients.get(username).sendMessage(message);
    }

    /**
     * Checks if a ClientHandler is in the clients list
     *
     * @param clientHandler the ClientHandler to check
     * @return true if found
     */
    public boolean isSubscribed(ClientHandler clientHandler) {
        return clients.containsKey(clientHandler.getUsername())
                && clients.get(clientHandler.getUsername()) == clientHandler;
    }

    /**
     * @return the current authentication provider
     */
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    /**
     * Kick a username from server
     *
     * @param username a username to kick
     */
    public synchronized void kick(String username, String kicker) throws UsernameNotFoundException {
        if (!clients.containsKey(username)) {
            throw new UsernameNotFoundException();
        }
        ClientHandler client = clients.get(username);
        unsubscribe(client);
        client.sendMessage("SERVER: You have been kicked by " + kicker);
        client.sendMessage("/bye");
        client.disconnect();
        broadcastMessage(kicker + " has kicked " + username + " from the chat");
    }
}
