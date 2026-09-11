package com.pdg.adventure.view.workflow;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.pdg.adventure.model.Word.Type.*;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.command.CommandViewModel;
import com.pdg.adventure.view.command.PreconditionActionEditor;
import com.pdg.adventure.view.command.PreconditionActionFormatter;
import com.pdg.adventure.view.component.VocabularyPicker;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Grid-of-commands + single-command editor, shared by {@link WorkflowEditorView} (pre-commands),
 * {@link ArrivalProcessesEditorView} (arrival-triggered commands), and {@link ResponsesEditorView}
 * (interceptor commands) — the three collections on {@code Workflow} that authors edit the same
 * way, differing only in which list they read/write and how that's described to the author. Kept
 * as a single concrete class (constructor-parameterized) rather than an abstract base since every
 * difference between the three screens is a fixed piece of data, not behaviour.
 */
public class CommandListEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    public enum CommandListType {
        PROCESS,
        ARRIVAL,
        RESPONSE
    }

    private static final Logger LOG = LoggerFactory.getLogger(CommandListEditorView.class);

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
    // SerializableFunction, not java.util.function.Function: unlike formatter/cvm/selectedCommand
    // below (marked transient because populate() rebuilds them from scratch on the next
    // beforeEnter), this is only ever set once, in the constructor - so it needs to actually survive
    // session serialization rather than come back null.
    private final SerializableFunction<WorkflowData, List<CommandData>> listAccessor;

    private final Binder<CommandViewModel> binder;
    private final VocabularyPicker verbSelector;
    private final VocabularyPicker adjectiveSelector;
    private final VocabularyPicker nounSelector;
    private final Span preconditionAndActionHolder;
    private PreconditionActionEditor preconditionActionEditor;

    private final Grid<CommandData> grid;
    private final Button backButton;
    private final Button newCommandButton;
    private final Button deleteCommandButton;
    private final Button saveCommandButton;

    private AdventureData adventureData;
    private WorkflowData workflowData;
    private transient PreconditionActionFormatter formatter;
    private transient CommandViewModel cvm;
    private transient CommandData selectedCommand;
    private boolean editorHasChanges = false;
    private String pageTitle;

    protected CommandListEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService,
                                     String anItemLabel, String aPageTitlePrefix, String aHelpText,
                                     String anEmptyStateText, CommandListType aCommandListType,
                                     SerializableFunction<WorkflowData, List<CommandData>> aListAccessor) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        itemLabel = anItemLabel;
        pageTitlePrefix = aPageTitlePrefix;
        listAccessor = aListAccessor;
        binder = new Binder<>(CommandViewModel.class);

        verbSelector = new VocabularyPickerField("Verb", "You may filter on verbs.");
        adjectiveSelector = new VocabularyPickerField("Adjective", "You may filter on adjectives.");
        nounSelector = new VocabularyPickerField("Noun", "You may filter on nouns.");
        setUpBinding();
        if (aCommandListType == CommandListType.PROCESS || aCommandListType == CommandListType.ARRIVAL) {
            verbSelector.setRequired(false);
        }

        Span helpText = new Span(aHelpText);
        helpText.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");

        grid = buildGrid(anEmptyStateText);
        grid.addSelectionListener(selection -> selection.getFirstSelectedItem().ifPresent(this::loadIntoEditor));

        backButton = new Button("Back", _ -> UI.getCurrent().navigate(AdventureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        newCommandButton = new Button("New " + itemLabel, _ -> loadIntoEditor(new CommandData()));

        deleteCommandButton = new Button("Delete " + itemLabel, _ -> confirmDeleteCommand(selectedCommand));
        deleteCommandButton.setEnabled(false);

        saveCommandButton = new Button("Save " + itemLabel, _ -> saveCommand());
        saveCommandButton.setEnabled(false);

        HorizontalLayout gridButtons = new HorizontalLayout(backButton, newCommandButton, deleteCommandButton);
        VerticalLayout gridSection = new VerticalLayout(gridButtons, grid);
        gridSection.setSizeFull();

        HorizontalLayout commandFieldsRow = new HorizontalLayout(verbSelector, adjectiveSelector, nounSelector);

        preconditionAndActionHolder = new Span();
        VerticalLayout editorSection = new VerticalLayout(new NativeLabel("Preconditions & Actions"),
                                                          preconditionAndActionHolder, saveCommandButton);

        setSizeFull();
        setMargin(true);
        setPadding(true);
        add(helpText, gridSection, commandFieldsRow, editorSection);
    }

    private void setUpBinding() {
        binder.forField(verbSelector).asRequired("Verb is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a verb with text")
              .bind(CommandViewModel::getVerb, CommandViewModel::setVerb);
        binder.forField(adjectiveSelector).bind(CommandViewModel::getAdjective, CommandViewModel::setAdjective);
        binder.forField(nounSelector).bind(CommandViewModel::getNoun, CommandViewModel::setNoun);

        binder.addStatusChangeListener(event -> updateSaveButtonState());
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

    private void loadIntoEditor(CommandData aCommandData) {
        selectedCommand = aCommandData;
        cvm = new CommandViewModel(aCommandData.getCommandDescription());
        binder.readBean(cvm);
        preconditionActionEditor.setCommand(aCommandData);
        editorHasChanges = false;
        deleteCommandButton.setEnabled(commands().contains(aCommandData));
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        boolean valid = binder.isValid() && preconditionActionEditor != null && preconditionActionEditor.validate();
        saveCommandButton.setEnabled(valid && (binder.hasChanges() || editorHasChanges));
    }

    private void saveCommand() {
        try {
            if (!preconditionActionEditor.validate() || !binder.validate().isOk()) {
                return;
            }
            binder.writeBean(cvm);
            selectedCommand.setCommandDescription(cvm.getData());
            preconditionActionEditor.saveToCommand(selectedCommand);

            if (!commands().contains(selectedCommand)) {
                commands().add(selectedCommand);
            }

            adventureService.saveAdventureData(adventureData);
            refreshGrid();
            editorHasChanges = false;
            loadIntoEditor(new CommandData());
        } catch (ValidationException e) {
            LOG.error(e.getMessage());
        }
    }

    private void confirmDeleteCommand(CommandData aCommand) {
        var dialog = ViewSupporter.getConfirmDialog("Delete " + itemLabel, itemLabel.toLowerCase(),
                ViewSupporter.formatDescription(aCommand.getCommandDescription()));
        dialog.addConfirmListener(_ -> {
            commands().remove(aCommand);
            adventureService.saveAdventureData(adventureData);
            refreshGrid();
            loadIntoEditor(new CommandData());
        });
        dialog.open();
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

        if (preconditionActionEditor == null) {
            preconditionActionEditor = new PreconditionActionEditor(adventureData);
            preconditionActionEditor.setOnChange(() -> {
                editorHasChanges = true;
                updateSaveButtonState();
            });
            preconditionAndActionHolder.add(preconditionActionEditor);
        }

        VocabularyData vocabularyData = adventureData.getVocabularyData();
        verbSelector.populate(vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());
        adjectiveSelector.populate(vocabularyData.getWords(ADJECTIVE).stream().filter(word -> word.getSynonym() == null).toList());
        nounSelector.populate(vocabularyData.getWords(NOUN).stream().filter(word -> word.getSynonym() == null).toList());

        refreshGrid();
        loadIntoEditor(new CommandData());
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges() || editorHasChanges);
    }

    /** Test seam: exposes the verb picker so tests can assert vocabulary reached it without relying on
     * ComboBox setValue()/getValue(), which is unreliable under BrowserlessTest. */
    VocabularyPicker getVerbSelector() {
        return verbSelector;
    }
}
