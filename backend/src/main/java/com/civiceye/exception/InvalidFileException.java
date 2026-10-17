package com.civiceye.exception;

public class InvalidFileException extends RuntimeException {

    private final int statusCode;

    public InvalidFileException(String message) {
        super(message);
        this.statusCode = 400;
    }

    public InvalidFileException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
