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
        Server server = null;
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
            server = new Server(port);
            server.start();
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }
}
