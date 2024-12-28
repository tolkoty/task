package com.test.task.service;


import com.test.task.client.ApiClient;
import com.test.task.client.SomeRequest;
import com.test.task.client.SomeResponse;
import com.test.task.model.ResponseLog;
import com.test.task.repository.ResponseLogRepository;
import com.test.task.rest.SomeException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResponseLogServiceImpl implements ResponseLogService {

    private final ResponseLogRepository repository;

    private final ApiClient client;

    @Override
    public ResponseLog getById(Long id) {
        val response = client.getById(id);
        if (response == null) {
            throw new SomeException("Provider have not data with id = " + id);       }

        val responseLog = mapResponse(response);
        repository.save(responseLog);
        return responseLog;

    }

    @Override
    public ResponseLog sent(SomeRequest request) {
        val response = client.save(request);
        val responseLog = mapResponse(response);
        repository.save(responseLog);
        return responseLog;
    }

    private ResponseLog mapResponse(SomeResponse response) {
        if (response == null) {
            return null;
        }
        val responseLog = new ResponseLog();
        responseLog.setRemoteId(response.getRemoteId());
        responseLog.setData(response.getData());
        responseLog.setStatus(response.getStatus());
        responseLog.setMessage(responseLog.getMessage());
        return responseLog;
    }
}
