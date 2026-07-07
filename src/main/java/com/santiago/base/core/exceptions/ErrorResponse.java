package com.santiago.base.core.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        Instant timestamp,
        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> errors
) {}
