package com.pdg.adventure.views.adventure;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.support.ViewSupporter;
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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@PageTitle("Your World Of Adventures")
@Route(value = "adventures", layout = AdventuresMainLayout.class)
@RouteAlias(value = "", layout = AdventuresMainLayout.class)
public class AdventuresMenuView extends VerticalLayout {

    private transient final AdventureService adventureService;
    private String targetAdventureId;

    private Button runAdventure;

    @Autowired
    public AdventuresMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;

        Button create = new Button("Create Adventure", e -> UI.getCurrent().navigate(AdventureEditorView.class,
                                                 new RouteParameters("adventureId", "new")));
        //<theme-editor-local-classname>
        create.addClassName("adventures-menu-view-button-1");

        runAdventure = new Button("Run Adventure");
        runAdventure.setEnabled(false);
//        runAdventure.addClickListener(event -> UI.getCurrent().navigate(RunAdventureView.class));

        VerticalLayout leftSide = new VerticalLayout(create, runAdventure);

        List<AdventureData> adventures = adventureService.getAdventures();
        Div gridContainer = getGridContainer(adventures);
        VerticalLayout rightSide = new VerticalLayout(gridContainer);

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);
//        setMargin(true);
//        setPadding(true);

        add(jumpRow);
    }


    private Div getGridContainer(List<AdventureData> locations) {
        Grid<AdventureData> grid = new Grid<>(AdventureData.class, false);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(AdventureData::getTitle).setHeader("Title").setSortable(true).setAutoWidth(true);
        grid.addSelectionListener(selection -> {
            Optional<AdventureData> optionalAdventure = selection.getFirstSelectedItem();
            if (optionalAdventure.isPresent()) {
                targetAdventureId = optionalAdventure.get().getId();
                // TODO:
                //  enable this again
//                runAdventure.setEnabled(true);
            } else {
//                runAdventure.setEnabled(false);
            }
        });
        grid.addItemDoubleClickListener(e -> {
            targetAdventureId = e.getItem().getId();
            navigateToAdventureEditor(targetAdventureId);
        });

        grid.setItems(locations);
        grid.setWidth("500px");
        grid.setHeight("500px");
        AdventuresMenuView.AdventureDataContextMenu contextMenu = new AdventuresMenuView.AdventureDataContextMenu(grid);

        Div gridContainer = new Div(grid);
        gridContainer.setWidth("100%");
        gridContainer.setHeight("100%");
        return gridContainer;
    }

    private void navigateToAdventureEditor(String aTargetAdventureId) {
        UI.getCurrent().navigate(AdventureEditorView.class,
                                 new RouteParameters("adventureId", aTargetAdventureId)
        );

    }


    private class AdventureDataContextMenu extends GridContextMenu<AdventureData> {
        public AdventureDataContextMenu(Grid<AdventureData> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(adventure -> {
                targetAdventureId = adventure.getId();
                navigateToAdventureEditor(targetAdventureId);
            }));

            add(new Hr());

            GridMenuItem<AdventureData> adventureDetailItem =
                    addItem("AdventureId", e -> e.getItem().ifPresent(adventure -> {
                        adventure.getId();
                    }));

            setDynamicContentHandler(adventure -> {
                // Do not show context menu when header is clicked
                if (adventure == null) {
                    return false;
                }
                adventureDetailItem.scrollIntoView();
                adventureDetailItem.setText(adventure.getNotes());
                return true;
            });

            add(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(adventure -> {
                ListDataProvider<AdventureData> dataProvider =
                        (ListDataProvider<AdventureData>) target.getDataProvider();
                dataProvider.getItems().remove(adventure);
                dataProvider.refreshAll();
                adventureService.deleteAdventure(adventure.getId());
            }));
        }
    }
}
