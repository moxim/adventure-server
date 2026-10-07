package com.pdg.adventure.support;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Import into a plain Spring test context so beans scoped to "vaadin-session" can be created and switched. */
@Configuration
public class FakeSessionScopeConfig {

    @Bean
    public static FakeSessionScope fakeSessionScope() {
        return new FakeSessionScope();
    }

    @Bean
    public static CustomScopeConfigurer fakeSessionScopeRegistration(FakeSessionScope aScope) {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope(FakeSessionScope.NAME, aScope);
        return configurer;
    }
}
