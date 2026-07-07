package com.santiago.base.core.exceptions;

public abstract class BaseException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    protected BaseException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
