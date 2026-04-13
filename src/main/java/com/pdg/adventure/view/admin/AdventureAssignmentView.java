package com.pdg.adventure.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.security.service.UserService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "admin/adventures/assignments", layout = AdventuresMainLayout.class)
@PageTitle("Adventure Assignments")
@RolesAllowed("ROLE_ADMIN")
public class AdventureAssignmentView extends VerticalLayout {

    private final transient AdventureAccessService accessService;
    private final transient UserService userService;

    private final Grid<AdventureData> adventureGrid = new Grid<>(AdventureData.class, false);
    private final VerticalLayout detailPanel = new VerticalLayout();

    private transient AdventureData selectedAdventure;
    private transient Map<String, String> authorNames;

    public AdventureAssignmentView(AdventureAccessService accessService, UserService userService) {
        this.accessService = accessService;
        this.userService = userService;

        setSizeFull();
        setPadding(true);

        add(new H2("Adventure Assignments"));

        SplitLayout splitLayout = new SplitLayout(buildAdventurePanel(), buildDetailPanel());
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(40);

        add(splitLayout);
        setFlexGrow(1, splitLayout);

        refreshAdventureGrid();
    }

    // -------------------------------------------------------------------------
    // Left panel — adventure grid
    // -------------------------------------------------------------------------

    private VerticalLayout buildAdventurePanel() {
        adventureGrid.addColumn(AdventureData::getTitle)
                     .setHeader("Title").setSortable(true).setAutoWidth(true);
        adventureGrid.addColumn(a -> authorNames.getOrDefault(a.getId(), "—"))
                     .setHeader("Author").setAutoWidth(true);

        adventureGrid.asSingleSelect().addValueChangeListener(e -> {
            selectedAdventure = e.getValue();
            refreshDetailPanel();
        });

        ViewSupporter.setSize(adventureGrid);
        adventureGrid.setEmptyStateText("No adventures found.");

        VerticalLayout panel = new VerticalLayout(adventureGrid);
        panel.setSizeFull();
        panel.setPadding(false);
        return panel;
    }

    private void refreshAdventureGrid() {
        authorNames = accessService.getAuthorNamesByAdventureId();
        adventureGrid.setItems(accessService.getAdventuresForUser(ViewSupporter.getCurrentUser()));
    }

    // -------------------------------------------------------------------------
    // Right panel — assignment detail
    // -------------------------------------------------------------------------

    private VerticalLayout buildDetailPanel() {
        detailPanel.setSizeFull();
        detailPanel.setPadding(true);
        showEmptyDetail();
        return detailPanel;
    }

    private void showEmptyDetail() {
        detailPanel.removeAll();
        detailPanel.add(new Span("Select an adventure to manage its assignments."));
    }

    private void refreshDetailPanel() {
        detailPanel.removeAll();

        if (selectedAdventure == null) {
            showEmptyDetail();
            return;
        }

        detailPanel.add(new H3(selectedAdventure.getTitle()));
        detailPanel.add(buildAuthorSection());
        detailPanel.add(buildPlayersSection());
    }

    // -------------------------------------------------------------------------
    // Author section
    // -------------------------------------------------------------------------

    private VerticalLayout buildAuthorSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.add(new H3("Author"));

        Optional<UserData> currentAuthorOpt =
                accessService.findAuthorForAdventure(selectedAdventure.getId());

        // Current author row with Remove button (only when an author is assigned)
        currentAuthorOpt.ifPresent(author -> {
            Button removeBtn = new Button("Remove");
            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            removeBtn.addClickListener(_ -> confirmRemoveAuthor(author));
            section.add(new HorizontalLayout(new Span("Assigned: " + author.getUsername()), removeBtn));
        });

        Button actionBtn = new Button(currentAuthorOpt.isPresent() ? "Reassign Author" : "Assign Author");
        actionBtn.setEnabled(false);

        // Assign / Reassign row — always visible
        ComboBox<UserData> authorCb = new ComboBox<>(
                currentAuthorOpt.isPresent() ? "Reassign to" : "Select author");
        authorCb.setItems(userService.findByRole(Role.AUTHOR));
        authorCb.setItemLabelGenerator(UserData::getUsername);
        authorCb.setClearButtonVisible(true);
        authorCb.addValueChangeListener(e -> {
            UserData selectedAuthor = e.getValue();
            // Disable action button if no author is selected or if the same author is selected
            boolean disableAction = selectedAuthor == null || currentAuthorOpt.map(a -> a.getId().equals(selectedAuthor.getId())).orElse(false);
            actionBtn.setEnabled(!disableAction);
        });

        actionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionBtn.addClickListener(_ -> {
            UserData selectedAuthor = authorCb.getValue();
            if (currentAuthorOpt.isPresent()) {
                confirmReassignAuthor(currentAuthorOpt.get(), selectedAuthor);
            } else {
                doAssignAuthor(selectedAuthor);
            }
        });

        section.add(new VerticalLayout(authorCb, actionBtn));
        return section;
    }

    private void confirmRemoveAuthor(UserData author) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Remove Author");
        dialog.setText("Remove " + author.getUsername() + " as author of \""
                + selectedAdventure.getTitle() + "\"?");
        dialog.setConfirmText("Remove");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.addConfirmListener(_ -> doRemoveAuthor());
        dialog.open();
    }

    private void confirmReassignAuthor(UserData current, UserData selected) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Reassign Author");
        dialog.setText("Replace " + current.getUsername() + " with " + selected.getUsername()
                + " as author of \"" + selectedAdventure.getTitle() + "\"?");
        dialog.setConfirmText("Reassign");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.addConfirmListener(_ -> doReassignAuthor(selected));
        dialog.open();
    }

    private void doAssignAuthor(UserData author) {
        try {
            accessService.assignAuthor(selectedAdventure.getId(), author);
            authorNames.put(selectedAdventure.getId(), author.getUsername());
            adventureGrid.getDataProvider().refreshAll();
            refreshDetailPanel();
            Notification.show("Author assigned.");
        } catch (IllegalStateException | AccessDeniedException e) {
            Notification.show("Could not assign author: " + e.getMessage());
        }
    }

    private void doReassignAuthor(UserData newAuthor) {
        accessService.reassignAuthor(selectedAdventure.getId(), newAuthor);
        authorNames.put(selectedAdventure.getId(), newAuthor.getUsername());
        adventureGrid.getDataProvider().refreshAll();
        refreshDetailPanel();
        Notification.show("Author reassigned.");
    }

    private void doRemoveAuthor() {
        accessService.removeAuthor(selectedAdventure.getId());
        authorNames.remove(selectedAdventure.getId());
        adventureGrid.getDataProvider().refreshAll();
        refreshDetailPanel();
        Notification.show("Author removed.");
    }

    // -------------------------------------------------------------------------
    // Players section
    // -------------------------------------------------------------------------

    private VerticalLayout buildPlayersSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.add(new H3("Players"));

        Grid<UserData> playersGrid = new Grid<>(UserData.class, false);
        playersGrid.addColumn(UserData::getUsername).setHeader("Username").setAutoWidth(true);
        playersGrid.addComponentColumn(player -> {
            Button remove = new Button("Remove");
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            remove.addClickListener(_ -> doRemovePlayer(player));
            return remove;
        }).setAutoWidth(true).setFlexGrow(0);

        playersGrid.setItems(accessService.findPlayersForAdventure(selectedAdventure.getId()));
        playersGrid.setMaxHeight("250px");
        playersGrid.setWidthFull();

        Button addBtn = new Button("Add Player");
        addBtn.setEnabled(false);

        ComboBox<UserData> playerCb = new ComboBox<>("Add player");
        playerCb.setItems(userService.findByRole(Role.PLAYER));
        playerCb.setItemLabelGenerator(UserData::getUsername);
        playerCb.setClearButtonVisible(true);
        playerCb.addValueChangeListener(e -> {
            UserData selectedPlayer = e.getValue();
            addBtn.setEnabled(selectedPlayer != null);
        });

        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(_ -> {
            UserData selectedPlayer = playerCb.getValue();
            doAddPlayer(selectedPlayer);
        });

        section.add(playersGrid, new VerticalLayout(playerCb, addBtn));
        return section;
    }

    private void doAddPlayer(UserData player) {
        accessService.assignPlayer(selectedAdventure.getId(), player);
        refreshDetailPanel();
        Notification.show("Player assigned.");
    }

    private void doRemovePlayer(UserData player) {
        accessService.removePlayer(selectedAdventure.getId(), player);
        refreshDetailPanel();
        Notification.show("Player removed.");
    }
}
