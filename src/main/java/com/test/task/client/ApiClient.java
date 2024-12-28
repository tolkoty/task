package com.test.task.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiClient {

    @Value("${some.url}")
    private String baseUrl;
    @Value("${some.token}")
    private String token;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SomeResponse save(SomeRequest request) {
        val url = "";
        return makeRequest(url, HttpMethod.POST, request, SomeResponse.class);
    }

    public SomeResponse getById(Long id) {
        val url = "/" + id;
        return makeRequest(url, HttpMethod.GET, null, SomeResponse.class);
    }

    private <T> T makeRequest(String localUri, HttpMethod method, Object request, final Class<T> clazz) {
        val url = getBaseUrl() + localUri;
        log.info("Request: {} '{}'", method, url);
        try {
            return restTemplate.exchange(url, method, new HttpEntity<>(request, buildHeaders()), clazz).getBody();
        } catch (ResourceAccessException ex) {
            log.error("Cannot get access to url " + url, ex);
            throw ex;
        }
    }

    private HttpHeaders buildHeaders() {
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("token", token);
        return headers;
    }

    private String getBaseUrl() {
        return baseUrl;
    }
}
