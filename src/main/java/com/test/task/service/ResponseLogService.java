package com.test.task.service;

import com.test.task.client.SomeRequest;
import com.test.task.model.ResponseLog;

public interface ResponseLogService {

    ResponseLog getById(Long id);

    ResponseLog sent(SomeRequest request);
}
