package com.pdg.adventure.view.command;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pdg.adventure.view.support.RouteIds.ADVENTURE_ID;
import static com.pdg.adventure.view.support.RouteIds.LOCATION_ID;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.item.ItemEditorView;
import com.pdg.adventure.view.location.LocationEditorView;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations/:locationId/commands", layout = AdventuresMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/locations/:locationId/items/:itemId/commands", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class CommandsMenuView extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver {
    private final transient AdventureService adventureService;
    private final transient ItemService itemService;
    private final Binder<CommandProviderData> binder;
    private final Div gridContainer;
    private final Button saveButton;
    private final Button resetButton;
    private final Button backButton;
    private final Button createButton;
    private Grid<CommandData> grid;
    private String pageTitle;
    private LocationData locationData;
    private AdventureData adventureData;
    private ItemData itemData;
    private CommandProviderData commandProviderData;
    private GridListDataView<CommandData> gridListDataView;
    private transient PreconditionActionFormatter formatter;

    public CommandsMenuView(AdventureService anAdventureService, ItemService anItemService) {
        adventureService = anAdventureService;
        itemService = anItemService;
        binder = new BeanValidationBinder<>(CommandProviderData.class);

        setSizeFull();
        gridContainer = new Div();
        gridContainer.setSizeFull();

        createButton = new Button("Create", _ -> {
            if (itemData != null) {
                UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                          new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                          new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                          new RouteParam(RouteIds.ITEM_ID.getValue(), itemData.getId())))
                  .ifPresent(editor -> editor.setData(adventureData, locationData, itemData));
            } else {
                UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                          new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                          new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
                  .ifPresent(editor -> editor.setData(adventureData, locationData));
            }
        });

        saveButton = new Button("Save");
        saveButton.addClickListener(_ -> {
            if (itemData != null) {
                itemService.saveItem(itemData);
            } else {
                adventureService.saveLocationData(locationData);
            }
            saveButton.setEnabled(false);
        });

        backButton = new Button("Back", _ -> {
            if (itemData != null) {
                UI.getCurrent().navigate(ItemEditorView.class, new RouteParameters(
                        new RouteParam(ADVENTURE_ID.getValue(), adventureData.getId()),
                        new RouteParam(LOCATION_ID.getValue(), locationData.getId()),
                        new RouteParam(RouteIds.ITEM_ID.getValue(), itemData.getId())))
                  .ifPresent(e -> e.setData(adventureData, locationData));
            } else {
                UI.getCurrent().navigate(LocationEditorView.class, new RouteParameters(
                        new RouteParam(LOCATION_ID.getValue(), locationData.getId()),
                        new RouteParam(ADVENTURE_ID.getValue(), adventureData.getId())))
                  .ifPresent(e -> e.setData(adventureData));
            }
        });
        backButton.addClickShortcut(Key.ESCAPE);

        resetButton = new Button("Reset", _ -> {
            binder.readBean(commandProviderData);
        });

        VerticalLayout vll = new VerticalLayout(createButton, backButton, resetButton, saveButton);
        vll.setMaxWidth("10%");
        vll.setMinWidth("10%");
        vll.setWidth("10%");
        VerticalLayout vlr = new VerticalLayout(ViewSupporter.doubleClickEditHint(), gridContainer);

        HorizontalLayout hl = new HorizontalLayout(vll, vlr);
        hl.setSizeFull();
        add(hl);
    }

    private Grid<CommandData> buildGrid() {
        Grid<CommandData> aGrid = new Grid<>(CommandData.class, false);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getVerb()))
             .setHeader("Verb").setAutoWidth(true);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getAdjective()))
             .setHeader("Adjective").setAutoWidth(true);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getNoun()))
             .setHeader("Noun").setAutoWidth(true);
        aGrid.addColumn(new ComponentRenderer<>(cmd -> stack(formatter.formatConditions(cmd.getPreConditions()))))
             .setHeader("Preconditions").setAutoWidth(true);
        aGrid.addColumn(new ComponentRenderer<>(cmd -> stack(formatter.formatActions(cmd.getActions()))))
             .setHeader("Actions").setAutoWidth(true);
        aGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        ViewSupporter.setSize(aGrid);
        return aGrid;
    }

    /** Stack each rendered line in its own Span so multi-entry precondition/action cells wrap vertically. */
    private static Component stack(List<String> lines) {
        Div box = new Div();
        box.getStyle().set("display", "flex").set("flex-direction", "column");
        lines.forEach(line -> box.add(new Span(line)));
        return box;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        if (optionalItemId.isPresent()) {
            pageTitle = "Commands for item #" + optionalItemId.get();
        } else {
            String locationId = event.getRouteParameters().get(LOCATION_ID.getValue()).orElse("666");
            pageTitle = "Commands for location #" + locationId;
        }
    }

    private GridListDataView<CommandData> fillGrid(CommandProviderData aCommandProviderData) {
        final List<CommandData> rows = new ArrayList<>();
        for (CommandChainData chain : aCommandProviderData.getAvailableCommands().values()) {
            rows.addAll(chain.getCommands());
        }
        return grid.setItems(rows);
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData, ItemData anItemData) {
        itemData = anItemData;
        populate(anAdventureData, aLocationData);
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        itemData = null;
        populate(anAdventureData, aLocationData);
    }

    private void populate(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

        commandProviderData = itemData != null
                ? itemData.getCommandProviderData()
                : locationData.getCommandProviderData();
        binder.setBean(commandProviderData);

        formatter = new PreconditionActionFormatter(adventureData);
        grid = buildGrid();
        grid.setEmptyStateText("Create some commands.");

        // Double-click edits the command. Editing is keyed by the command spec; CommandEditorView is
        // chain-aware and opens the chain (at index 0) for that spec.
        grid.addItemDoubleClickListener(e ->
                navigateToCommandEditor(e.getItem().getCommandDescription().getCommandSpecification()));

        gridListDataView = fillGrid(commandProviderData);

        // Add context menu
        new CommandContextMenu(grid);

        gridContainer.setSizeFull();
        gridContainer.add(grid);
        saveButton.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private void navigateToCommandEditor(String aCommandId) {
        if (itemData != null) {
            UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                      new RouteParam(RouteIds.COMMAND_ID.getValue(), aCommandId),
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                      new RouteParam(RouteIds.ITEM_ID.getValue(), itemData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData, itemData));
        } else {
            UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                      new RouteParam(RouteIds.COMMAND_ID.getValue(), aCommandId),
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData));
        }
    }

    private class CommandContextMenu extends GridContextMenu<CommandData> {
        public CommandContextMenu(Grid<CommandData> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(command ->
                    navigateToCommandEditor(command.getCommandDescription().getCommandSpecification())));

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(command -> {
                String commandSpec = command.getCommandDescription().getCommandSpecification();
                CommandChainData chain = commandProviderData.getAvailableCommands().get(commandSpec);
                if (chain != null) {
                    chain.getCommands().remove(command);
                    // Drop the whole spec entry once its chain is empty.
                    if (chain.getCommands().isEmpty()) {
                        commandProviderData.getAvailableCommands().remove(commandSpec);
                    }
                }
                gridListDataView.removeItem(command);
                if (itemData != null) {
                    itemService.saveItem(itemData);
                } else {
                    adventureService.saveLocationData(locationData);
                }
                gridListDataView.refreshAll();
            }));
        }
    }

}

