package com.thachlp.cdc.consumer.pojo;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public class CDCObject {

    private CDCSchema schema;
    private CDCPayload payload;

    @Data
    public static class CDCSchema {
        private String type;
        private List<CDCSchemaField> fields;

        public Optional<CDCSchemaField> getFieldByFieldName(String name) {
            if (fields == null) {
                return Optional.empty();
            }
            return fields.stream().filter(f -> f.getField().equalsIgnoreCase(name)).findFirst();
        }
    }

    @Data
    public static class CDCSchemaField {
        private String type;
        private String name;
        private String field;
        private List<CDCSchemaFieldStruct> fields;

        public Map<String, String> getMapType() {
            if (fields == null) {
                return new HashMap<>();
            }

            return fields.stream()
                    .collect(Collectors.toMap(CDCSchemaFieldStruct::getField,
                            s -> {
                                final String fieldName = s.getName();
                                if (fieldName != null) {
                                    if (fieldName.contains("MicroTimestamp")) {
                                        return "MicroTimestamp";
                                    } else if (fieldName.contains("ZonedTimestamp")) {
                                        return "ZonedTimestamp";
                                    } else if (fieldName.contains("Timestamp")) {
                                        return "Timestamp";
                                    }
                                }
                                return s.getType();
                            }
                    ));
        }
    }

    @Data
    public static class CDCSchemaFieldStruct {
        private String type;
        private boolean optional;
        private String field;
        private String name;
    }

    @Data
    public static class CDCPayload {
        private CDCPayloadSource source;
        private LinkedHashMap<String, Object> before;
        private LinkedHashMap<String, Object> after;
        private String op;
    }

    @Data
    public static class CDCPayloadSource {
        private String db;
        private String table;
        private String query;
        private String snapshot;
    }
}
