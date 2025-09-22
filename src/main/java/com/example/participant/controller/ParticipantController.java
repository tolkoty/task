package com.example.participant.controller;

import com.example.participant.domain.ParticipantAggregate;
import com.example.participant.dto.PageResponse;
import com.example.participant.filter.FilterRequest;
import com.example.participant.service.ParticipantService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/participants", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParticipantController {
    private final ParticipantService service;

    public ParticipantController(ParticipantService service) {
        this.service = service;
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ParticipantAggregate> search(@Validated @RequestBody FilterRequest request) {
        return service.search(request);
    }
}

