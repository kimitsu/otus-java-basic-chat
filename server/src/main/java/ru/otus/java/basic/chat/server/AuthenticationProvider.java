package ru.otus.java.basic.chat.server;

public interface AuthenticationProvider {
    /**
     * Authenticates and logs in a ClientHandler by a login and a password combination.
     * Calls ClientHandler.login on a success, otherwise sends error messages to the client.
     *
     * @param clientHandler the ClientHandler to authenticate
     * @param login         the login
     * @param password      the password
     * @return true if authentication successful, false if the login or the password are incorrect,
     * or if the user has already logged in
     */
    boolean authenticate(ClientHandler clientHandler, String login, String password);

    /**
     * Registers a username and logs in a ClientHandler, given the username and a login and a password combination.
     * Calls ClientHandler.login on a success, otherwise sends error messages to the client.
     *
     * @param clientHandler the ClientHandler to authenticate
     * @param login         the login
     * @param password      the password
     * @param username      the username
     * @return true if registration successful, false if the username, the login or the password are of incorrect format,
     * or if the username is already taken, or if the user has already logged in (which is an error)
     */
    boolean register(ClientHandler clientHandler, String login, String password, String username);
}
