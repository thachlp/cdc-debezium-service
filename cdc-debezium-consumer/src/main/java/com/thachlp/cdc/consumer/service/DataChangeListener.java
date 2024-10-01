package com.thachlp.cdc.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thachlp.cdc.consumer.data.TableMetaDataAccess;
import com.thachlp.cdc.consumer.pojo.CDCObject;
import com.thachlp.cdc.consumer.pojo.DMLType;
import com.thachlp.cdc.consumer.querybuilder.QueryBuildable;
import com.thachlp.cdc.consumer.querybuilder.QueryBuilderDelete;
import com.thachlp.cdc.consumer.querybuilder.QueryBuilderInsert;
import com.thachlp.cdc.consumer.querybuilder.QueryBuilderUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataChangeListener {

    private final ObjectMapper objectMapper;
    private static final Map<String, List<String>> mapPrivateKey = new HashMap<>();
    private final TableMetaDataAccess tableMetaDataAccess;
    private static final EnumMap<DMLType, QueryBuildable> actionBuilder = new EnumMap<>(DMLType.class);

    static {
        actionBuilder.put(DMLType.CREATE, new QueryBuilderInsert());
        actionBuilder.put(DMLType.UPDATE, new QueryBuilderUpdate());
        actionBuilder.put(DMLType.DELETE, new QueryBuilderDelete());
    }

    @KafkaListener(topicPattern = "#{@topicPattern}", groupId = "coffee_shop")
    public void listen(@Payload ConsumerRecord<String, String> event) {
        final CDCObject jsonNode;
        try {
            if (event.value() == null) {
                log.info("Received a tombstone event with key {}", event.key());
                return;
            }

            jsonNode = objectMapper.readValue(event.value(), CDCObject.class);
            final String database = jsonNode.getPayload().getSource().getDb();
            if (getActionType(jsonNode.getPayload()) == DMLType.READ) {
                return;
            }

            if (!"false".equals(jsonNode.getPayload().getSource().getSnapshot())) {
                if ("last".equals(jsonNode.getPayload().getSource().getSnapshot())) {
                    log.info("Snapshot finished");
                }
                return;
            }

            final String table = jsonNode.getPayload().getSource().getTable();
            if (!mapPrivateKey.containsKey(table)) {
                mapPrivateKey.put(table, tableMetaDataAccess.getPrimaryKeysFrom(database, table));
            }

            final String query = actionBuilder.get(getActionType(jsonNode.getPayload()))
                    .build(table, mapPrivateKey.get(table), jsonNode);
            log.info("The captured query: {}",  query);
        } catch (Exception e) {
            log.error("Error processing message: {}, error {}", event.value(), e.getMessage(), e);
        }
    }

    private static DMLType getActionType(CDCObject.CDCPayload payload) {
        return switch (payload.getOp()) {
            case "c" -> DMLType.CREATE;
            case "d" -> DMLType.DELETE;
            case "u" -> DMLType.UPDATE;
            default -> DMLType.READ;
        };
    }
}
