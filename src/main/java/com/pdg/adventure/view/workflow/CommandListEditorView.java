package com.pdg.adventure.view.workflow;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.command.PreconditionActionFormatter;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Lists all commands of one type, shared by {@link WorkflowEditorView} (pre-commands),
 * {@link ArrivalProcessesEditorView} (arrival-triggered commands), and {@link ResponsesEditorView}
 * (interceptor commands) — the three collections on {@code Workflow} that authors browse the same
 * way, differing only in which list they read/write and how that's described to the author. Kept
 * as a single concrete class (constructor-parameterized) rather than an abstract base since every
 * difference between the three screens is a fixed piece of data, not behaviour. Editing one
 * command is a separate routed page ({@link SingleCommandEditorView} and its own three
 * subclasses) reached by double-click or the grid's context menu, the same page-nav pattern
 * {@code com.pdg.adventure.view.command.CommandsMenuView}/{@code CommandEditorView} already use.
 */
public class CommandListEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver {

    // Grid presentation order: alphabetical by verb, then adjective, then noun - matches the
    // Verb/Adjective/Noun column order and how Workflow.process() now orders preCommand execution.
    private static final Comparator<CommandData> ALPHABETICAL = Comparator
            .comparing((CommandData cmd) -> ViewSupporter.getWordText(cmd.getCommandDescription().getVerb()),
                       String.CASE_INSENSITIVE_ORDER)
            .thenComparing(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getAdjective()),
                            String.CASE_INSENSITIVE_ORDER)
            .thenComparing(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getNoun()),
                            String.CASE_INSENSITIVE_ORDER);

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final String itemLabel;
    private final String pageTitlePrefix;
    // SerializableFunction, not java.util.function.Function: this is only ever set once, in the
    // constructor - so it needs to actually survive session serialization rather than come back
    // null (unlike formatter below, which populate() rebuilds from scratch on the next beforeEnter).
    private final SerializableFunction<WorkflowData, List<CommandData>> listAccessor;
    private final Class<? extends Component> editorViewClass;

    private final Grid<CommandData> grid;
    private final Button backButton;
    private final Button newCommandButton;

    private AdventureData adventureData;
    private WorkflowData workflowData;
    private transient PreconditionActionFormatter formatter;
    private String pageTitle;

    protected CommandListEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService,
                                     String anItemLabel, String aPageTitlePrefix, String aHelpText,
                                     String anEmptyStateText,
                                     SerializableFunction<WorkflowData, List<CommandData>> aListAccessor,
                                     Class<? extends Component> anEditorViewClass) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        itemLabel = anItemLabel;
        pageTitlePrefix = aPageTitlePrefix;
        listAccessor = aListAccessor;
        editorViewClass = anEditorViewClass;

        Span helpText = new Span(aHelpText);
        helpText.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");

        grid = buildGrid(anEmptyStateText);
        grid.addItemDoubleClickListener(e -> navigateToEditor(e.getItem().getId()));
        new CommandContextMenu(grid);

        backButton = new Button("Back", _ -> UI.getCurrent().navigate(AdventureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        newCommandButton = new Button("Create " + itemLabel, _ -> navigateToEditor(null));

        VerticalLayout gridButtons = new VerticalLayout(newCommandButton, backButton);
        VerticalLayout gridSection = new VerticalLayout(ViewSupporter.doubleClickEditHint(), grid);
        gridSection.setSizeFull();
        HorizontalLayout hl = new HorizontalLayout(gridButtons, gridSection);

        setSizeFull();
        setMargin(true);
        setPadding(true);
        add(helpText, hl);
    }

    private Grid<CommandData> buildGrid(String anEmptyStateText) {
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
        aGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        aGrid.setEmptyStateText(anEmptyStateText);
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

    private List<CommandData> commands() {
        return listAccessor.apply(workflowData);
    }

    private void navigateToEditor(String aCommandId) {
        RouteParameters params = aCommandId == null
                ? new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))
                : new RouteParameters(
                        new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                        new RouteParam(RouteIds.COMMAND_ID.getValue(), aCommandId));
        UI.getCurrent().navigate(editorViewClass, params);
    }

    private void refreshGrid() {
        List<CommandData> sorted = new ArrayList<>(commands());
        sorted.sort(ALPHABETICAL);
        grid.setItems(sorted);
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        pageTitle = pageTitlePrefix + resolvedAdventure.get().getTitle();
        populate(resolvedAdventure.get());
    }

    private void populate(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        workflowData = adventureData.getWorkflowData();
        formatter = new PreconditionActionFormatter(adventureData);
        refreshGrid();
    }

    private class CommandContextMenu extends GridContextMenu<CommandData> {
        public CommandContextMenu(Grid<CommandData> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(cmd -> navigateToEditor(cmd.getId())));

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(cmd -> buildDeleteConfirmDialog(cmd).open()));
        }
    }

    /**
     * Package-private for testing: GridContextMenu item clicks have no reliable way to be
     * driven from a browserless test, so this builds the dialog (and wires its confirm
     * listener) in isolation from the context-menu click that triggers it.
     */
    ConfirmDialog buildDeleteConfirmDialog(CommandData aCommand) {
        ConfirmDialog dialog = ViewSupporter.getConfirmDialog("Delete " + itemLabel, itemLabel.toLowerCase(),
                ViewSupporter.formatDescription(aCommand.getCommandDescription()));
        dialog.addConfirmListener(_ -> {
            commands().remove(aCommand);
            adventureService.saveAdventureData(adventureData);
            refreshGrid();
        });
        return dialog;
    }
}
