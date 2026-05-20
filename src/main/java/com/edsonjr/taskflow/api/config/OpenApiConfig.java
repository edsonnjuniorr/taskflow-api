package com.edsonjr.taskflow.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String DEVELOPMENT_VERSION = "development";

    @Bean
    public OpenAPI taskflowOpenAPI(ObjectProvider<BuildProperties> buildPropertiesProvider) {
        return new OpenAPI()
                .info(new Info()
                        .title("Taskflow API")
                        .version(resolveVersion(buildPropertiesProvider))
                        .description("REST API for internal task and subtask management."));
    }

    private String resolveVersion(ObjectProvider<BuildProperties> buildPropertiesProvider) {
        BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();

        if (buildProperties == null || buildProperties.getVersion() == null) {
            return DEVELOPMENT_VERSION;
        }

        return buildProperties.getVersion();
    }
}
