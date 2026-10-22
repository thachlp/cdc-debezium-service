package com.thachlp.cdc.consumer.querybuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thachlp.cdc.consumer.common.CdcEventTestFiles;
import com.thachlp.cdc.consumer.pojo.CDCObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class QueryBuilderInsertTest {
    @InjectMocks
    private QueryBuilderInsert queryBuilderInsert;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = CdcEventTestFiles.newObjectMapper();
        queryBuilderInsert = new QueryBuilderInsert();
    }

    @Test
    void testBuild_InsertQuery() throws IOException {
        final File jsonFile = CdcEventTestFiles.INSERT.toFile();
        final CDCObject cdcObject = objectMapper.readValue(jsonFile, CDCObject.class);
        final String query = queryBuilderInsert.build(CdcEventTestFiles.TABLE_NAME, CdcEventTestFiles.PRIMARY_KEYS, cdcObject);
        final String expectedQuery = "INSERT INTO category(id,name,created_at,updated_at,old_name) VALUES (1,'Juice','2024-07-12 04:14:38','2024-07-12 04:14:38',null);";

        assertEquals(expectedQuery, query);
    }
}
