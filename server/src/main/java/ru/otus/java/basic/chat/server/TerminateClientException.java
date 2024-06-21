package ru.otus.java.basic.chat.server;

public class TerminateClientException extends Exception {
    /**
     * The client connection is to be terminated
     */
    public TerminateClientException() {
        super();
    }
}
