package com.santiago.base.core.exceptions;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
