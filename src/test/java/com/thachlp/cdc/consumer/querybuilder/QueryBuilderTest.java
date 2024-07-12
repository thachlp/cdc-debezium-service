package com.thachlp.cdc.consumer.querybuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public abstract class QueryBuilderTest {
    protected ObjectMapper objectMapper;
    protected static final String TABLE_NAME = "category";
    protected static final String FILE_UPDATE = "src/test/resources/cdc-event/update.json";
    protected static final String FILE_INSERT = "src/test/resources/cdc-event/insert.json";
    protected static final String FILE_DELETE = "src/test/resources/cdc-event/delete.json";
    protected static final List<String> PRIMARY_KEYS = List.of("id");

    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
