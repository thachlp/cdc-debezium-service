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
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataChangeListener {

    @PersistenceContext
    private final EntityManager entityManager;
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
    @Transactional(noRollbackFor = Exception.class)
    public void listen(@Payload ConsumerRecord<String, String> event) {
        final CDCObject jsonNode;
        try {
            if (event.value() == null) {
                log.info("Received a tombstone event with key {}", event.key());
                return;
            }

            if (isDltTopic(event.topic())) {
                log.warn("Skip DLT topic in main listener: {}", event.topic());
                return;
            }
            jsonNode = objectMapper.readValue(event.value(), CDCObject.class);
            if (isSnapshot(jsonNode.getPayload().getSource().getSnapshot())) {
                log.info("Received a tombstone event with key {}", event.key());
                return;
            }

            if (jsonNode.getPayload() == null || jsonNode.getPayload().getSource() == null) {
                log.warn("Skip event because payload/source is missing, topic: {}", event.topic());
                return;
            }

            if (getActionType(jsonNode.getPayload()) == DMLType.READ) {
                return;
            }

            final String table = jsonNode.getPayload().getSource().getTable();
            final String query = actionBuilder.get(getActionType(jsonNode.getPayload()))
                    .build(table, mapPrivateKey.get(table), jsonNode);
            if (isDdlQuery(jsonNode)) {
                final int result = entityManager.createNativeQuery(query).executeUpdate();
                log.info("DDL query: {}, affectedRows: {}", query, result);
                return;
            }

            final String database = jsonNode.getPayload().getSource().getDb();


            if (!mapPrivateKey.containsKey(table)) {
                mapPrivateKey.put(table, tableMetaDataAccess.getPrimaryKeysFrom(database, table));
            }


            final int result = entityManager.createNativeQuery(query).executeUpdate();
            log.info("The captured query: {}, affectedRows: {}", query, result);
        } catch (Exception e) {
            log.error("Error processing message: {}, error {}", event.value(), e.getMessage(), e);
            throw new IllegalStateException("Failed to process CDC event", e);
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

    private static boolean isSnapshot(String snapshot) {
        if (!"false".equals(snapshot)) {
            if ("last".equals(snapshot)) {
                log.info("Snapshot finished");
            }
            return true;
        }
        return false;
    }

    private boolean isDdlQuery(CDCObject jsonNode) {
        return jsonNode.getPayload() != null && StringUtils.isNotEmpty(jsonNode.getPayload().getDdl());
    }

    private boolean isDltTopic(String topic) {
        return StringUtils.endsWith(topic, ".DLT") || StringUtils.contains(topic, ".DLT.");
    }
}
