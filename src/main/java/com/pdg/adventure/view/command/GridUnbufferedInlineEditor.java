package com.pdg.adventure.view.command;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.view.component.VocabularyPicker;

public class GridUnbufferedInlineEditor extends VerticalLayout {

    private final Grid<CommandDescriptionData> grid = new Grid<>(CommandDescriptionData.class, false);
    private final GridListDataView<CommandDescriptionData> listDataView;

    public GridUnbufferedInlineEditor(Set<CommandDescriptionData> aListOfCommandDescriptions, VocabularyData aVocabularyData,
                                      Button aSaveButton) {
        Grid<CommandDescriptionData> grid = new Grid<>(CommandDescriptionData.class, false);
        Grid.Column<CommandDescriptionData> verbColumn = grid.addColumn(cdd -> wordToTextConverter(CommandDescriptionData::getVerb, cdd)).setHeader("Verb").setWidth("25%").setFlexGrow(0);
        Grid.Column<CommandDescriptionData> adjectiveColumn = grid.addColumn(cdd -> wordToTextConverter(CommandDescriptionData::getAdjective, cdd)).setHeader("Adjective").setWidth("40%").setFlexGrow(0);
        Grid.Column<CommandDescriptionData> nounColumn = grid.addColumn(cdd -> wordToTextConverter(CommandDescriptionData::getNoun, cdd)).setHeader("Noun");
        grid.setEmptyStateText("Create some commands.");

        Binder<CommandDescriptionData> binder = new BeanValidationBinder<>(CommandDescriptionData.class);
        Editor<CommandDescriptionData> editor = grid.getEditor();
        editor.setBinder(binder);

        VocabularyPicker verbField = new VocabularyPicker("Verb");
        VocabularyPicker adjectiveField = new VocabularyPicker("Adjective");
        VocabularyPicker nounField = new VocabularyPicker("Noun");

        List<Word> availableVerbs = new LinkedList<>();
        List<Word> availableAdjectives = new LinkedList<>();
        List<Word> availableNouns = new LinkedList<>();

        aVocabularyData.getWords().forEach(word -> {
                   switch (word.getType()) {
                       case VERB -> availableVerbs.add(word);
                       case ADJECTIVE -> availableAdjectives.add(word);
                       case NOUN -> availableNouns.add(word);
                       default -> {
                       }
                       // do nothing
                   }
               }
        );
        verbField.setItems(availableVerbs);
        verbField.setWidthFull();
        verbField.setHelperText("");
        addCloseHandler(verbField, editor);
        ValidationMessage verbValidationMessage = new ValidationMessage();
        binder.forField(verbField).asRequired("Verb must not be empty").withStatusLabel(verbValidationMessage)
              .bind(CommandDescriptionData::getVerb, CommandDescriptionData::setVerb);
        verbColumn.setEditorComponent(verbField);

        adjectiveField.setItems(availableAdjectives);
        adjectiveField.setWidthFull();
        adjectiveField.setHelperText("");
        addCloseHandler(adjectiveField, editor);
        binder.forField(adjectiveField)
              .bind(CommandDescriptionData::getAdjective, CommandDescriptionData::setAdjective);
        adjectiveColumn.setEditorComponent(adjectiveField);

        nounField.setItems(availableNouns);
        nounField.setWidthFull();
        nounField.setHelperText("");
        addCloseHandler(nounField, editor);
        binder.forField(nounField).bind(CommandDescriptionData::getNoun, CommandDescriptionData::setNoun);
        nounColumn.setEditorComponent(nounField);

        grid.addItemDoubleClickListener(e -> {
            final CommandDescriptionData item = e.getItem();
            CommandDescriptionData oldItem = new CommandDescriptionData(item.getCommandSpecification());
            editor.editItem(item);
            final Binder<CommandDescriptionData> localBinder = editor.getBinder();
            localBinder.setBean(item);

            editor.addCloseListener(event -> {
                extracted(aSaveButton, editor);
            });
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable<?> ec) {
                ec.focus();
            }
        });

        ValidationMessage adjectiveValidationMessage = new ValidationMessage();
        ValidationMessage nounValidationMessage = new ValidationMessage();

        editor.addCancelListener(e -> {
            verbValidationMessage.setText("");
            adjectiveValidationMessage.setText("");
            nounValidationMessage.setText("");
        });

        listDataView = grid.setItems(aListOfCommandDescriptions);
        grid.setWidth(640, Unit.PIXELS);
        getThemeList().clear();
        getThemeList().add("spacing-s");
        add(grid, verbValidationMessage, adjectiveValidationMessage, nounValidationMessage);
    }

    private static void extracted(Button aSaveButton, Editor<CommandDescriptionData> editor) {
        final Binder<CommandDescriptionData> localBinder = editor.getBinder();
        if (localBinder.hasChanges()) {
            aSaveButton.setEnabled(true);
        }
    }

    public void addItem(CommandDescriptionData newCommandDescription) {
        listDataView.addItem(newCommandDescription);
    }

    private String wordToTextConverter(Function<CommandDescriptionData, Word> wordFunction, CommandDescriptionData aCommandDescriptionData) {
        return wordFunction.apply(aCommandDescriptionData) == null ? "" : wordFunction.apply(aCommandDescriptionData).getText();
    }

    private static void addCloseHandler(Component textField, Editor<CommandDescriptionData> editor) {
        textField.getElement().addEventListener("keydown", e -> editor.cancel()).setFilter("event.code === 'Escape'");
    }

    public void setEmptyStateText(String anEmptyStateText) {
        grid.setEmptyStateText(anEmptyStateText);
    }
}
