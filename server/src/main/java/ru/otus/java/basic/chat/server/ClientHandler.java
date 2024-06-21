package ru.otus.java.basic.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    private String username;

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
        startThread();
    }

    /**
     * Starts the thread which receives messages from the inputStream and broadcasts them to the server
     * Messages that start with "/" are not broadcast and cause special behavior:
     * /join name - subscribes or resubscribes the client to the server under a specified username
     * /w name message - sends the message to the specified username
     * /exit - sends /bye to the client and closes the connection
     */
    private void startThread() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    if (message.startsWith("/")) {
                        processCommand(message);
                    } else if (server.isSubscribed(this)) {
                        server.broadcastMessage("[" + username + "]: " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error while communicating with a client");
                e.printStackTrace();
            } catch (TerminateClientException e) {
            } finally {
                disconnect();
            }
        }).start();
    }

    /**
     * Processes a message containing a command
     * @param message the message to process
     * @throws TerminateClientException if the client connection is to be terminated
     */
    private void processCommand(String message) throws TerminateClientException {
        String[] arguments = message.split(" ");
        String command = arguments[0];
        if (command.equals("/join") && arguments.length > 1) {
            if (server.isSubscribed(this)) {
                server.unsubscribe(this);
            }
            username = arguments[1];
            try {
                server.subscribe(this);
            } catch (UsernameAlreadyTakenException e) {
                username = null;
                sendMessage("SERVER: Username is already taken");
                sendMessage("/bye");
                throw new TerminateClientException();
            }
        } else if (command.equals("/w") && arguments.length > 2) {
            try {
                String recipient = arguments[1];
                String whisper = Arrays.stream(arguments).skip(2).collect(Collectors.joining(" "));
                server.whisperMessage(recipient, "(whisper from " + username + "): " + whisper);
                sendMessage("(whispered to " + recipient + "): " + whisper);
            } catch (UsernameNotFoundException e) {
                sendMessage("SERVER: User not found");
            }
        } else if (command.equals("/exit")) {
            sendMessage("/bye");
            throw new TerminateClientException();
        } else {
            sendMessage("SERVER: Unrecognized command or incorrect arguments");
        }
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
