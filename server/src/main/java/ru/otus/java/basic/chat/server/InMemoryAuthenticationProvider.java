package ru.otus.java.basic.chat.server;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private class User {
        private String login;
        private String password;
        private String username;
        private UserRole role;

        public User(String login, String password, String username, UserRole role) {
            this.login = login;
            this.password = password;
            this.username = username;
            this.role = role;
        }
    }

    private final List<User> users;

    /**
     * Initializes a simple in-memory authentication provider
     */
    public InMemoryAuthenticationProvider() {
        this.users = new ArrayList<>();
        this.users.add(new User("user1", "pass1", "Ivanov", UserRole.ADMIN));
        this.users.add(new User("user2", "pass2", "Pertov", UserRole.USER));
        this.users.add(new User("user3", "pass3", "Sidoroff", UserRole.USER));
        System.out.println("In-memory authentication service initialized.");
    }

    /**
     * Finds a User for a specific login and password combination
     *
     * @param login    a login
     * @param password a passoword
     * @return a User if the login and password combination is found, null if no matches found
     */
    private synchronized User getUserByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Checks if a specific login is already taken
     *
     * @param login a login to find
     * @return true is the login is found, false otherwise
     */
    private boolean isLoginExists(String login) {
        for (User user : users) {
            if (user.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a specific username is already taken
     *
     * @param username a username to find
     * @return true is the username is found, false otherwise
     */
    private boolean isUsernameExists(String username) {
        for (User user : users) {
            if (user.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

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
    @Override
    public synchronized boolean authenticate(ClientHandler clientHandler, String login, String password) {
        User user = getUserByLoginAndPassword(login, password);
        if (user == null) {
            clientHandler.sendMessage("AUTH: Incorrect login/password");
            return false;
        }
        clientHandler.sendMessage("AUTH: Authentication successful");
        if (!clientHandler.login(new UserProfile(user.username, user.role))) {
            return false;
        }
        return true;
    }

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
    @Override
    public synchronized boolean register(ClientHandler clientHandler, String login, String password, String username) {
        if (login.length() < 3 || password.length() < 6 || username.length() < 3) {
            clientHandler.sendMessage("AUTH: Login must be 3+ symbols, password 6+ symbols, username 3+ symbols");
            return false;
        }
        if (isLoginExists(login)) {
            clientHandler.sendMessage("AUTH: Login is already taken");
            return false;
        }
        if (isUsernameExists(username)) {
            clientHandler.sendMessage("AUTH: Username is already taken");
            return false;
        }
        users.add(new User(login, password, username, UserRole.USER));
        clientHandler.sendMessage("AUTH: Registration successful");
        if (!clientHandler.login(new UserProfile(username, UserRole.USER))) {
            return false;
        }
        return true;
    }

}
