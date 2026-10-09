package com.pdg.adventure.view.component;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.page.ColorScheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;

/** The navbar's light/dark toggle switches the page's color scheme and flips its own icon and label. */
class AdventureAppLayoutColorSchemeTest extends BrowserlessTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Button toggle() {
        UserData user = new UserData();
        user.setUsername("test-user");
        user.setRoles(Set.of(Role.AUTHOR));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        AdventuresMainLayout layout = new AdventuresMainLayout();
        UI.getCurrent().add(layout);
        return find(Button.class, layout).withId("color-scheme-toggle").single();
    }

    @Test
    void clickingToggle_switchesBetweenDarkAndLight() {
        Button toggle = toggle();

        test(toggle).click();
        assertThat(UI.getCurrent().getPage().getColorScheme()).isEqualTo(ColorScheme.Value.DARK);
        assertThat(toggle.getAriaLabel()).hasValue("Switch to light mode");

        test(toggle).click();
        assertThat(UI.getCurrent().getPage().getColorScheme()).isEqualTo(ColorScheme.Value.LIGHT);
        assertThat(toggle.getAriaLabel()).hasValue("Switch to dark mode");
    }
}
