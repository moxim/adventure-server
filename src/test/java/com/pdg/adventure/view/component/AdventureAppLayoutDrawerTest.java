package com.pdg.adventure.view.component;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.sidenav.SideNavItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;

/**
 * Regression test for the drawer having no entry back to a player-only account's home screen
 * (UX audit 2026-09-14, Finding 8): a user with only ROLE_PLAYER used to fall through both the
 * isAdmin() and isAuthor() branches in AdventureAppLayout.createMyDrawer(), leaving About and
 * Logout as the only drawer links.
 */
class AdventureAppLayoutDrawerTest extends BrowserlessTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdventuresMainLayout createLayoutAsUser(Set<Role> roles) {
        UserData user = new UserData();
        user.setUsername("test-user");
        user.setRoles(roles);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        AdventuresMainLayout layout = new AdventuresMainLayout();
        UI.getCurrent().add(layout);
        return layout;
    }

    @Test
    void playerOnlyUser_seesLibraryLinkInDrawer() {
        AdventuresMainLayout layout = createLayoutAsUser(Set.of(Role.PLAYER));

        List<SideNavItem> items = find(SideNavItem.class, layout).all();

        assertThat(items).extracting(SideNavItem::getLabel).contains("Library");
        assertThat(items).extracting(SideNavItem::getLabel).doesNotContain("Dashboard");
        SideNavItem library = items.stream().filter(i -> "Library".equals(i.getLabel())).findFirst().orElseThrow();
        assertThat(library.getPath()).isEqualTo("player/library");
    }

    @Test
    void authorUser_seesDashboardLink_notLibrary() {
        AdventuresMainLayout layout = createLayoutAsUser(Set.of(Role.AUTHOR));

        List<SideNavItem> items = find(SideNavItem.class, layout).all();

        assertThat(items).extracting(SideNavItem::getLabel).contains("Dashboard");
        assertThat(items).extracting(SideNavItem::getLabel).doesNotContain("Library");
    }

    @Test
    void userWithNoRecognizedRole_seesNeitherDashboardNorLibrary() {
        AdventuresMainLayout layout = createLayoutAsUser(Set.of());

        List<SideNavItem> items = find(SideNavItem.class, layout).all();

        assertThat(items).extracting(SideNavItem::getLabel).containsExactly("About", "Logout");
    }
}
