package ru.practicum.ewm.base.exceptions;

public class DuplicatedDataException extends RuntimeException {
    public DuplicatedDataException(String message) {
        super(message);
    }

    public DuplicatedDataException(String message, Throwable cause) {
        super(message, cause);
    }
}