package com.pdg.adventure.views.commands;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.views.components.VocabularyPicker;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class GridUnbufferedInlineEditor extends VerticalLayout {

    public GridUnbufferedInlineEditor(Set<CommandDescriptionData> aListOfCommandDescriptions) {
        ValidationMessage verbValidationMessage = new ValidationMessage();
        ValidationMessage adjectiveValidationMessage = new ValidationMessage();
        ValidationMessage nounValidationMessage = new ValidationMessage();

        Grid<CommandDescriptionData> grid = new Grid<>(CommandDescriptionData.class, false);
        Grid.Column<CommandDescriptionData> verbColumn = grid.addColumn(cdd -> wordToTextConverter(CommandDescriptionData::getVerb, cdd)).setHeader("Verb").setWidth("120px").setFlexGrow(0);
        Grid.Column<CommandDescriptionData> adjectiveColumn = grid.addColumn(cdd -> wordToTextConverter(CommandDescriptionData::getAdjective, cdd)).setHeader("Adjective").setWidth("120px").setFlexGrow(0);
        Grid.Column<CommandDescriptionData> nounColumn = grid.addColumn(cdd -> wordToTextConverter(CommandDescriptionData::getNoun, cdd)).setHeader("Noun");
        grid.setEmptyStateText("Create some commands.");

        Binder<CommandDescriptionData> binder = new Binder<>(CommandDescriptionData.class);
        Editor<CommandDescriptionData> editor = grid.getEditor();
        editor.setBinder(binder);

        VocabularyPicker verbField = new VocabularyPicker("Verb");
        VocabularyPicker adjectiveField = new VocabularyPicker("Adjective");
        VocabularyPicker nounField = new VocabularyPicker("Noun");
        int numberOfCommands = aListOfCommandDescriptions.size();
        List<Word> allVerbs = new ArrayList(numberOfCommands);
        List<Word> allAdjectives = new ArrayList(numberOfCommands);
        List<Word> allNouns = new ArrayList(numberOfCommands);
        for (CommandDescriptionData cdd : aListOfCommandDescriptions) {
            Word verb = cdd.getVerb();
            if (!verb.getText().isEmpty()) {
                allVerbs.add(cdd.getVerb());
            }
            Word adjective = cdd.getAdjective();
            if (!adjective.getText().isEmpty()) {
                allAdjectives.add(adjective);
            }
            Word noun = cdd.getNoun();
            if (!noun.getText().isEmpty()) {
                allNouns.add(noun);
            }
        }

        verbField.setItems(allVerbs);
        verbField.setWidthFull();
        addCloseHandler(verbField, editor);
        binder.forField(verbField).asRequired("Verb must not be empty").withStatusLabel(verbValidationMessage).bind(CommandDescriptionData::getVerb, CommandDescriptionData::setVerb);
        verbColumn.setEditorComponent(verbField);

        adjectiveField.setItems(allAdjectives);
        adjectiveField.setWidthFull();
        addCloseHandler(adjectiveField, editor);
        binder.forField(adjectiveField).bind(CommandDescriptionData::getAdjective, CommandDescriptionData::setAdjective);
        adjectiveColumn.setEditorComponent(adjectiveField);

        nounField.setItems(allNouns);
        nounField.setWidthFull();
        addCloseHandler(nounField, editor);
        binder.forField(nounField).bind(CommandDescriptionData::getNoun, CommandDescriptionData::setNoun);
        nounColumn.setEditorComponent(nounField);

        grid.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable<?> ec) {
                ec.focus();
            }
        });

        editor.addCancelListener(e -> {
            verbValidationMessage.setText("");
            adjectiveValidationMessage.setText("");
            nounValidationMessage.setText("");
        });

        Set<CommandDescriptionData> commandDescriptions = aListOfCommandDescriptions;
        grid.setItems(commandDescriptions);
        grid.setWidth(300, Unit.PIXELS);
        getThemeList().clear();
        getThemeList().add("spacing-s");
        add(grid, verbValidationMessage, adjectiveValidationMessage, nounValidationMessage);
    }

    private String wordToTextConverter(Function<CommandDescriptionData, Word> wordFunction, CommandDescriptionData aCommandDescriptionData) {
        return wordFunction.apply(aCommandDescriptionData) == null ? "" : wordFunction.apply(aCommandDescriptionData).getText();
    }

    private static void addCloseHandler(Component textField, Editor<CommandDescriptionData> editor) {
        textField.getElement().addEventListener("keydown", e -> editor.cancel()).setFilter("event.code === 'Escape'");
    }

}
