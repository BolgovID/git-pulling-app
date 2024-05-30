package org.programming.task.gitpullingapp.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.programming.task.gitpullingapp.exception.ErrorMessage;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(GitUserNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleGitUserNotFoundException(GitUserNotFoundException ex) {
        log.debug(ex.getMessage());
        var errorMessage = new ErrorMessage(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(
                errorMessage,
                HttpStatus.NOT_FOUND);
    }
}
