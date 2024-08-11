package com.thachlp.cdc.consumer.querybuilder;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class QueryBuilderUtils {

    private static final DateTimeFormatter DATETIME_FORMATTER_MICROSECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private static final DateTimeFormatter DATETIME_FORMATTER_SECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE_ID_UTC = ZoneId.of("UTC");


    public static String toQueryValue(String type, String value) {
        if (value == null || "null".equals(value.toLowerCase().trim())) {
            return "null";
        }

        if (type.startsWith("int")) {
            return escapeSQL(value);
        }

        if ("Timestamp".equals(type)) {
            final long microTimestamp = Long.parseLong(value);
            return convertTimestamp(microTimestamp);
        }

        if ("MicroTimestamp".equals(type)) {
            final long microTimestamp = Long.parseLong(value);
            return convertMicroseconds(microTimestamp);
        }

        if ("ZonedTimestamp".equals(type)) {
            return convertZonedDateTime(value);
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

    private String convertMicroseconds(long microseconds) {
        final Instant instant = Instant.ofEpochSecond(
                microseconds / 1_000_000, // seconds
                (microseconds % 1_000_000) * 1_000  // nanoseconds
        );

        final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZONE_ID_UTC);
        return String.format("'%s'", dateTime.format(DATETIME_FORMATTER_MICROSECOND));
    }

    private String convertZonedDateTime(String zonedTimestamp) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse(zonedTimestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        final ZonedDateTime targetZonedDateTime = zonedDateTime.withZoneSameInstant(ZONE_ID_UTC);
        final LocalDateTime localDateTime = targetZonedDateTime.toLocalDateTime();
        return String.format("'%s'", localDateTime.format(DATETIME_FORMATTER_SECOND));
    }

    private String convertTimestamp(long timestamp) {
        final Instant instant = Instant.ofEpochMilli(timestamp);
        final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZONE_ID_UTC);
        return String.format("'%s'", dateTime.format(DATETIME_FORMATTER_SECOND));
    }
}
