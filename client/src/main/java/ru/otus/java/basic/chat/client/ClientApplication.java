package ru.otus.java.basic.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientApplication {
    public static void main(String[] args) {
        try {
            new Client();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
