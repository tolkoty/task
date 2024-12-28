package com.test.task.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SomeResponse {
    private Long remoteId;
    private String data;
    private int status;
    private String message;
}
