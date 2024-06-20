package ru.otus.java.basic.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Client() throws IOException {
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket("localhost", 8189);
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    System.out.println(message);
                    if (message.equals("/exitok")) {
                        disconnect();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();

            }
        }).start();
        while (true) {
            String message = scanner.nextLine();
            outputStream.writeUTF(message);
            if (message.equals("/exit")) {
                break;
            }
        }

    }

    public void disconnect() {
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
}
