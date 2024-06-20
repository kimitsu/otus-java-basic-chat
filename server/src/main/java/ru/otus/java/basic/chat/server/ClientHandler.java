package ru.otus.java.basic.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private String username;
    private static int usersCount = 0;

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        usersCount++;
        this.username = "user" + usersCount;
        new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    if (message.startsWith("/")) {
                        if (message.equals("/exit")) {
                            sendMessage("/exitok");
                            break;
                        }
                        continue;
                    }
                    server.broadcastMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
}
