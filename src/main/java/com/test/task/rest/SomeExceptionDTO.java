package com.test.task.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SomeExceptionDTO {

    private String error;

    private String message;

    private String path;

    private int status;

    private String timestamp;


}
