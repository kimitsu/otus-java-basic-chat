package ru.otus.java.basic.chat.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import java.util.Random;

public class JDBCAuthenticationProvider implements AuthenticationProvider {
    private final Connection connection;

    /**
     * Creates an authentication provider based on PostgresSQL database.
     * Establishes PostgresSQL connection based on environment variables:
     * OTUS_CHAT_DB_ADDR=address:port/database_name
     * OTUS_CHAT_DB_USER=database_user_name
     * OTUS_CHAT_DB_PWD=database_user_password
     */
    public JDBCAuthenticationProvider() {
        String address = System.getenv("OTUS_CHAT_DB_ADDR");
        String user = System.getenv("OTUS_CHAT_DB_USER");
        String password = System.getenv("OTUS_CHAT_DB_PWD");
        if (address == null || user == null || password == null) {
            throw new RuntimeException("Environment variables (OTUS_CHAT_DB_ADDR, OTUS_CHAT_DB_USER, OTUS_CHAT_DB_PWD)" +
                    "are not set correctly");
        }
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://" + address, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates SHA-256 of two concatenated strings
     *
     * @param string     a string
     * @param saltString another string
     * @return Base64 encoding of SHA-256 of the concatenation of the two stings
     */
    private String getSaltedHash(String string, String saltString) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(
                            (saltString + string).getBytes()
                    )
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
        try {
            if (!isLoginPasswordMatch(login, password)) {
                clientHandler.sendMessage("AUTH: Incorrect login/password");
                return false;
            }
            clientHandler.sendMessage("AUTH: Authentication successful");
            UserProfile profile = getUserProfile(login);
            if (profile == null) {
                clientHandler.sendMessage("AUTH: Profile not found");
                return false;
            }
            if (!clientHandler.login(profile)) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            clientHandler.sendMessage("AUTH: Internal database error");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if login and password matches some database entry
     *
     * @param login    a login
     * @param password a password
     * @return true if match is found, false otherwise
     * @throws SQLException in case of database failure
     */
    private boolean isLoginPasswordMatch(String login, String password) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT password_salted_hash, password_salt FROM authentication.user WHERE login = ?"
        )) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String password_salted_hash = result.getString(1);
                    String password_salt = result.getString(2);
                    if (password_salted_hash.equals(getSaltedHash(password, password_salt))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates a UserProfile based on the database entry for the specified login
     *
     * @param login a login
     * @return new UserProfile
     * @throws SQLException in case of database failure or if login not found
     */
    private UserProfile getUserProfile(String login) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name, role FROM authentication.user WHERE login = ?"
        )) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String name = result.getString(1);
                    String role = result.getString(2);
                    UserRole userRole;
                    try {
                        userRole = UserRole.valueOf(role);
                    } catch (IllegalArgumentException e) {
                        throw new SQLException("Database entry for the user role is corrupted (login=" + login + ")");
                    }
                    return new UserProfile(name, userRole);
                }
            }
        }
        throw new SQLException("User profile not found");
    }

    /**
     * Checks if a login already exists in the database
     *
     * @param login a login to look up
     * @return true if already exits, false otherwise
     * @throws SQLException in case of database failure
     */
    private boolean isLoginExists(String login) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM authentication.user WHERE login = ?"
        )) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a username already exists in the database
     *
     * @param username a username to look up
     * @return true if already exits, false otherwise
     * @throws SQLException in case of database failure
     */
    private boolean isUsernameExists(String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM authentication.user WHERE name = ?"
        )) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds user to the database
     *
     * @param login    a login
     * @param password a password
     * @param username a username
     * @param role     a name of a role
     * @throws SQLException in case of database failure
     */
    private void addUser(String login, String password, String username, UserRole role) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO authentication.user (login, password_salted_hash, password_salt, name, role)"
                        + "VALUES (?, ?, ?, ?, ?)"
        )) {
            String passwordSalt = new Random()
                    .ints((int) 'A', (int) 'z')
                    .limit(16)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            statement.setString(1, login);
            statement.setString(2, getSaltedHash(password, passwordSalt));
            statement.setString(3, passwordSalt);
            statement.setString(4, username);
            statement.setString(5, role.name());
            statement.executeUpdate();
        }
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
    public boolean register(ClientHandler clientHandler, String login, String password, String username) {
        try {
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
            addUser(login, password, username, UserRole.USER);
            clientHandler.sendMessage("AUTH: Registration successful");
            if (!clientHandler.login(new UserProfile(username, UserRole.USER))) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            clientHandler.sendMessage("AUTH: Internal database error");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Closes JDBC connection
     *
     * @throws SQLException in case of database failure
     */
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
