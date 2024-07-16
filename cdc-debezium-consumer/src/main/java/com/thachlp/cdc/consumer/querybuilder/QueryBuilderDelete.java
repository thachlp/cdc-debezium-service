package com.thachlp.cdc.consumer.querybuilder;

import com.thachlp.cdc.consumer.pojo.CDCObject;
import com.thachlp.cdc.consumer.exception.QueryBuilderFieldNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QueryBuilderDelete implements QueryBuildable {

    @Override
    public String build(String tableName, List<String> primaryKeys, CDCObject jsonObject) {
        final Map<String, String> type = jsonObject.getSchema()
                                                   .getFieldByFieldName("before")
                                                   .map(CDCObject.CDCSchemaField::getMapType)
                                                   .orElseThrow(QueryBuilderFieldNotFoundException::new);
        final StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(tableName);
        sb.append(" WHERE ");
        for (String pKey: primaryKeys) {
            sb.append(pKey);
            sb.append('=');
            sb.append(QueryBuilderUtils.toQueryValue(type.get(pKey),
                    Optional.ofNullable(jsonObject.getPayload()
                                    .getBefore()
                                    .get(pKey))
                                    .map(Object::toString)
                                    .orElse(null)));
            sb.append(" AND ");
        }
        // Delete last " AND " characters
        sb.delete(sb.length() - 5, sb.length());
        sb.append(';');
        return sb.toString();
    }
}
