package ru.otus.java.basic.chat.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum UserRole {
    ADMIN(), USER(new HashSet<>(Arrays.asList("/kick")));
    private final Set<String> forbiddenCommands;

    UserRole() {
        this.forbiddenCommands = null;
    }

    UserRole(Set<String> forbiddenCommands) {
        this.forbiddenCommands = forbiddenCommands;
    }

    /**
     * Checks if a command is allowed for the user role
     *
     * @param command a command to check
     * @return true if command is allowed, false otherwise
     */
    public boolean isCommandAllowed(String command) {
        return forbiddenCommands == null || !forbiddenCommands.contains(command);
    }
}
