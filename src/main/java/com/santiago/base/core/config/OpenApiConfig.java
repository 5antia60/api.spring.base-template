package com.santiago.base.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Base Template API")
                        .version("1.0.0")
                        .description("Spring Boot API with JWT Authentication"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer acceptLanguageHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            StringSchema localeSchema = new StringSchema()
                    .addEnumItem("pt-BR")
                    .addEnumItem("en-US");
            localeSchema.setDefault("en-US");

            Parameter acceptLanguage = new Parameter()
                    .in("header")
                    .name("Accept-Language")
                    .description("Idioma das mensagens de resposta (i18n). Suportados: pt-BR, en-US.")
                    .required(false)
                    .schema(localeSchema);
            operation.addParametersItem(acceptLanguage);
            return operation;
        };
    }
}