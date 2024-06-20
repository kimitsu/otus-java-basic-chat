package ru.otus.java.basic.chat.server;

public class ServerApplication {
    private static final int DEFAULT_PORT = 8189;

    /**
     * Start the chat server application
     * Arguments: [port]
     *
     * @param args passed arguments
     */
    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
            new Server(port).start();
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
        }
    }
}
