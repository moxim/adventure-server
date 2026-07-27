package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;

class AdventuresMenuViewTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private AdventureData adventure;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);

        adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        when(accessService.getAdventuresForUser(any(UserData.class))).thenReturn(List.of(adventure));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unchecked")
    @Test
    void runAdventureButton_disabledUntilARowIsSelected() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        Button runButton = find(Button.class, view).withText("Run Adventure").single();
        assertThat(runButton.isEnabled()).isFalse();

        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();
        test(grid).select(0);

        assertThat(runButton.isEnabled()).isTrue();
    }
}
