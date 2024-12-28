package com.test.task.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TestRestTemplateBasicLiveTest {

    private static final String SOME_RESOURCE_URL = "http://localhost:" + 8081 + "/api";

    @Test
    public void givenTestRestTemplate_whenSendGetForEntity_thenStatusOk() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<SomeResponse> response = testRestTemplate.getForEntity(SOME_RESOURCE_URL + "/1", SomeResponse.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

}