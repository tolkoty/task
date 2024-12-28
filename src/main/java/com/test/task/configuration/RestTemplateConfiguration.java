package com.test.task.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfiguration {

    @Value("${rest.connection.timeout:50000}")
    private Integer restConnectionTimeOut;

    @Value("${rest.read.timeout:50000}")
    private Integer restReadTimeOut;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .connectTimeout(Duration.ofMillis(restConnectionTimeOut))
                .readTimeout(Duration.ofMillis(restReadTimeOut))
                .build();
    }
}
