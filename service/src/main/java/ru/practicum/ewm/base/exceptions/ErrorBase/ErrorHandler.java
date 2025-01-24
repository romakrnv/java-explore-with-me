package ru.practicum.ewm.base.exceptions.ErrorBase;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.base.exceptions.*;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final ConflictException e) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.toString(),
                "Integrity constraint violated",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final ConditionsNotMetException e) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.toString(),
                "The conditions for the called operation are not met",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final DuplicatedDataException e) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.toString(),
                "Duplicate key value",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.toString(),
                "Request object not found",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(final BadRequestException e) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                "The request is not written correctly.",
                e.getMessage());
    }


    // Для валидации через jakarta.validation.constraints
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        String field = Objects.requireNonNull(e.getFieldError()).getField();
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                "The request is not written correctly.",
                String.format("Field: %s. Invalid field value. Value: %s", field, e.getFieldValue(field)));
    }

    // Для валидации через jakarta.validation.constraints
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(final ConstraintViolationException e) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                "The request is not written correctly.",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                "Server error",
                e.getMessage());
    }
}

