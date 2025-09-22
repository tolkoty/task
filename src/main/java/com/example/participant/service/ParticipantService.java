package com.example.participant.service;

import com.example.participant.cache.LookupCaches;
import com.example.participant.domain.ParticipantAggregate;
import com.example.participant.dto.PageResponse;
import com.example.participant.filter.Filter;
import com.example.participant.filter.FilterRequest;
import com.example.participant.repository.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParticipantService {
    private final ParticipantRepository repository;
    private final LookupCaches caches;

    public ParticipantService(ParticipantRepository repository, LookupCaches caches) {
        this.repository = repository;
        this.caches = caches;
    }

    public PageResponse<ParticipantAggregate> search(FilterRequest request) {
        List<Filter> filters = request.getFilters();
        int page = request.getPage();
        int size = request.getSize();
        int offset = page * size;

        long total = repository.countParticipantIdsByFilters(filters);
        List<Long> ids = repository.findParticipantIdsByFilters(filters, offset, size);
        List<ParticipantAggregate> participants = repository.loadParticipantsByIds(ids);

        // enrich from caches (gender, source, documents)
        for (ParticipantAggregate p : participants) {
            Map<String, Object> base = p.getParticipant();
            if (base != null) {
                Long genderId = asLong(base.get("gender_id"));
                Long sourceId = asLong(base.get("source_id"));
                p.setGender(caches.getGender(genderId));
                p.setSource(caches.getSource(sourceId));
            }

            // assuming profile has document ids array field 'document_ids'
            Map<String, Object> profile = p.getProfile();
            if (profile != null) {
                Object docIdsObj = profile.get("document_ids");
                List<Long> docIds = toLongList(docIdsObj);
                p.setDocuments(caches.getDocuments(docIds));
            }
        }

        return new PageResponse<>(participants, total, page, size);
    }

    private Long asLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(String.valueOf(value)); } catch (Exception e) { return null; }
    }

    private List<Long> toLongList(Object obj) {
        List<Long> res = new ArrayList<>();
        if (obj == null) return res;
        if (obj instanceof Iterable) {
            for (Object o : (Iterable<?>) obj) {
                Long v = asLong(o);
                if (v != null) res.add(v);
            }
        }
        return res;
    }
}

