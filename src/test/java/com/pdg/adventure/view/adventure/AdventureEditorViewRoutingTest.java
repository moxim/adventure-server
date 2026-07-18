package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.RouteIds;

class AdventureEditorViewRoutingTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private AdventureEditorView view;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new AdventureEditorView(accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    @Test
    void beforeEnter_validAdventureId_setsTitleFromAdventureTitle() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setLocationData(new HashMap<>());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(view.getPageTitle()).isEqualTo("Edit Adventure: The Demo");
    }

    @Test
    void beforeEnter_noAdventureId_setsNewAdventureTitle() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters());

        view.beforeEnter(event);

        assertThat(view.getPageTitle()).isEqualTo("A new adventure awaits!");
    }
}
