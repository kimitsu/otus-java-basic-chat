package ru.otus.java.basic.chat.server;

import java.util.Arrays;
import java.util.List;

public enum UserRole {
    ADMIN(), USER(Arrays.asList("/kick"));
    private final List<String> forbiddenCommands;

    UserRole() {
        this.forbiddenCommands = null;
    }

    UserRole(List<String> forbiddenCommands) {
        this.forbiddenCommands = forbiddenCommands;
    }

    /**
     * Checks if a command is allowed for the user role
     *
     * @param command a command to check
     * @return true if command is allowed, false otherwise
     */
    public boolean isCommandAllowed(String command) {
        return forbiddenCommands == null || forbiddenCommands.stream().noneMatch(aCommand -> aCommand.equals(command));
    }
}
