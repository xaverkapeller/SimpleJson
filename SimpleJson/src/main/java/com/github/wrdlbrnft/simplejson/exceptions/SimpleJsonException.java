package com.github.wrdlbrnft.simplejson.exceptions;

/**
 * Created by kapeller on 21/06/16.
 */

public class SimpleJsonException extends RuntimeException {

    public SimpleJsonException(String detailMessage) {
        super(detailMessage);
    }

    public SimpleJsonException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
