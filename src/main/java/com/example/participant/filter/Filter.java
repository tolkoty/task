package com.example.participant.filter;

import java.util.List;

public class Filter {
    private String column; // qualified or alias-supported column name
    private FilterOperation operation;
    private List<String> values; // transport as strings; convert later based on column metadata

    public Filter() {}

    public Filter(String column, FilterOperation operation, List<String> values) {
        this.column = column;
        this.operation = operation;
        this.values = values;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public FilterOperation getOperation() {
        return operation;
    }

    public void setOperation(FilterOperation operation) {
        this.operation = operation;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}

