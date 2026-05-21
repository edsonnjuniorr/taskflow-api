package com.edsonjr.taskflow.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void shouldUseBuildVersionWhenBuildPropertiesAreAvailable() {
        ObjectProvider<BuildProperties> buildPropertiesProvider = mockBuildPropertiesProvider();

        when(buildPropertiesProvider.getIfAvailable())
                .thenReturn(buildPropertiesWithVersion("1.2.3"));

        OpenAPI openAPI = openApiConfig.taskflowOpenAPI(buildPropertiesProvider);

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Taskflow API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.2.3");
        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("REST API for internal task and subtask management.");
    }

    @Test
    void shouldUseDevelopmentVersionWhenBuildPropertiesAreNotAvailable() {
        ObjectProvider<BuildProperties> buildPropertiesProvider = mockBuildPropertiesProvider();

        when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);

        OpenAPI openAPI = openApiConfig.taskflowOpenAPI(buildPropertiesProvider);

        assertThat(openAPI.getInfo().getVersion()).isEqualTo("development");
    }

    @Test
    void shouldUseDevelopmentVersionWhenBuildVersionIsNotAvailable() {
        ObjectProvider<BuildProperties> buildPropertiesProvider = mockBuildPropertiesProvider();

        when(buildPropertiesProvider.getIfAvailable())
                .thenReturn(new BuildProperties(new Properties()));

        OpenAPI openAPI = openApiConfig.taskflowOpenAPI(buildPropertiesProvider);

        assertThat(openAPI.getInfo().getVersion()).isEqualTo("development");
    }

    private BuildProperties buildPropertiesWithVersion(String version) {
        Properties properties = new Properties();
        properties.setProperty("version", version);

        return new BuildProperties(properties);
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<BuildProperties> mockBuildPropertiesProvider() {
        return mock(ObjectProvider.class);
    }
}
