package com.pdg.adventure.view.player;

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

class PlayerLibraryViewTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private AdventureData adventure1;
    private AdventureData adventure2;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);

        adventure1 = new AdventureData();
        adventure1.setId("adv-1");
        adventure1.setTitle("The Demo");

        adventure2 = new AdventureData();
        adventure2.setId("adv-2");
        adventure2.setTitle("A Second Adventure");

        UserData testUser = new UserData();
        testUser.setUsername("test-player");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        when(accessService.getAdventuresForUser(any(UserData.class))).thenReturn(List.of(adventure1, adventure2));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private PlayerLibraryView createView() {
        PlayerLibraryView view = new PlayerLibraryView(accessService);
        UI.getCurrent().add(view);
        return view;
    }

    @SuppressWarnings("unchecked")
    private Grid<AdventureData> theGrid(PlayerLibraryView view) {
        return (Grid<AdventureData>) find(Grid.class, view).single();
    }

    @Test
    void showsOnlyTheAdventuresAssignedToTheCurrentUser() {
        PlayerLibraryView view = createView();

        assertThat(test(theGrid(view)).size()).isEqualTo(2);
    }

    @Test
    void runAdventureButton_disabledUntilAnAdventureIsSelected() {
        PlayerLibraryView view = createView();
        Button runButton = find(Button.class, view).withText("Run Adventure").single();
        assertThat(runButton.isEnabled()).isFalse();

        test(theGrid(view)).select(0);

        assertThat(runButton.isEnabled()).isTrue();
    }
}
