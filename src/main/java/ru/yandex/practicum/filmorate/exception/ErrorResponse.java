package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String error;
    private final String description;
    private final LocalDateTime timestamp;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error) {
        this(error, null);
    }
}