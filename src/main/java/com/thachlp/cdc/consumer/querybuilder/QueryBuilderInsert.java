package com.thachlp.cdc.consumer.querybuilder;

import com.thachlp.cdc.consumer.pojo.CDCObject;
import com.thachlp.cdc.consumer.exception.QueryBuilderFieldNotFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class QueryBuilderInsert implements QueryBuildable {

    protected Map<String, String> getType(CDCObject jsonObject) {
        return  jsonObject.getSchema()
                          .getFieldByFieldName("after")
                          .map(CDCObject.CDCSchemaField::getMapType)
                          .orElseThrow(QueryBuilderFieldNotFoundException::new);
    }

    protected LinkedHashMap<String, Object> getDataPayload(CDCObject jsonObject) {
        return jsonObject.getPayload().getAfter();
    }

    @Override
    public String build(String tableName, List<String> primaryKeys, CDCObject jsonObject) {
        final Map<String, String> type = getType(jsonObject);
        final StringBuilder stmSb = new StringBuilder();
        stmSb.append("INSERT INTO ");
        stmSb.append(tableName);
        stmSb.append('(');
        final Set<String> keys = getDataPayload(jsonObject).keySet();
        final StringBuilder valuePartSb = new StringBuilder();
        for (String key : keys) {
            stmSb.append(key);
            stmSb.append(',');
            final Object value = getDataPayload(jsonObject).get(key);
            valuePartSb.append(QueryBuilderUtils.toQueryValue(type.get(key),
                    Optional.ofNullable(value)
                            .map(Objects::toString)
                            .orElse("null")));
            valuePartSb.append(',');
        }
        // Delete last ',' character from stm part
        stmSb.deleteCharAt(stmSb.length() - 1);
        stmSb.append(") VALUES (");

        // Delete last ',' character from value part
        valuePartSb.deleteCharAt(valuePartSb.length() - 1);
        stmSb.append(valuePartSb);
        stmSb.append(");");
        return stmSb.toString();
    }
}
