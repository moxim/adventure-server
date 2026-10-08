package com.pdg.adventure.server.storage.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BuilderVersionTest {

    @Test
    void of_aVersion_isCurrent() {
        assertThat(BuilderVersion.of("1.2.3").current()).contains("1.2.3");
    }

    @Test
    void of_null_isUnknown() {
        assertThat(BuilderVersion.of(null).current()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void withBuildProperties_usesTheirVersion() {
        Properties properties = new Properties();
        properties.setProperty("version", "1.0.0-SNAPSHOT");
        ObjectProvider<BuildProperties> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(new BuildProperties(properties));

        assertThat(new BuilderVersion(provider).current()).contains("1.0.0-SNAPSHOT");
    }

    @Test
    @SuppressWarnings("unchecked")
    void withoutBuildProperties_isUnknown() {
        ObjectProvider<BuildProperties> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        assertThat(new BuilderVersion(provider).current()).isEmpty();
    }
}
