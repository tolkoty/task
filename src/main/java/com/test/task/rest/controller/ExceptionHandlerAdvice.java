package com.test.task.rest.controller;

import com.test.task.rest.SomeException;
import com.test.task.rest.SomeExceptionDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
@Slf4j
@AllArgsConstructor
public class ExceptionHandlerAdvice {


    @ExceptionHandler(SomeException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public SomeExceptionDTO handleLongevityException(HttpServletRequest req, SomeException e) {
        log.error("handleLongevityException", e);
        String localizedMessage = e.getLocalizedMessage();
        return new SomeExceptionDTO(
                e.getMessage(),
                "Bad_request_title",
                req.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                new Date().toString()
        );
    }



}
