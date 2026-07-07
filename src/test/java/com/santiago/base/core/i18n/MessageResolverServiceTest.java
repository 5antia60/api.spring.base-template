package com.santiago.base.core.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MessageResolverServiceTest {

    private MessageResolverService messageResolver;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        messageResolver = new MessageResolverService(source);
    }

    @Test
    void shouldResolveInEnglishByDefault() {
        withLocale(Locale.ENGLISH, () -> {
            String message = messageResolver.resolve("user.notFound", 42L);
            assertThat(message).contains("User not found").contains("42");
        });
    }

    @Test
    void shouldResolveInPortuguese() {
        withLocale(Locale.forLanguageTag("pt-BR"), () -> {
            String message = messageResolver.resolve("user.notFound", 42L);
            assertThat(message).contains("Usuário não encontrado").contains("42");
        });
    }

    @Test
    void shouldFallbackToKeyWhenMissing() {
        withLocale(Locale.ENGLISH, () -> {
            String message = messageResolver.resolve("nonexistent.key");
            assertThat(message).isEqualTo("nonexistent.key");
        });
    }

    @Test
    void shouldFallbackToDefaultKeyWhenMessageNotResolvable() {
        withLocale(Locale.ENGLISH, () -> {
            String message = messageResolver.resolveOrDefault("Spring internal message", "error.accessDenied");
            assertThat(message).contains("permission");
        });
    }

    @Test
    void shouldResolveOrDefaultWhenKeyExists() {
        withLocale(Locale.forLanguageTag("pt-BR"), () -> {
            String message = messageResolver.resolveOrDefault("user.accessDenied.viewOthers", "error.accessDenied");
            assertThat(message).contains("outros usuários");
        });
    }

    private void withLocale(Locale locale, Runnable action) {
        LocaleContextHolder.setLocale(locale);
        try {
            action.run();
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }
}
