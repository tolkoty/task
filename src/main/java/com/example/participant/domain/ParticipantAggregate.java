package com.example.participant.domain;

import java.util.List;
import java.util.Map;

public class ParticipantAggregate {
    private Long id;
    private Map<String, Object> participant; // base row fields
    private Map<String, Object> status;
    private Map<String, Object> contact;
    private Map<String, Object> profile;
    private Map<String, Object> organization;
    private List<Map<String, Object>> records;

    // immutable lookups hydrated from cache
    private Map<String, Object> gender;
    private Map<String, Object> source;
    private List<Map<String, Object>> documents;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Map<String, Object> getParticipant() { return participant; }
    public void setParticipant(Map<String, Object> participant) { this.participant = participant; }

    public Map<String, Object> getStatus() { return status; }
    public void setStatus(Map<String, Object> status) { this.status = status; }

    public Map<String, Object> getContact() { return contact; }
    public void setContact(Map<String, Object> contact) { this.contact = contact; }

    public Map<String, Object> getProfile() { return profile; }
    public void setProfile(Map<String, Object> profile) { this.profile = profile; }

    public Map<String, Object> getOrganization() { return organization; }
    public void setOrganization(Map<String, Object> organization) { this.organization = organization; }

    public List<Map<String, Object>> getRecords() { return records; }
    public void setRecords(List<Map<String, Object>> records) { this.records = records; }

    public Map<String, Object> getGender() { return gender; }
    public void setGender(Map<String, Object> gender) { this.gender = gender; }

    public Map<String, Object> getSource() { return source; }
    public void setSource(Map<String, Object> source) { this.source = source; }

    public List<Map<String, Object>> getDocuments() { return documents; }
    public void setDocuments(List<Map<String, Object>> documents) { this.documents = documents; }
}

