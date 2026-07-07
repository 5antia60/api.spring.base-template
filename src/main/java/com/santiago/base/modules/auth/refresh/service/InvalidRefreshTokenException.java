package com.santiago.base.modules.auth.refresh.service;

import com.santiago.base.core.exceptions.BaseException;

public class InvalidRefreshTokenException extends BaseException {
    public InvalidRefreshTokenException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
