package com.pdg.adventure.view.vocabulary;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class SpecialWordsViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private SpecialWordsView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new SpecialWordsView(adventureService, accessService);
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
    void beforeEnter_validAdventureId_resolvesAndPopulatesWithoutForwarding() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        VocabularyData vocabularyData = new VocabularyData();
        Word take = vocabularyData.createWord("grab", Word.Type.VERB);
        adventure.setVocabularyData(vocabularyData);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithAdventureId("adv-1");

        view.beforeEnter(event);

        verify(accessService).findAdventureById(eq("adv-1"), any(UserData.class));
        verify(event, never()).forwardTo(any(Class.class));
        // ComboBox.getValue() is unreliable under BrowserlessTest (DataKeyMapper not
        // initialised — see reference-adventurebuilder-browserless-testing memory), but the
        // in-memory backing item list assigned via populate()/setItems() is a real, reliably
        // readable signal that the resolved adventure's vocabulary — not an empty/wrong one —
        // reached the selectors.
        VocabularyPickerField takeSelector = find(VocabularyPickerField.class, view).atIndex(1);
        assertThat(takeSelector.getListDataView().getItems()).containsExactly(take);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
