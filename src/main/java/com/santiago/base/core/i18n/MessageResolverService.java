package com.santiago.base.core.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageResolverService {

    private final MessageSource messageSource;

    public String resolve(String key, Object... args) {
        return resolve(key, LocaleContextHolder.getLocale(), args);
    }

    public String resolve(String key, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            return key;
        }
    }

    public String resolveOrDefault(String key, String defaultKey, Object... args) {
        return resolveOrDefault(key, defaultKey, LocaleContextHolder.getLocale(), args);
    }

    public String resolveOrDefault(String key, String defaultKey, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            return resolve(defaultKey, locale, args);
        }
    }
}
