package com.test.task.rest.controller;

import com.test.task.client.SomeRequest;
import com.test.task.model.ResponseLog;
import com.test.task.service.ResponseLogService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/request")
@RequiredArgsConstructor
public class SomeController {

    private final ResponseLogService service;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseLog> getById(@PathVariable("id") Long remoteId) {
        val result = service.getById(remoteId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ResponseLog> sent(@RequestBody SomeRequest request) {
        val result = service.sent(request);
        return ResponseEntity.ok(result);
    }
}
