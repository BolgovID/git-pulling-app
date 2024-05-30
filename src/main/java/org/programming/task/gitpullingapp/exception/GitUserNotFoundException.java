package org.programming.task.gitpullingapp.exception;

public class GitUserNotFoundException extends RuntimeException {
    public GitUserNotFoundException(String username) {
        super("User " + username + " not found");
    }
}
