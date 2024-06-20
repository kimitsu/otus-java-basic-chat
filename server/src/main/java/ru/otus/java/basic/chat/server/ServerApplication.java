package ru.otus.java.basic.chat.server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {
    public static void main(String[] args) {
        int port = 8189;
        new Server(port).start();
    }
}
