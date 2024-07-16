package com.thachlp.cdc.consumer.querybuilder;

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
class QueryBuilderUpdateTest extends QueryBuilderTest {
    @InjectMocks
    private QueryBuilderUpdate queryBuilderUpdate;

    @Override
    @BeforeEach
    public void setup() {
        super.setup();
        queryBuilderUpdate = new QueryBuilderUpdate();
    }

    @Test
    void testBuild_UpdateQuery() throws IOException {
        final File jsonFile = new File(FILE_UPDATE);
        final CDCObject cdcObject = objectMapper.readValue(jsonFile, CDCObject.class);
        final String query = queryBuilderUpdate.build(TABLE_NAME, PRIMARY_KEYS, cdcObject);
        final String expectedQuery = "UPDATE category SET name='Ice-Blended',created_at='2024-07-12 04:14:39',updated_at='2024-07-12 04:14:39',old_name='Tea' WHERE id=1;";

        assertEquals(expectedQuery, query);
    }
}
