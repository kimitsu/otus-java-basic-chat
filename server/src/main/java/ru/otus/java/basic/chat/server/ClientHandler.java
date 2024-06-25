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
    private int id;
    private static int idCounter = 0;

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
        this.id = idCounter++;
        startThread();
    }

    /**
     * Starts the thread which receives messages from the inputStream and broadcasts them to the server
     * Messages that start with "/" are not broadcast and cause special behavior:
     * /auth login password - tries to log in to the server using a login and a password combination.
     * /reg username login password - tries to register a specific username for a login and a password combination.
     * /w name message - sends the message to the specified username.
     * /exit - sends /bye to the client and closes the connection.
     */
    private void startThread() {
        System.out.println("Client connection established (id:" + id + ").");
        new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    System.out.println("RECV(id:" + id + "): " + message);
                    if (message.startsWith("/")) {
                        processCommand(message);
                    } else {
                        if (!isLoggedIn()) {
                            sendMessage("SERVER: You are not authenticated. Use /auth <login> <password> or /reg <username> <login> <password>");
                            continue;
                        }
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
        if (command.equals("/auth")) {
            if (arguments.length != 3) {
                sendMessage("SERVER: Incorrect arguments. Use /auth <login> <password>");
                return;
            }
            server.getAuthenticationProvider().authenticate(this, arguments[1], arguments[2]);
            return;
        }
        if (command.equals("/reg")) {
            if (arguments.length != 4) {
                sendMessage("SERVER: Incorrect arguments. Use /reg <username> <login> <password>");
            }
            server.getAuthenticationProvider().register(this, arguments[2], arguments[3], arguments[1]);
            return;
        }
        if (!isLoggedIn()) {
            sendMessage("SERVER: You are not authenticated. Use /auth <login> <password> or /reg <username> <login> <password>");
            return;
        }
        if (command.equals("/w")) {
            if (arguments.length < 3) {
                sendMessage("SERVER: Incorrect arguments. Use /w <username> <message>...");
            }
            try {
                String recipient = arguments[1];
                String whisper = Arrays.stream(arguments).skip(2).collect(Collectors.joining(" "));
                sendMessage("(whispered to " + recipient + "): " + whisper);
                server.whisperMessage(recipient, "(whisper from " + username + "): " + whisper);
            } catch (UsernameNotFoundException e) {
                sendMessage("SERVER: User not found");
            }
            return;
        }
        if (command.equals("/exit")) {
            sendMessage("/bye");
            throw new TerminateClientException();
        }
        sendMessage("SERVER: Unrecognized command");
    }

    /**
     * Logs in under a specific username. Sends an error message if the username is already taken.
     *
     * @param username the username to log in
     * @return true if login successful, false if the username is already taken
     */
    public synchronized boolean login(String username) {
        server.unsubscribe(this);
        this.username = username;
        try {
            server.subscribe(this);
        } catch (UsernameAlreadyTakenException e) {
            this.username = null;
            sendMessage("SERVER: User has already logged in");
            return false;
        }
        return true;
    }

    /**
     * Sends a message to the client
     *
     * @param message a message to send
     */
    public void sendMessage(String message) {
        try {
            System.out.println("SEND(id:" + id + "): " + message);
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
     * @return the client's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return true if the client is logged in under some username, false if the client is not logged in
     */
    private boolean isLoggedIn() {
        return username != null;
    }
}
