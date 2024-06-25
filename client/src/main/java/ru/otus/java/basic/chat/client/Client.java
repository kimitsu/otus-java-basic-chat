package ru.otus.java.basic.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    //private final String username;

    /**
     * Creates a client which connects to a server and starts sending and receiving messages
     * It reads lines from system input and sends them to the server until "/exit" is entered
     * It reads messages from the server and prints them to the system output until "/bye" is received
     *
     * @param host a host server address
     * @param port a host server port
     * @throws IOException if something goes wrong
     */
    public Client(String host, int port) throws IOException {
        Scanner scanner = new Scanner(System.in);
        socket = new Socket(host, port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Connection established");
        Thread thread = startThread();
        while (true) {
            String message = scanner.nextLine();
            if (socket.isClosed()) {
                System.out.println("Connection lost");
                break;
            }
            sendMessage(message);
            if (message.equals("/exit")) {
                System.out.println("Disconnecting...");
                break;
            }
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a thread that reads messages from inputStream and prints them to System.out
     * until "/bye" is received
     *
     * @return the thread
     */
    private Thread startThread() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    if (message.equals("/bye")) {
                        System.out.println("Server has terminated the connection");
                        break;
                    }
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.out.println("Error while communicating with the server");
                e.printStackTrace();
            } finally {
                disconnect();
            }
        });
        thread.start();
        return thread;
    }

    /**
     * Sends a message to the server if the socket is not closed
     * Prints an error message in case of an IOException
     *
     * @param message the message to send
     */
    private void sendMessage(String message) {
        if (!socket.isClosed()) {
            try {
                outputStream.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Failed to send the message");
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the streams and the socket
     */
    private void disconnect() {
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
}
