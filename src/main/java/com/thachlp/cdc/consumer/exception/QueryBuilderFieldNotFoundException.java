package com.thachlp.cdc.consumer.exception;

import java.io.Serial;

public class QueryBuilderFieldNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6971286249213619517L;

    public QueryBuilderFieldNotFoundException() {
    }

    public QueryBuilderFieldNotFoundException(String message) {
        super(message);
    }

    public QueryBuilderFieldNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryBuilderFieldNotFoundException(Throwable cause) {
        super(cause);
    }

    public QueryBuilderFieldNotFoundException(String message, Throwable cause, boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
