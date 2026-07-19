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
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

@Route(value = "author/adventures/:adventureId/workflow", layout = WorkflowMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class WorkflowEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowEditorView.class);

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;

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

    public WorkflowEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(CommandViewModel.class);

        verbSelector = new VocabularyPickerField("Verb", "You may filter on verbs.");
        adjectiveSelector = new VocabularyPickerField("Adjective", "You may filter on adjectives.");
        nounSelector = new VocabularyPickerField("Noun", "You may filter on nouns.");
        setUpBinding();

        Span helpText = new Span("Workflow commands run automatically every turn. " +
                                 "Add preconditions to control when it fires; " +
                                 "an unmet precondition still shows its message every turn," +
                                 " it does not silently skip.");
        helpText.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");

        grid = buildGrid();
        grid.addSelectionListener(selection -> selection.getFirstSelectedItem().ifPresent(this::loadIntoEditor));

        backButton = new Button("Back", _ -> UI.getCurrent().navigate(AdventureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        newCommandButton = new Button("New Command", _ -> loadIntoEditor(new CommandData()));

        deleteCommandButton = new Button("Delete Command", _ -> confirmDeleteCommand(selectedCommand));
        deleteCommandButton.setEnabled(false);

        saveCommandButton = new Button("Save Command", _ -> saveCommand());
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
        aGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        aGrid.setEmptyStateText("No workflow commands yet. Create one to get started.");
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

    private void loadIntoEditor(CommandData aCommandData) {
        selectedCommand = aCommandData;
        cvm = new CommandViewModel(aCommandData.getCommandDescription());
        binder.readBean(cvm);
        preconditionActionEditor.setCommand(aCommandData);
        editorHasChanges = false;
        deleteCommandButton.setEnabled(workflowData.getCommands().contains(aCommandData));
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

            if (!workflowData.getCommands().contains(selectedCommand)) {
                workflowData.getCommands().add(selectedCommand);
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
        var dialog = ViewSupporter.getConfirmDialog("Delete Command", "command",
                ViewSupporter.formatDescription(aCommand.getCommandDescription()));
        dialog.addConfirmListener(_ -> {
            workflowData.getCommands().remove(aCommand);
            adventureService.saveAdventureData(adventureData);
            refreshGrid();
            loadIntoEditor(new CommandData());
        });
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(new ArrayList<>(workflowData.getCommands()));
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
        pageTitle = "Workflow for " + resolvedAdventure.get().getTitle();
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
