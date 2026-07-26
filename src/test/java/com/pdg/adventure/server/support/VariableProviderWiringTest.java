package com.pdg.adventure.server.support;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.AdventureConfig;

/**
 * Guards against the {@link VariableProvider} bean being registered twice in the real
 * (full component-scan) Spring context - once via {@link AdventureConfig#allVariables()} and
 * once via component-scanning {@link VariableProvider} itself. A duplicate silently splits
 * writers (variable-mutating actions) from readers (variable-comparing conditions) across two
 * unrelated instances, so variables set at runtime are invisible to conditions checking them.
 */
class VariableProviderWiringTest {

    @Configuration
    @Import(AdventureConfig.class)
    @ComponentScan(basePackages = "com.pdg.adventure.server.support")
    static class ProbeConfig {
    }

    @Test
    void onlyOneVariableProviderBeanExists() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ProbeConfig.class)) {
            String[] names = context.getBeanNamesForType(VariableProvider.class);
            assertThat(names).hasSize(1);
        }
    }
}
