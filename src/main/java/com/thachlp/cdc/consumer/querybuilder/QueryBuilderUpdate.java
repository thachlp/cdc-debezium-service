package com.thachlp.cdc.consumer.querybuilder;

import com.thachlp.cdc.consumer.pojo.CDCObject;
import com.thachlp.cdc.consumer.exception.QueryBuilderFieldNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class QueryBuilderUpdate implements QueryBuildable {

    @Override
    public String build(String tableName, List<String> primaryKeys, CDCObject jsonObject) {
        final Map<String, String> type = jsonObject.getSchema()
                                                   .getFieldByFieldName("after")
                                                   .map(CDCObject.CDCSchemaField::getMapType)
                                                   .orElseThrow(QueryBuilderFieldNotFoundException::new);
        final StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(tableName);
        sb.append(" SET ");

        final Set<String> keys = jsonObject.getPayload().getBefore().keySet();
        for (String key : keys) {
            if (primaryKeys.contains(key)) {
                continue;
            }
            sb.append(key);
            sb.append('=');
            sb.append(QueryBuilderUtils.toQueryValue(type.get(key),
                    Optional.ofNullable(jsonObject.getPayload()
                                    .getAfter()
                                    .get(key))
                            .map(Object::toString)
                            .orElse(null)));
            sb.append(',');
        }
        // Delete last ',' character
        sb.deleteCharAt(sb.length() - 1);
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
