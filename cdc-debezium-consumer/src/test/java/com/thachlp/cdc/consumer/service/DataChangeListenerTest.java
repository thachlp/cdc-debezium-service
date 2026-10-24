package com.thachlp.cdc.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thachlp.cdc.consumer.common.CdcEventTestFiles;
import com.thachlp.cdc.consumer.data.TableMetaDataAccess;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataChangeListenerTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Mock
    private TableMetaDataAccess tableMetaDataAccess;

    private DataChangeListener dataChangeListener;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = CdcEventTestFiles.newObjectMapper();
        dataChangeListener = new DataChangeListener(entityManager, objectMapper, tableMetaDataAccess);
    }

    @Test
    void listen_ShouldExecuteDdlQuery_WhenSchemaChangeEventHasNoOp() throws IOException {
        final String eventValue = Files.readString(CdcEventTestFiles.DDL_CREATE_PRODUCT);
        final String expectedDdl = objectMapper.readTree(eventValue).path("payload").path("ddl").asText();
        final ConsumerRecord<String, String> event = new ConsumerRecord<>("mysql", 0, 0L, "ddl-key", eventValue);

        when(entityManager.createNativeQuery(expectedDdl)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        assertDoesNotThrow(() -> dataChangeListener.listen(event));

        verify(entityManager).createNativeQuery(expectedDdl);
        verify(query).executeUpdate();
        verifyNoInteractions(tableMetaDataAccess);
    }

    @Test
    void listen_ShouldExecuteDropDdlQuery_WhenSchemaChangeEventHasNoOp() throws IOException {
        final String eventValue = Files.readString(CdcEventTestFiles.DDL_DROP_PRODUCT);
        final String expectedDdl = objectMapper.readTree(eventValue).path("payload").path("ddl").asText();
        final ConsumerRecord<String, String> event = new ConsumerRecord<>("mysql", 0, 0L, "ddl-key", eventValue);

        when(entityManager.createNativeQuery(expectedDdl)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        assertDoesNotThrow(() -> dataChangeListener.listen(event));

        verify(entityManager).createNativeQuery(expectedDdl);
        verify(query).executeUpdate();
        verifyNoInteractions(tableMetaDataAccess);
    }
}
