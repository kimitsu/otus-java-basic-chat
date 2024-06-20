package ru.otus.java.basic.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    private String username;
    private static int usersCount = 0;

    /**
     * Creates a thread that will read and write to the socket of the established connection to a client
     *
     * @param server the Server which accepted the connection
     * @param socket the Socket of the connection
     * @throws IOException if something went wrong
     */
    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        usersCount++;
        this.username = "user" + usersCount;
        new Thread(() -> {
            try {
                server.subscribe(this);
                while (true) {
                    String message = inputStream.readUTF();
                    if (message.startsWith("/")) {
                        if (message.equals("/exit")) {
                            sendMessage("/bye");
                            break;
                        }
                        continue;
                    }
                    server.broadcastMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Error while communicating with a client");
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    /**
     * Sends a message to the client
     *
     * @param message a message to send
     */
    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Failed to send the message");
            e.printStackTrace();
        }
    }

    /**
     * Close the streams and the socket
     */
    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            System.out.println("Error while disconnecting");
            e.printStackTrace();
        }
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            System.out.println("Error while disconnecting");
            e.printStackTrace();
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error while disconnecting");
            e.printStackTrace();
        }
    }

    /**
     * @return client's username
     */
    public String getUsername() {
        return username;
    }
}
