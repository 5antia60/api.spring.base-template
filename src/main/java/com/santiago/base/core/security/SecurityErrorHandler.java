package com.santiago.base.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santiago.base.core.exceptions.ErrorResponse;
import com.santiago.base.core.i18n.MessageResolverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler {

    private final MessageResolverService messageResolver;
    private final ObjectMapper objectMapper;
    private final LocaleResolver localeResolver;

    public void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) {
        write(request, response, HttpStatus.UNAUTHORIZED, "error.notAuthenticated");
    }

    public void writeForbidden(HttpServletRequest request, HttpServletResponse response) {
        write(request, response, HttpStatus.FORBIDDEN, "error.accessDenied");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String key) {
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageResolver.resolve(key, locale);

        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = new ErrorResponse(
                status.value(),
                message,
                Instant.now(),
                null
        );

        try {
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (IOException e) {
            log.error("Failed to write security error response", e);
        }
    }
}
