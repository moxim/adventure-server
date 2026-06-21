package com.pdg.adventure.view.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AdventureAppLayout}'s pure navbar-title resolution.
 * Guards against the single-header regression where a view without a page title
 * would otherwise leave the (now single) navbar header blank.
 */
class AdventureAppLayoutTest {

    @Test
    void navbarTitle_fallsBackToAppName_whenPageTitleBlank() {
        assertThat(AdventureAppLayout.navbarTitleOrDefault("")).isEqualTo("Adventure Builder");
        assertThat(AdventureAppLayout.navbarTitleOrDefault("   ")).isEqualTo("Adventure Builder");
        assertThat(AdventureAppLayout.navbarTitleOrDefault(null)).isEqualTo("Adventure Builder");
    }

    @Test
    void navbarTitle_usesPageTitle_whenPresent() {
        assertThat(AdventureAppLayout.navbarTitleOrDefault("Locations")).isEqualTo("Locations");
    }
}
