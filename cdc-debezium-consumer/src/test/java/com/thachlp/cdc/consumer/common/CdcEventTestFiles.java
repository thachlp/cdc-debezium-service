package com.thachlp.cdc.consumer.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;

public final class CdcEventTestFiles {

    public static final Path INSERT = Path.of("src/test/resources/cdc-event/insert.json");
    public static final Path UPDATE = Path.of("src/test/resources/cdc-event/update.json");
    public static final Path DELETE = Path.of("src/test/resources/cdc-event/delete.json");
    public static final Path DDL_CREATE_PRODUCT = Path.of("src/test/resources/cdc-event/ddl-create-product.json");
    public static final Path DDL_DROP_PRODUCT = Path.of("src/test/resources/cdc-event/ddl-drop-product.json");
    public static final String TABLE_NAME = "category";
    public static final List<String> PRIMARY_KEYS = List.of("id");

    private CdcEventTestFiles() {
    }

    public static ObjectMapper newObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}



