package com.thachlp.cdc.consumer.data;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TableMetaDataAccess {

    @PersistenceContext
    private final EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<String> getPrimaryKeysFrom(String db, String table) {
        if (entityManager == null) {
            throw new NullPointerException("Entity manager is null");
        }

        try (Session session = entityManager.unwrap(Session.class)) {
            if (session == null) {
                throw new NullPointerException("Session is null");
            }
            final String sql = String.format(
                    "SELECT COLUMN_NAME as columnName " +
                            "FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = '%s' " +
                            "  AND TABLE_NAME = '%s' " +
                            "  AND COLUMN_KEY = 'PRI'",
                    db, table
            );
            return entityManager.createNativeQuery(sql).getResultList();
        }
    }
}
