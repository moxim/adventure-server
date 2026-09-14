package com.pdg.adventure.view.adventure;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@PageTitle("Your World Of Adventures")
@Route(value = "author/adventures", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class AdventuresMenuView extends VerticalLayout {

    private final transient AdventureAccessService accessService;

    private String targetAdventureId;
    private final Button runAdventure;

    public AdventuresMenuView(AdventureAccessService anAccessService) {

        setSizeFull();

        accessService = anAccessService;

        Button create = new Button("Create Adventure", _ -> UI.getCurrent().navigate(AdventureEditorView.class));
        create.addClassName("adventures-menu-view-button-1");

        runAdventure = new Button("Run Adventure");
        runAdventure.setEnabled(false);
        runAdventure.addClickListener(_ -> navigateToAdventureRun(targetAdventureId));

        VerticalLayout leftSide = new VerticalLayout(create, runAdventure);

        List<AdventureData> adventures = accessService.getAdventuresForUser(ViewSupporter.getCurrentUser());
        Div gridContainer = getGridContainer(adventures);
        VerticalLayout rightSide = new VerticalLayout(ViewSupporter.doubleClickEditHint(), gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);

        add(jumpRow);
    }

    private Div getGridContainer(List<AdventureData> adventures) {
        Grid<AdventureData> grid = new Grid<>(AdventureData.class, false);
        grid.addColumn(AdventureData::getTitle).setHeader("Title").setSortable(true).setAutoWidth(true);
        grid.addSelectionListener(selection -> {
            Optional<AdventureData> optionalAdventure = selection.getFirstSelectedItem();
            if (optionalAdventure.isPresent()) {
                targetAdventureId = optionalAdventure.get().getId();
            }
            runAdventure.setEnabled(optionalAdventure.isPresent());
        });
        grid.addItemDoubleClickListener(e -> {
            targetAdventureId = e.getItem().getId();
            navigateToAdventureEditor(targetAdventureId);
        });

        grid.setItems(adventures);

        ViewSupporter.setSize(grid);
        grid.setEmptyStateText("Create some adventures.");

        new AdventureDataContextMenu(grid);

        Div gridContainer = new Div(grid);
        gridContainer.setSizeFull();

        return gridContainer;
    }

    private void navigateToAdventureEditor(String aTargetAdventureId) {
        UI.getCurrent().navigate(AdventureEditorView.class,
                                 new RouteParameters(RouteIds.ADVENTURE_ID.getValue(), aTargetAdventureId));
    }

    private void navigateToAdventureRun(String aTargetAdventureId) {
        UI.getCurrent().navigate(AdventureRunView.menuRunPath(aTargetAdventureId));
    }

    private class AdventureDataContextMenu extends GridContextMenu<AdventureData> {
        public AdventureDataContextMenu(Grid<AdventureData> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(adventure -> {
                targetAdventureId = adventure.getId();
                navigateToAdventureEditor(targetAdventureId);
            }));

            addComponent(new Hr());

            GridMenuItem<AdventureData> adventureDetailItem =
                    addItem("AdventureId", e -> e.getItem().ifPresent(adventure -> adventure.getId()));

            setDynamicContentHandler(adventure -> {
                if (adventure == null) return false;
                adventureDetailItem.scrollIntoView();
                adventureDetailItem.setText(adventure.getNotes());
                return true;
            });

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(adventure ->
                    buildDeleteConfirmDialog(adventure, target).open()));
        }
    }

    /**
     * Package-private for testing: GridContextMenu item clicks have no reliable way to be
     * driven from a browserless test, so this builds the dialog (and wires its confirm
     * listener) in isolation from the context-menu click that triggers it.
     */
    @SuppressWarnings("unchecked")
    ConfirmDialog buildDeleteConfirmDialog(AdventureData adventure, Grid<AdventureData> grid) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Adventure");
        dialog.setText("Are you sure you want to delete '" + adventure.getTitle()
                + "'? This cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(_ -> {
            ListDataProvider<AdventureData> dataProvider =
                    (ListDataProvider<AdventureData>) grid.getDataProvider();
            dataProvider.getItems().remove(adventure);
            dataProvider.refreshAll();
            accessService.deleteAdventure(adventure.getId(), ViewSupporter.getCurrentUser());

            Notification notification = Notification.show("Adventure '" + adventure.getTitle()
                    + "' deleted successfully.", 2000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        return dialog;
    }
}
