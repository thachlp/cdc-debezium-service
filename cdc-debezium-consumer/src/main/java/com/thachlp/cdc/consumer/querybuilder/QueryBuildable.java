package com.thachlp.cdc.consumer.querybuilder;

import com.thachlp.cdc.consumer.pojo.CDCObject;

import java.util.List;

@FunctionalInterface
public interface QueryBuildable {

    String build(String tableName, List<String> primaryKeys, CDCObject jsonNode);
}
