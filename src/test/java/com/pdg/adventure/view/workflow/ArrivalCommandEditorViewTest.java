package com.pdg.adventure.view.workflow;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class ArrivalCommandEditorViewTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private ArrivalCommandEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new ArrivalCommandEditorView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static AdventureData adventureWithOneArrivalProcess() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setLocationData(new HashMap<>());
        WorkflowData workflowData = new WorkflowData();
        workflowData.getArrivalProcesses().add(new CommandData(new CommandDescriptionData("welcome||")));
        adventure.setWorkflowData(workflowData);
        return adventure;
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_noCommandId_startsANewArrivalProcess() {
        AdventureData adventure = adventureWithOneArrivalProcess();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));

        assertThat(view.getPageTitle()).isEqualTo("New Arrival Process");
    }

    @Test
    void beforeEnter_existingCommandId_loadsItForEditing() {
        AdventureData adventure = adventureWithOneArrivalProcess();
        CommandData existing = adventure.getWorkflowData().getArrivalProcesses().getFirst();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.COMMAND_ID.getValue(), existing.getId())));

        assertThat(view.getPageTitle()).isEqualTo("Edit Arrival Process: welcome");
    }

    @Test
    void verbSelector_notRequired_forAnArrivalProcess() {
        assertThat(view.getVerbSelector().isRequired()).isFalse();
    }

    @Test
    void persistCommand_newArrivalProcess_addsItToWorkflowDataAndPersists() {
        // NB: verbSelector.isRequired() is false for Arrival (previous test), but a pre-existing
        // bug (present before this refactor too, carried forward as-is - see docs/ux-audit-
        // 2026-09-14.md) means a genuinely blank verb still fails binder validation, so this
        // still needs one selected. Not a regression from this change; tracked separately.
        AdventureData adventure = adventureWithOneArrivalProcess();
        VocabularyData vocabulary = new VocabularyData();
        vocabulary.createWord("arrive", Word.Type.VERB);
        adventure.setVocabularyData(vocabulary);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        Word arrive = view.getVerbSelector().getListDataView().getItems()
                .filter(w -> "arrive".equals(w.getText())).findFirst().orElseThrow();
        view.getVerbSelector().setValue(arrive);

        boolean saved = view.persistCommand();

        assertThat(saved).isTrue();
        assertThat(adventure.getWorkflowData().getArrivalProcesses()).hasSize(2);
        verify(adventureService).saveAdventureData(adventure);
    }

    @Test
    void persistCommand_doesNotTouchOtherWorkflowLists() {
        AdventureData adventure = adventureWithOneArrivalProcess();
        adventure.getWorkflowData().getCommands().add(new CommandData(new CommandDescriptionData("wait||")));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));

        view.persistCommand();

        assertThat(adventure.getWorkflowData().getCommands()).hasSize(1);
    }
}
