package org.programming.task.gitpullingapp.exception;

public class ApiRateLimitExceededException extends RuntimeException{
    public ApiRateLimitExceededException() {
        super("API rate limit exceeded");
    }
}
