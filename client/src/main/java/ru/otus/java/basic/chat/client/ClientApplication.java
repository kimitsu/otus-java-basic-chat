package ru.otus.java.basic.chat.client;

import java.io.IOException;

public class ClientApplication {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8189;
    public static void main(String[] args) {
        try {
            String host = args.length > 0 ? args[0] : DEFAULT_HOST;
            int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
            new Client(host, port);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
        } catch (IOException e) {
            System.out.println("Network error occurred");
            e.printStackTrace();
        }
    }
}
