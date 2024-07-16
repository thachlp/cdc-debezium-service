package com.thachlp.cdc.consumer.querybuilder;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class QueryBuilderUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String toQueryValue(String type, String value) {
        if (value == null || "null".equals(value.toLowerCase().trim())) {
            return "null";
        }

       if (type.startsWith("int")) {
           return escapeSQL(value);
       }

       if (type.startsWith("Timestamp")) {
           final LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), ZoneId.of("GMT"));
           return String.format("'%s'", dateTime.format(formatter));
       }

       if (type.startsWith("boolean")) {
           return Boolean.TRUE.equals(Boolean.valueOf(value)) ? "1" : "0";
       }

       return String.format("'%s'", escapeSQL(value));
    }

    public static String escapeSQL(String value) {
        if (value == null) {
            return "";
        }
        return StringUtils.replace(value, "'", "''");
    }
}
