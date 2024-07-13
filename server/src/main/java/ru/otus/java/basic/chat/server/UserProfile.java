package ru.otus.java.basic.chat.server;

public class UserProfile {
    private String username;
    private UserRole role;

    /**
     * Creates a user profile
     *
     * @param username a username
   у  * @param role     a role
     */
    public UserProfile(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    /**
     * @return the user's name
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the user's role
     */
    public UserRole getRole() {
        return role;
    }
}
