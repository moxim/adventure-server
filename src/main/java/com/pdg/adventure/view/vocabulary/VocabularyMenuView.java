package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static com.pdg.adventure.model.Word.Type.VERB;

import com.pdg.adventure.model.*;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.GridProvider;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "adventures/:adventureId/vocabulary", layout = VocabularyMainLayout.class)
@RouteAlias(value = "adventures/vocabulary", layout = VocabularyMainLayout.class)
@PageTitle("Vocabulary")
public class VocabularyMenuView extends VerticalLayout implements SaveListener, GuiListener {

    private transient final AdventureService adventureService;
    private AdventureData adventureData;
    private VocabularyData vocabularyData;

    private Button create;
    private Button edit;
    private Button back;
    private Button save;
    private TextField searchField;
    private Div gridContainer;
    private VocabularyPickerField takeSelector;
    private VocabularyPickerField dropSelector;
    private DescribableWordAdapter currentWordAdapter;

    @Autowired
    public VocabularyMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        setSizeFull();
        createGUI();
    }

    private void createGUI() {
        final VerticalLayout leftSide = createLeftSide();
        final VerticalLayout rightSide = createRightSide();
        final VerticalLayout farSide = createFarSide();
        HorizontalLayout horizontalLayout = new HorizontalLayout(leftSide, rightSide, farSide);
        add(horizontalLayout);
    }

    private VerticalLayout createFarSide() {
        takeSelector = new VocabularyPickerField("Taker");
        takeSelector.setHelperText("A verb used for taking items.");
        takeSelector.addValueChangeListener(event -> {vocabularyData.setTakeWord(event.getValue());});

        dropSelector = new VocabularyPickerField("Dropper");
        dropSelector.setHelperText("A verb used for dropping items.");
        dropSelector.addValueChangeListener(event -> {vocabularyData.setDropWord(event.getValue());});

        NativeLabel specialLabel = new NativeLabel("Special Verbs");
        specialLabel.getStyle().set("font-weight", "bold")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin-bottom", "var(--lumo-space-s)");
        final VerticalLayout verticalLayout = new VerticalLayout(specialLabel, takeSelector, dropSelector);
//        verticalLayout.getStyle().set("font-size", "var(--lumo-font-size-l)");
        return verticalLayout;
    }

    private VerticalLayout createRightSide() {
        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find Word");
        searchField.setTooltipText("Find words by ID, text or type.");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        gridContainer = new Div();
        gridContainer.setSizeFull();
        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);
        return rightSide;
    }

    private VerticalLayout createLeftSide() {
        edit = new Button("Edit Word", e -> {createWordInfoDialog(WordEditorDialogue.EditType.EDIT, currentWordAdapter );});
        edit.setEnabled(false);
        create = new Button("Create Word", e -> {createWordInfoDialog(WordEditorDialogue.EditType.NEW, null);});
        back = new Button("Back", event -> {
            UI.getCurrent().navigate(AdventureEditorView.class,
                    new RouteParameters(
                            new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))
            );
        });
        back.addClickShortcut(Key.ESCAPE);
        save = new Button("Save", event -> {
            persistData();
            Notification.show("Vocabulary saved", 2000, Notification.Position.BOTTOM_START);
        });

        VerticalLayout vl = new VerticalLayout(create, edit, back, save);
        return vl;
    }

    private void createWordInfoDialog(WordEditorDialogue.EditType anEditType, DescribableWordAdapter aWord) {
        WordEditorDialogue dialogue = new WordEditorDialogue(vocabularyData);
        dialogue.addGuiListener(this);
        dialogue.addSaveListener(this);
        dialogue.open(anEditType, aWord);
    }


    private void fillGUI() {
        vocabularyData = adventureData.getVocabularyData();
        gridContainer.removeAll();
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);
        gridContainer.add(getVocabularyGrid(vocabularyData, searchField, filter));
        takeSelector.populate(vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());
        if (vocabularyData.getTakeWord() != null) {
            takeSelector.setValue(vocabularyData.getTakeWord());
        }
        dropSelector.populate(vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());
        if (vocabularyData.getDropWord() != null) {
            dropSelector.setValue(vocabularyData.getDropWord());
        }
    }

    private Grid<DescribableWordAdapter> getVocabularyGrid(VocabularyData aVocabularyData, TextField aSearchField, SerializablePredicate<DescribableWordAdapter> aFilter) {
        GridProvider<DescribableWordAdapter> gridProvider = new GridProvider<>(DescribableWordAdapter.class);
        gridProvider.addColumn(DescribableWordAdapter::getType, "Type");
        gridProvider.addColumn(DescribableWordAdapter::getSynonym, "Synonym");
        Grid<DescribableWordAdapter> grid = gridProvider.getGrid();
        grid.setWidth("500px");
        grid.setHeight("500px");

        List<DescribableWordAdapter> wordList = new ArrayList<>();
        for (Word word : aVocabularyData.getWords()) {
            wordList.add(new DescribableWordAdapter(word));
        }
        final GridListDataView<DescribableWordAdapter> dataView = grid.setItems(wordList);
        aSearchField.addValueChangeListener(e -> dataView.refreshAll());
        dataView.addFilter(aFilter);

        gridProvider.addItemDoubleClickListener(e -> {
            final DescribableWordAdapter wordAdapter = e.getItem();
            enterWordDialouge(wordAdapter);
        });

        gridProvider.addSelectionListener(  selectedWord   -> {
            final DescribableWordAdapter wordAdapter = selectedWord.getFirstSelectedItem().orElse(null);
            currentWordAdapter = wordAdapter;
            edit.setEnabled(wordAdapter != null);
        });

        // Add context menu
        createContextMenu(grid, dataView);

        return grid;
    }

    private void enterWordDialouge(final DescribableWordAdapter aWordAdapter) {
        createWordInfoDialog(WordEditorDialogue.EditType.EDIT, aWordAdapter);
    }

    public void setAdventureData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        updateGui();
    }

    @Override
    public void updateGui() {
        fillGUI();
    }

    @Override
    public void persistData() {
        adventureService.saveAdventureData(adventureData);
//        adventureService.saveWordData(adventureData.getVocabularyData().getWords());
//        adventureService.saveVocabularyData(adventureData.getVocabularyData());
    }

    private void createContextMenu(Grid<DescribableWordAdapter> grid, GridListDataView<DescribableWordAdapter> dataView) {
        GridContextMenu<DescribableWordAdapter> contextMenu = grid.addContextMenu();

        // Create a span to display the word text
        Span wordTextSpan = new Span();
        wordTextSpan.getStyle()
                .set("font-style", "italic")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("padding", "var(--lumo-space-s)")
                .set("display", "block")
                .set("max-width", "400px");

        // Update the word text when the context menu opens
        contextMenu.addGridContextMenuOpenedListener(event -> {
            event.getItem().ifPresent(wordAdapter -> {
                Word word = wordAdapter.getWord();
                wordTextSpan.setText("\"" + word.getText() + "\" (" + word.getType() + ")");
            });
        });

        // Add the word text at the top
        contextMenu.addComponentAsFirst(wordTextSpan);

        // Add separator
        contextMenu.addComponentAsFirst(new Hr());

        // Edit option
        contextMenu.addItem("Edit", event -> {
            event.getItem().ifPresent(wordAdapter ->
                    createWordInfoDialog(WordEditorDialogue.EditType.EDIT, wordAdapter)
            );
        });

        // Show usages option - always enabled
        contextMenu.addItem("Show Usages", event -> {
            event.getItem().ifPresent(this::showWordUsages);
        });

        // Delete option
        var deleteItem = contextMenu.addItem("Delete", event -> {
            event.getItem().ifPresent(wordAdapter -> confirmDeleteWord(wordAdapter, dataView));
        });

        // Dynamically enable/disable menu items based on word usage
        contextMenu.addGridContextMenuOpenedListener(event -> {
            event.getItem().ifPresent(wordAdapter -> {
                Word word = wordAdapter.getWord();
                List<WordUsage> usages = getAllWordUsages(word);
                boolean isUsed = !usages.isEmpty();

                // Disable "Delete" if word is used anywhere
                deleteItem.setEnabled(!isUsed);
            });
        });
    }

    private List<WordUsage> getAllWordUsages(Word targetWord) {
        List<WordUsage> usages = new ArrayList<>();
        if (adventureData == null || targetWord == null) {
            return usages;
        }

        // Check synonyms
        if (vocabularyData != null) {
            for (Word word : vocabularyData.getWords()) {
                if (word.getSynonym() != null && word.getSynonym().getId().equals(targetWord.getId())) {
                    usages.add(new WordUsage("Synonym", word.getText() + " (" + word.getType() + ")", "Word"));
                }
            }
        }

        // Check locations
        if (adventureData.getLocationData() != null) {
            for (LocationData location : adventureData.getLocationData().values()) {
                checkDescriptionUsage(location.getDescriptionData(), targetWord, "Location", location.getId(), usages);
                checkCommandProviderUsage(location.getCommandProviderData(), targetWord, "Location", location.getId(), usages);

                // Check directions in this location
                if (location.getDirectionsData() != null) {
                    for (DirectionData direction : location.getDirectionsData()) {
                        checkDescriptionUsage(direction.getDescriptionData(), targetWord, "Direction", direction.getId(), usages);
                        checkCommandProviderUsage(direction.getCommandProviderData(), targetWord, "Direction", direction.getId(), usages);
                        checkCommandDataUsage(direction.getCommandData(), targetWord, "Direction", direction.getId(), usages);
                    }
                }

                // Check items in this location
                if (location.getItemContainerData() != null && location.getItemContainerData().getItems() != null) {
                    for (ItemData item : location.getItemContainerData().getItems()) {
                        if (item != null) {
                            checkDescriptionUsage(item.getDescriptionData(), targetWord, "Item", item.getId(), usages);
                            checkCommandProviderUsage(item.getCommandProviderData(), targetWord, "Item", item.getId(), usages);
                        }
                    }
                }
            }
        }

        return usages;
    }

    private void checkDescriptionUsage(com.pdg.adventure.model.basic.DescriptionData description, Word targetWord, String type, String id, List<WordUsage> usages) {
        if (description == null) return;

        if (description.getAdjective() != null && description.getAdjective().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Adjective", id, type));
        }
        if (description.getNoun() != null && description.getNoun().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Noun", id, type));
        }
    }

    private void checkCommandProviderUsage(CommandProviderData commandProvider, Word targetWord, String type, String id, List<WordUsage> usages) {
        if (commandProvider == null || commandProvider.getAvailableCommands() == null) return;

        for (CommandChainData commandChain : commandProvider.getAvailableCommands().values()) {
            if (commandChain != null && commandChain.getCommands() != null) {
                for (CommandData command : commandChain.getCommands()) {
                    checkCommandDataUsage(command, targetWord, type, id, usages);
                }
            }
        }
    }

    private void checkCommandDataUsage(CommandData command, Word targetWord, String type, String id, List<WordUsage> usages) {
        if (command == null || command.getCommandDescription() == null) return;

        com.pdg.adventure.model.basic.CommandDescriptionData cmdDesc = command.getCommandDescription();
        if (cmdDesc.getVerb() != null && cmdDesc.getVerb().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Verb in Command", id, type));
        }
        if (cmdDesc.getAdjective() != null && cmdDesc.getAdjective().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Adjective in Command", id, type));
        }
        if (cmdDesc.getNoun() != null && cmdDesc.getNoun().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Noun in Command", id, type));
        }
    }

    // Helper class to store usage information
    private static class WordUsage {
        final String usageType;
        final String itemId;
        final String itemType;

        WordUsage(String usageType, String itemId, String itemType) {
            this.usageType = usageType;
            this.itemId = itemId;
            this.itemType = itemType;
        }
    }

    private void showWordUsages(DescribableWordAdapter wordAdapter) {
        Word word = wordAdapter.getWord();
        List<WordUsage> usages = getAllWordUsages(word);

        if (usages.isEmpty()) {
            Notification.show("Word '" + word.getText() + "' is not used anywhere.",
                    3000, Notification.Position.MIDDLE);
            return;
        }

        // Create a dialog to show the usages
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("The word '" + word.getText() + "' is used in the following places");
        dialog.setCancelable(true);
        dialog.setConfirmText("Close");

        // Build the message with grouped usages
        StringBuilder message = new StringBuilder();
        message.append("Found ").append(usages.size()).append(" usage(s):\n\n");

        // Group usages by type
        java.util.Map<String, List<WordUsage>> groupedUsages = new java.util.HashMap<>();
        for (WordUsage usage : usages) {
            String key = usage.itemType;
            groupedUsages.computeIfAbsent(key, k -> new ArrayList<>()).add(usage);
        }

        // Display grouped usages
        for (String groupType : groupedUsages.keySet()) {
            message.append("▶ ").append(groupType).append("s:\n");
            for (WordUsage usage : groupedUsages.get(groupType)) {
                message.append("  • ").append(usage.itemId);
                if (!usage.usageType.equals(groupType)) {
                    message.append(" (").append(usage.usageType).append(")");
                }
                message.append("\n");
            }
            message.append("\n");
        }

        dialog.setText(message.toString());
        dialog.open();
    }

    private void confirmDeleteWord(DescribableWordAdapter wordAdapter, GridListDataView<DescribableWordAdapter> dataView) {
        Word word = wordAdapter.getWord();

        // Check if word is used anywhere
        List<WordUsage> usages = getAllWordUsages(word);
        if (!usages.isEmpty()) {
            // Build detailed error message
            StringBuilder message = new StringBuilder();
            message.append("Cannot delete word '").append(word.getText())
                    .append("' because it is used in ").append(usages.size())
                    .append(" place(s):\n\n");

            // Group usages by type
            java.util.Map<String, Integer> usageCounts = new java.util.HashMap<>();
            for (WordUsage usage : usages) {
                String key = usage.itemType;
                usageCounts.put(key, usageCounts.getOrDefault(key, 0) + 1);
            }

            for (String type : usageCounts.keySet()) {
                message.append("• ").append(usageCounts.get(type)).append(" ").append(type);
                if (usageCounts.get(type) > 1) {
                    message.append("s");
                }
                message.append("\n");
            }

            message.append("\nPlease remove these references first or use \"Show Usages\" to see details.");

            Notification.show(message.toString(), 5000, Notification.Position.MIDDLE);
            return;
        }

        // Create confirmation dialog
        final var dialog = getConfirmDialog(wordAdapter, dataView, word);

        dialog.open();
    }

    private ConfirmDialog getConfirmDialog(final DescribableWordAdapter wordAdapter,
                                                    final GridListDataView<DescribableWordAdapter> dataView,
                                                    final Word word) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Word");
        dialog.setText("Are you sure you want to delete the word '" + word.getText() + "' (" + word.getType() + ")?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> {
            // Remove word from vocabulary
            vocabularyData.getWords().remove(word);

            // Save changes
            persistData();

            // Refresh the grid
            dataView.removeItem(wordAdapter);

            Notification.show("Word '" + word.getText() + "' deleted successfully.",
                    3000, Notification.Position.BOTTOM_START);
        });
        return dialog;
    }
}
