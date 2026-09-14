package com.pdg.adventure.view.workflow;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
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
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class WorkflowEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private WorkflowEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new WorkflowEditorView(adventureService, accessService);
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

    private static AdventureData adventureWithOneWorkflowCommand() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setLocationData(new HashMap<>());
        WorkflowData workflowData = new WorkflowData();
        workflowData.getCommands().add(new CommandData(new CommandDescriptionData("shiver||")));
        adventure.setWorkflowData(workflowData);
        return adventure;
    }

    @SuppressWarnings("unchecked")
    private Grid<CommandData> grid(WorkflowEditorView view) {
        return (Grid<CommandData>) (Grid<?>) find(Grid.class, view).single();
    }

    @Test
    void beforeEnter_validAdventureId_populatesGridFromWorkflowData() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(view.getPageTitle()).isEqualTo("Workflow for The Demo");
        assertThat(test(grid(view)).size()).isEqualTo(1);
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

    @Test
    void beforeEnter_gridShowsCommandsInAlphabeticalVerbOrder_regardlessOfInsertionOrder() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        adventure.getWorkflowData().getCommands().addFirst(new CommandData(new CommandDescriptionData("zebra||")));
        adventure.getWorkflowData().getCommands().addFirst(new CommandData(new CommandDescriptionData("apple||")));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        var gridTester = test(grid(view));
        assertThat(List.of(gridTester.getRow(0), gridTester.getRow(1), gridTester.getRow(2)))
                .extracting(cmd -> cmd.getCommandDescription().getSafeVerb().getText())
                .containsExactly("apple", "shiver", "zebra");
    }

    @Test
    void newAndBackButtons_arePresent() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(find(Button.class, view).withText("New Command").single()).isNotNull();
        assertThat(find(Button.class, view).withText("Back").single()).isNotNull();
    }

    // Edit/Delete go through a GridContextMenu item click, which has no reliable way to be
    // driven from a browserless test - these tests instead build the delete confirmation dialog
    // directly (buildDeleteConfirmDialog is package-private for exactly this) and drive it as a
    // user would: open it, then confirm or cancel. Mirrors AdventuresMenuViewTest's pattern.
    @Test
    void deleteConfirmDialog_showsTheCommandsDescriptionAndDoesNotDeleteUntilConfirmed() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        CommandData existing = adventure.getWorkflowData().getCommands().getFirst();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(existing);
        UI.getCurrent().add(dialog);
        dialog.open();

        assertThat(test(dialog).getHeader()).isEqualTo("Delete Command");
        assertThat(test(dialog).getText()).contains("shiver");
        assertThat(adventure.getWorkflowData().getCommands()).contains(existing);
    }

    @Test
    void confirmingDeleteDialog_removesTheCommandAndPersistsAdventure() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        CommandData existing = adventure.getWorkflowData().getCommands().getFirst();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(existing);
        UI.getCurrent().add(dialog);
        dialog.open();

        test(dialog).confirm();

        assertThat(adventure.getWorkflowData().getCommands()).isEmpty();
        verify(adventureService).saveAdventureData(adventure);
    }

    @Test
    void cancelingDeleteDialog_deletesNothing() {
        AdventureData adventure = adventureWithOneWorkflowCommand();
        CommandData existing = adventure.getWorkflowData().getCommands().getFirst();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(existing);
        UI.getCurrent().add(dialog);
        dialog.open();

        test(dialog).cancel();

        assertThat(adventure.getWorkflowData().getCommands()).contains(existing);
        verify(adventureService, never()).saveAdventureData(any());
    }
}
