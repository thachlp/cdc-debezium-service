package com.thachlp.cdc.consumer.testsupport;

import java.nio.file.Path;

public final class CdcEventTestFiles {

    public static final Path INSERT = Path.of("src/test/resources/cdc-event/insert.json");
    public static final Path UPDATE = Path.of("src/test/resources/cdc-event/update.json");
    public static final Path DELETE = Path.of("src/test/resources/cdc-event/delete.json");
    public static final Path DDL_CREATE_PRODUCT = Path.of("src/test/resources/cdc-event/ddl-create-product.json");

    private CdcEventTestFiles() {
    }
}

