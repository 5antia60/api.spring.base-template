package com.santiago.base.modules.auth.refresh.service;

import com.santiago.base.core.exceptions.BaseException;

public class RefreshTokenReuseException extends BaseException {
    public RefreshTokenReuseException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
