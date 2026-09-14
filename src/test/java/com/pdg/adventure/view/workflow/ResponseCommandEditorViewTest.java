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
import static org.mockito.Mockito.never;
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

class ResponseCommandEditorViewTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private ResponseCommandEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new ResponseCommandEditorView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static AdventureData adventureWithOneResponse() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setLocationData(new HashMap<>());
        WorkflowData workflowData = new WorkflowData();
        workflowData.getInterceptorCommands().add(new CommandData(new CommandDescriptionData("shiver||")));
        adventure.setWorkflowData(workflowData);
        return adventure;
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_existingCommandId_loadsItForEditing() {
        AdventureData adventure = adventureWithOneResponse();
        CommandData existing = adventure.getWorkflowData().getInterceptorCommands().getFirst();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.COMMAND_ID.getValue(), existing.getId())));

        assertThat(view.getPageTitle()).isEqualTo("Edit Response: shiver");
    }

    @Test
    void verbSelector_isRequired_forAResponse() {
        assertThat(view.getVerbSelector().isRequired()).isTrue();
    }

    @Test
    void persistCommand_newResponseWithNoVerb_isRejected() {
        // Unlike Process/Arrival, a Response is matched by its trigger - a verb is mandatory.
        AdventureData adventure = adventureWithOneResponse();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));

        boolean saved = view.persistCommand();

        assertThat(saved).isFalse();
        assertThat(adventure.getWorkflowData().getInterceptorCommands()).hasSize(1);
        verify(adventureService, never()).saveAdventureData(any());
    }

    @Test
    void persistCommand_newResponseWithAVerb_isAccepted() {
        AdventureData adventure = adventureWithOneResponse();
        VocabularyData vocabulary = new VocabularyData();
        vocabulary.createWord("wait", Word.Type.VERB);
        adventure.setVocabularyData(vocabulary);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        Word wait = view.getVerbSelector().getListDataView().getItems()
                .filter(w -> "wait".equals(w.getText())).findFirst().orElseThrow();
        view.getVerbSelector().setValue(wait);

        boolean saved = view.persistCommand();

        assertThat(saved).isTrue();
        assertThat(adventure.getWorkflowData().getInterceptorCommands()).hasSize(2);
        verify(adventureService).saveAdventureData(adventure);
    }
}
