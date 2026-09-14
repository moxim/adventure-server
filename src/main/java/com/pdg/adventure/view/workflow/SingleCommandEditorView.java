package com.pdg.adventure.view.workflow;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.pdg.adventure.model.Word.Type.*;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.command.CommandViewModel;
import com.pdg.adventure.view.command.PreconditionActionEditor;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.component.VocabularyPicker;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Single-command editor page, shared by {@link WorkflowCommandEditorView} (pre-commands),
 * {@link ArrivalCommandEditorView} (arrival-triggered commands), and
 * {@link ResponseCommandEditorView} (interceptor commands) - the page-nav counterpart to
 * {@link CommandListEditorView}, which lists all commands of one type. Unlike
 * {@code com.pdg.adventure.view.command.CommandEditorView}, there is no command-chain concept
 * here: each of the three {@code WorkflowData} lists this edits is flat, so a command is looked
 * up directly by its own stable id, no chain indirection needed.
 */
public class SingleCommandEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(SingleCommandEditorView.class);

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final String itemLabel;
    private final SerializableFunction<WorkflowData, List<CommandData>> listAccessor;
    private final Class<? extends Component> listViewClass;

    private final Binder<CommandViewModel> binder;
    private final VocabularyPicker verbSelector;
    private final VocabularyPicker adjectiveSelector;
    private final VocabularyPicker nounSelector;
    private final Span preconditionAndActionHolder;
    private PreconditionActionEditor preconditionActionEditor;
    private Button saveButton;
    private Button resetButton;

    private transient String commandId;
    private String pageTitle;
    private AdventureData adventureData;
    private WorkflowData workflowData;
    private transient CommandViewModel cvm;
    private transient CommandData commandData;
    private boolean editorHasChanges = false;

    protected SingleCommandEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService,
                                       String anItemLabel, CommandListType aCommandListType,
                                       SerializableFunction<WorkflowData, List<CommandData>> aListAccessor,
                                       Class<? extends Component> aListViewClass) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        itemLabel = anItemLabel;
        listAccessor = aListAccessor;
        listViewClass = aListViewClass;
        binder = new Binder<>(CommandViewModel.class);

        Span helpText = new Span("Entering verb / adjective / noun combos here is optional " +
                        "and serves only as a hint for you. They are not evaluated against the player's input.");
        helpText.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");

        verbSelector = new VocabularyPickerField("Verb", "You may filter on verbs.");
        adjectiveSelector = new VocabularyPickerField("Adjective", "You may filter on adjectives.");
        nounSelector = new VocabularyPickerField("Noun", "You may filter on nouns.");
        setUpBinding();
        if (aCommandListType == CommandListType.PROCESS || aCommandListType == CommandListType.ARRIVAL) {
            verbSelector.setRequired(false);
        }

        HorizontalLayout commandFieldsRow = new HorizontalLayout(verbSelector, adjectiveSelector, nounSelector);

        final ResetBackSaveView resetBackSaveView = setUpNavigationButtons();

        preconditionAndActionHolder = new Span();
        VerticalLayout editorSection = new VerticalLayout(new NativeLabel("Preconditions & Actions"),
                                                           preconditionAndActionHolder);

        setSizeFull();
        setMargin(true);
        setPadding(true);
        add(helpText, commandFieldsRow, editorSection, resetBackSaveView);
    }

    private void setUpBinding() {
        binder.forField(verbSelector).asRequired("Verb is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a verb with text")
              .bind(CommandViewModel::getVerb, CommandViewModel::setVerb);
        binder.forField(adjectiveSelector).bind(CommandViewModel::getAdjective, CommandViewModel::setAdjective);
        binder.forField(nounSelector).bind(CommandViewModel::getNoun, CommandViewModel::setNoun);

        binder.addStatusChangeListener(event -> {
            updateSaveButtonState();
            resetButton.setEnabled(event.getBinder().hasChanges() || editorHasChanges);
        });
    }

    private ResetBackSaveView setUpNavigationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        backButton.addClickShortcut(Key.ESCAPE);
        saveButton = resetBackSaveView.getSave();
        saveButton.setEnabled(false);
        saveButton.setText("Save " + itemLabel);
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);

        backButton.addClickListener(_ -> navigateBack());
        saveButton.addClickListener(_ -> saveCommand());
        resetButton.addClickListener(_ -> {
            binder.readBean(cvm);
            preconditionActionEditor.setCommand(commandData != null ? commandData : new CommandData());
            editorHasChanges = false;
            resetButton.setEnabled(false);
        });

        return resetBackSaveView;
    }

    private void navigateBack() {
        UI.getCurrent().navigate(listViewClass, new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
    }

    private void updateSaveButtonState() {
        boolean valid = binder.isValid() && preconditionActionEditor != null && preconditionActionEditor.validate();
        saveButton.setEnabled(valid && (binder.hasChanges() || editorHasChanges));
    }

    private void saveCommand() {
        if (persistCommand()) {
            navigateBack();
        }
    }

    /**
     * Package-private for testing: the actual validate/write/persist work, without the
     * navigate-back that follows a real save. Driving Save through the real verb/adjective/noun
     * {@code ComboBox} fields is unreliable under {@code BrowserlessTest} (see
     * {@link #getVerbSelector()}), and {@code navigateBack()} targets a Spring-managed view
     * {@code BrowserlessTest} can't instantiate outside a real application context - so tests
     * call this directly instead of clicking the Save button. Returns whether the save actually
     * happened (false on a validation failure, mirroring the button click's no-op in that case).
     */
    boolean persistCommand() {
        try {
            if (!preconditionActionEditor.validate() || !binder.validate().isOk()) {
                return false;
            }
            binder.writeBean(cvm);
            commandData.setCommandDescription(cvm.getData());
            preconditionActionEditor.saveToCommand(commandData);

            if (!commands().contains(commandData)) {
                commands().add(commandData);
            }

            adventureService.saveAdventureData(adventureData);
            editorHasChanges = false;
            return true;
        } catch (ValidationException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    private List<CommandData> commands() {
        return listAccessor.apply(workflowData);
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
        Optional<String> optionalCommandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        commandId = optionalCommandId.map(AdventureRouteResolver::decodeRouteParam).orElse(null);
        populate(resolvedAdventure.get());
    }

    private void populate(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        workflowData = adventureData.getWorkflowData();

        if (preconditionActionEditor == null) {
            preconditionActionEditor = new PreconditionActionEditor(adventureData);
            preconditionActionEditor.setOnChange(() -> {
                editorHasChanges = true;
                updateSaveButtonState();
                resetButton.setEnabled(true);
            });
            preconditionAndActionHolder.add(preconditionActionEditor);
        }

        commandData = (commandId != null && !commandId.isEmpty())
                ? commands().stream().filter(cmd -> commandId.equals(cmd.getId())).findFirst().orElse(null)
                : null;

        CommandDescriptionData commandDescriptionData;
        if (commandData != null) {
            commandDescriptionData = commandData.getCommandDescription();
            pageTitle = "Edit " + itemLabel + ": " + ViewSupporter.getDescriptionText(commandDescriptionData);
        } else {
            commandData = new CommandData();
            commandDescriptionData = commandData.getCommandDescription();
            pageTitle = "New " + itemLabel;
        }

        VocabularyData vocabularyData = adventureData.getVocabularyData();
        verbSelector.populate(vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());
        adjectiveSelector.populate(vocabularyData.getWords(ADJECTIVE).stream().filter(word -> word.getSynonym() == null).toList());
        nounSelector.populate(vocabularyData.getWords(NOUN).stream().filter(word -> word.getSynonym() == null).toList());

        saveButton.setEnabled(false);
        cvm = new CommandViewModel(commandDescriptionData);
        binder.readBean(cvm);
        editorHasChanges = false;

        preconditionActionEditor.setCommand(commandData);
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges() || editorHasChanges);
    }

    /** Test seam: exposes the verb picker so tests can assert vocabulary reached it and check
     * its required-ness without relying on ComboBox setValue()/getValue(), which is unreliable
     * under BrowserlessTest. */
    VocabularyPicker getVerbSelector() {
        return verbSelector;
    }
}
