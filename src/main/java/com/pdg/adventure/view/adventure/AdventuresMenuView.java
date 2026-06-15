package com.pdg.adventure.view.adventure;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
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

        VerticalLayout leftSide = new VerticalLayout(create, runAdventure);

        List<AdventureData> adventures = accessService.getAdventuresForUser(ViewSupporter.getCurrentUser());
        Div gridContainer = getGridContainer(adventures);
        VerticalLayout rightSide = new VerticalLayout(gridContainer);
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

            addItem("Delete", e -> e.getItem().ifPresent(adventure -> {
                ListDataProvider<AdventureData> dataProvider =
                        (ListDataProvider<AdventureData>) target.getDataProvider();
                dataProvider.getItems().remove(adventure);
                dataProvider.refreshAll();
                accessService.deleteAdventure(adventure.getId(), ViewSupporter.getCurrentUser());
            }));
        }
    }
}
