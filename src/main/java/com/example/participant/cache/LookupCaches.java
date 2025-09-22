package com.example.participant.cache;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LookupCaches {
    private final DSLContext dsl;

    private final Map<Long, Map<String, Object>> genderById = new HashMap<>();
    private final Map<Long, Map<String, Object>> sourceById = new HashMap<>();
    private final Map<Long, Map<String, Object>> documentById = new HashMap<>();

    public LookupCaches(DSLContext dsl) {
        this.dsl = dsl;
    }

    @PostConstruct
    public void preload() {
        // assuming immutable small tables; load once on startup
        loadCache("gender", genderById);
        loadCache("source", sourceById);
        loadCache("document", documentById);
    }

    private void loadCache(String table, Map<Long, Map<String, Object>> target) {
        Result<Record> rows = dsl.select().from(DSL.table(table)).fetch();
        target.clear();
        rows.forEach(r -> target.put(r.get("id", Long.class), r.intoMap()));
    }

    public Map<String, Object> getGender(Long id) {
        return id == null ? null : genderById.get(id);
    }

    public Map<String, Object> getSource(Long id) {
        return id == null ? null : sourceById.get(id);
    }

    public List<Map<String, Object>> getDocuments(List<Long> ids) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (ids == null) return res;
        for (Long id : ids) {
            Map<String, Object> doc = documentById.get(id);
            if (doc != null) res.add(doc);
        }
        return res;
    }
}

