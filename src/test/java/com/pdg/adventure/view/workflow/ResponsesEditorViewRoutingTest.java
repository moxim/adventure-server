package com.pdg.adventure.view.workflow;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
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
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class ResponsesEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private ResponsesEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new ResponsesEditorView(adventureService, accessService);
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

    @SuppressWarnings("unchecked")
    private Grid<CommandData> grid(ResponsesEditorView view) {
        return (Grid<CommandData>) (Grid<?>) find(Grid.class, view).single();
    }

    @Test
    void beforeEnter_validAdventureId_populatesGridFromInterceptorCommands() {
        AdventureData adventure = adventureWithOneResponse();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(view.getPageTitle()).isEqualTo("Responses for The Demo");
        assertThat(test(grid(view)).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_doesNotTouchPreCommands() {
        AdventureData adventure = adventureWithOneResponse();
        adventure.getWorkflowData().getCommands().add(new CommandData(new CommandDescriptionData("wait||")));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(test(grid(view)).size()).isEqualTo(1);
        assertThat(adventure.getWorkflowData().getCommands()).hasSize(1);
    }

    @Test
    void newAndBackButtons_arePresent() {
        AdventureData adventure = adventureWithOneResponse();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(find(Button.class, view).withText("Create Response Process").single()).isNotNull();
        assertThat(find(Button.class, view).withText("Back").single()).isNotNull();
    }

    @Test
    void deleteConfirmDialog_showsTheResponsesDescriptionAndDoesNotDeleteUntilConfirmed() {
        AdventureData adventure = adventureWithOneResponse();
        CommandData existing = adventure.getWorkflowData().getInterceptorCommands().getFirst();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(existing);
        UI.getCurrent().add(dialog);
        dialog.open();

        assertThat(test(dialog).getHeader()).isEqualTo("Delete Response Process");
        assertThat(test(dialog).getText()).contains("shiver");
        assertThat(adventure.getWorkflowData().getInterceptorCommands()).contains(existing);
    }

    @Test
    void confirmingDeleteDialog_removesTheResponseAndPersistsAdventure() {
        AdventureData adventure = adventureWithOneResponse();
        CommandData existing = adventure.getWorkflowData().getInterceptorCommands().getFirst();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(existing);
        UI.getCurrent().add(dialog);
        dialog.open();

        test(dialog).confirm();

        assertThat(adventure.getWorkflowData().getInterceptorCommands()).isEmpty();
        verify(adventureService).saveAdventureData(adventure);
    }

    @Test
    void cancelingDeleteDialog_deletesNothing() {
        AdventureData adventure = adventureWithOneResponse();
        CommandData existing = adventure.getWorkflowData().getInterceptorCommands().getFirst();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(existing);
        UI.getCurrent().add(dialog);
        dialog.open();

        test(dialog).cancel();

        assertThat(adventure.getWorkflowData().getInterceptorCommands()).contains(existing);
        verify(adventureService, never()).saveAdventureData(any());
    }
}
