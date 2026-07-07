package com.santiago.base.core.exceptions;

public class BusinessException extends BaseException {
    public BusinessException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
