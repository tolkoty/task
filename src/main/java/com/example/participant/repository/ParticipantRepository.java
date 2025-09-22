package com.example.participant.repository;

import com.example.participant.domain.ParticipantAggregate;
import com.example.participant.filter.Filter;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ParticipantRepository {
    private final DSLContext dsl;

    public ParticipantRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    // Table names assumed; replace with generated jOOQ tables if available
    private final Table<?> T_PARTICIPANT = DSL.table("participant").as("participant");
    private final TableField<Record, Long> F_PARTICIPANT_ID = DSL.field("participant.id", Long.class);

    public List<Long> findParticipantIdsByFilters(List<Filter> filters, int offset, int limit) {
        Condition condition = buildCondition(filters);

        org.jooq.SelectJoinStep<?> from = dsl.selectDistinct(F_PARTICIPANT_ID).from(T_PARTICIPANT);
        from = applyJoinsForFilters(from, filters);

        return from
                .where(condition)
                .orderBy(F_PARTICIPANT_ID.asc())
                .offset(offset)
                .limit(limit)
                .fetch(F_PARTICIPANT_ID);
    }

    public long countParticipantIdsByFilters(List<Filter> filters) {
        Condition condition = buildCondition(filters);
        org.jooq.SelectJoinStep<?> from = dsl.select(DSL.countDistinct(F_PARTICIPANT_ID)).from(T_PARTICIPANT);
        from = applyJoinsForFilters(from, filters);
        return from.where(condition).fetchOne(0, long.class);
    }

    public List<ParticipantAggregate> loadParticipantsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        // base participants
        Result<Record> participantRows = dsl.select().from("participant").where(DSL.field("participant.id").in(ids)).fetch();

        // related references (1-1)
        Result<Record> statusRows = dsl.select().from("status").where(DSL.field("status.participant_id").in(ids)).fetch();
        Result<Record> contactRows = dsl.select().from("contact").where(DSL.field("contact.participant_id").in(ids)).fetch();
        Result<Record> profileRows = dsl.select().from("profile").where(DSL.field("profile.participant_id").in(ids)).fetch();
        Result<Record> orgRows = dsl.select().from("organization").where(DSL.field("organization.id").in(
                dsl.select(DSL.field("participant.organization_id")).from("participant").where(DSL.field("participant.id").in(ids))
        )).fetch();

        // records (1-many)
        Result<Record> recordRows = dsl.select().from("record").where(DSL.field("record.participant_id").in(ids)).fetch();

        Map<Long, ParticipantAggregate> idToAgg = new HashMap<>();
        participantRows.forEach(r -> {
            Long id = r.get("id", Long.class);
            ParticipantAggregate agg = new ParticipantAggregate();
            agg.setId(id);
            agg.setParticipant(r.intoMap());
            idToAgg.put(id, agg);
        });

        statusRows.forEach(r -> {
            Long pid = r.get("participant_id", Long.class);
            ParticipantAggregate agg = idToAgg.get(pid);
            if (agg != null) agg.setStatus(r.intoMap());
        });

        contactRows.forEach(r -> {
            Long pid = r.get("participant_id", Long.class);
            ParticipantAggregate agg = idToAgg.get(pid);
            if (agg != null) agg.setContact(r.intoMap());
        });

        profileRows.forEach(r -> {
            Long pid = r.get("participant_id", Long.class);
            ParticipantAggregate agg = idToAgg.get(pid);
            if (agg != null) agg.setProfile(r.intoMap());
        });

        // Map organization by id, then attach by participant.organization_id
        Map<Long, Map<String, Object>> orgById = orgRows.stream().collect(Collectors.toMap(
                r -> r.get("id", Long.class),
                Record::intoMap
        ));
        participantRows.forEach(r -> {
            Long pid = r.get("id", Long.class);
            Long orgId = r.get("organization_id", Long.class);
            ParticipantAggregate agg = idToAgg.get(pid);
            if (agg != null && orgId != null) agg.setOrganization(orgById.get(orgId));
        });

        Map<Long, List<Map<String, Object>>> pidToRecords = new HashMap<>();
        recordRows.forEach(r -> {
            Long pid = r.get("participant_id", Long.class);
            pidToRecords.computeIfAbsent(pid, k -> new ArrayList<>()).add(r.intoMap());
        });
        idToAgg.values().forEach(a -> a.setRecords(pidToRecords.getOrDefault(a.getId(), new ArrayList<>())));

        // sort as ids order
        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) order.put(ids.get(i), i);
        return idToAgg.values().stream()
                .sorted((a, b) -> Integer.compare(order.getOrDefault(a.getId(), Integer.MAX_VALUE), order.getOrDefault(b.getId(), Integer.MAX_VALUE)))
                .collect(Collectors.toList());
    }

    private Condition buildCondition(List<Filter> filters) {
        Condition condition = DSL.trueCondition();
        if (filters == null || filters.isEmpty()) return condition;

        for (Filter f : filters) {
            String column = f.getColumn();
            List<String> values = f.getValues();
            switch (f.getOperation()) {
                case EQUALS:
                    condition = condition.and(DSL.field(column).eq(values.get(0)));
                    break;
                case NOT_EQUALS:
                    condition = condition.and(DSL.field(column).ne(values.get(0)));
                    break;
                case IN:
                    condition = condition.and(DSL.field(column).in(values));
                    break;
                case NOT_IN:
                    condition = condition.and(DSL.field(column).notIn(values));
                    break;
                case LIKE:
                    condition = condition.and(DSL.field(column, String.class).like(values.get(0)));
                    break;
                case ILIKE:
                    condition = condition.and(DSL.field(column, String.class).likeIgnoreCase(values.get(0)));
                    break;
                case GT:
                    condition = condition.and(DSL.field(column).gt(values.get(0)));
                    break;
                case GTE:
                    condition = condition.and(DSL.field(column).ge(values.get(0)));
                    break;
                case LT:
                    condition = condition.and(DSL.field(column).lt(values.get(0)));
                    break;
                case LTE:
                    condition = condition.and(DSL.field(column).le(values.get(0)));
                    break;
                case BETWEEN:
                    condition = condition.and(DSL.field(column).between(values.get(0), values.get(1)));
                    break;
                case IS_NULL:
                    condition = condition.and(DSL.field(column).isNull());
                    break;
                case IS_NOT_NULL:
                    condition = condition.and(DSL.field(column).isNotNull());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operation: " + f.getOperation());
            }
        }
        return condition;
    }

    private org.jooq.SelectJoinStep<?> applyJoinsForFilters(org.jooq.SelectJoinStep<?> from,
                                                            List<Filter> filters) {
        if (filters == null || filters.isEmpty()) return from;

        Set<String> needed = new HashSet<>();
        for (Filter f : filters) {
            String column = f.getColumn();
            if (column == null) continue;
            int dot = column.indexOf('.');
            if (dot <= 0) continue;
            String prefix = column.substring(0, dot);
            if (!"participant".equals(prefix)) {
                needed.add(prefix);
            }
        }

        for (String key : needed) {
            switch (key) {
                case "status":
                    from = from.leftJoin(DSL.table("status").as("status"))
                            .on(DSL.field("status.participant_id").eq(DSL.field("participant.id")));
                    break;
                case "contact":
                    from = from.leftJoin(DSL.table("contact").as("contact"))
                            .on(DSL.field("contact.participant_id").eq(DSL.field("participant.id")));
                    break;
                case "profile":
                    from = from.leftJoin(DSL.table("profile").as("profile"))
                            .on(DSL.field("profile.participant_id").eq(DSL.field("participant.id")));
                    break;
                case "organization":
                    from = from.leftJoin(DSL.table("organization").as("organization"))
                            .on(DSL.field("organization.id").eq(DSL.field("participant.organization_id")));
                    break;
                case "record":
                    from = from.leftJoin(DSL.table("record").as("record"))
                            .on(DSL.field("record.participant_id").eq(DSL.field("participant.id")));
                    break;
                default:
                    // ignore unknown prefix to avoid SQL error
                    break;
            }
        }
        return from;
    }
}

