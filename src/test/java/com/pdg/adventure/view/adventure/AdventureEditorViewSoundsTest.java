package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.sound.SoundMenuView;
import com.pdg.adventure.view.support.RouteIds;

class AdventureEditorViewSoundsTest extends BrowserlessTest {

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

    private void enterAdventureWithSounds(int aNumberOfSounds) {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        for (int i = 0; i < aNumberOfSounds; i++) {
            SoundData sound = new SoundData();
            sound.setName("sound " + i);
            adventure.getSoundData().put(sound.getId(), sound);
        }
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        view.beforeEnter(event);
    }

    @Test
    void totalSounds_showsTheNumberOfSounds() {
        enterAdventureWithSounds(2);

        assertThat(find(TextField.class, view).withLabel("Total Sounds").single().getValue()).isEqualTo("2");
    }

    @Test
    void totalSounds_isReadOnly() {
        enterAdventureWithSounds(0);

        assertThat(find(TextField.class, view).withLabel("Total Sounds").single().isReadOnly()).isTrue();
    }

    @Test
    void soundsButton_navigatesToTheSoundMenu() {
        enterAdventureWithSounds(1);
        List<Class<?>> targets = new ArrayList<>();
        UI.getCurrent().addBeforeEnterListener(event -> targets.add(event.getNavigationTarget()));

        test(find(Button.class, view).withText("Sounds").single()).click();

        assertThat(targets).contains(SoundMenuView.class);
    }
}
