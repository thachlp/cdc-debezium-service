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
class QueryBuilderDeleteTest {
    @InjectMocks
    private QueryBuilderDelete queryBuilderDelete;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = CdcEventTestFiles.newObjectMapper();
        queryBuilderDelete = new QueryBuilderDelete();
    }

    @Test
    void testBuild_DeleteQuery() throws IOException {
        final File jsonFile = CdcEventTestFiles.DELETE.toFile();
        final CDCObject cdcObject = objectMapper.readValue(jsonFile, CDCObject.class);
        final String query = queryBuilderDelete.build(CdcEventTestFiles.TABLE_NAME, CdcEventTestFiles.PRIMARY_KEYS, cdcObject);
        final String expectedQuery = "DELETE FROM category WHERE id=1;";

        assertEquals(expectedQuery, query);
    }
}
