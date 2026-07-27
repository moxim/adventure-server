package com.pdg.adventure.view.player;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventureRunView;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.support.ViewSupporter;

@PageTitle("Your Adventure Library")
@Route(value = "player/library", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_PLAYER")
public class PlayerLibraryView extends VerticalLayout {

    private final Button runAdventureButton;
    private String selectedAdventureId;

    public PlayerLibraryView(AdventureAccessService anAccessService) {
        setSizeFull();

        runAdventureButton = new Button("Run Adventure");
        runAdventureButton.setEnabled(false);
        runAdventureButton.addClickListener(_ -> navigateToRun(selectedAdventureId));

        VerticalLayout leftSide = new VerticalLayout(runAdventureButton);

        List<AdventureData> adventures = anAccessService.getAdventuresForUser(ViewSupporter.getCurrentUser());
        Div gridContainer = getGridContainer(adventures);
        VerticalLayout rightSide = new VerticalLayout(doubleClickRunHint(), gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout layout = new HorizontalLayout(leftSide, rightSide);
        layout.setSizeFull();

        add(layout);
    }

    private Div getGridContainer(List<AdventureData> adventures) {
        Grid<AdventureData> grid = new Grid<>(AdventureData.class, false);
        grid.addColumn(AdventureData::getTitle).setHeader("Title").setSortable(true).setAutoWidth(true);
        grid.addSelectionListener(selection -> {
            Optional<AdventureData> optionalAdventure = selection.getFirstSelectedItem();
            optionalAdventure.ifPresent(adventure -> selectedAdventureId = adventure.getId());
            runAdventureButton.setEnabled(optionalAdventure.isPresent());
        });
        grid.addItemDoubleClickListener(e -> navigateToRun(e.getItem().getId()));

        grid.setItems(adventures);
        ViewSupporter.setSize(grid);
        grid.setEmptyStateText("No adventures have been assigned to you yet.");

        Div gridContainer = new Div(grid);
        gridContainer.setSizeFull();
        return gridContainer;
    }

    private static Span doubleClickRunHint() {
        Span hint = new Span("Double-click a row to run it");
        hint.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        return hint;
    }

    private void navigateToRun(String anAdventureId) {
        UI.getCurrent().navigate(AdventureRunView.libraryRunPath(anAdventureId));
    }
}
