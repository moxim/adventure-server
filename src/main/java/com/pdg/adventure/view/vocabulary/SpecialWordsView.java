package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.pdg.adventure.model.Word.Type.VERB;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "adventures/:adventureId/vocabulary/special", layout = VocabularyMainLayout.class)
@PageTitle("Special Words")
public class SpecialWordsView extends VerticalLayout implements SaveListener, GuiListener {

    private transient final AdventureService adventureService;
    private AdventureData adventureData;
    private VocabularyData vocabularyData;
    private transient WordUsageTracker wordUsageTracker;

    private Button back;
    private Button save;
    private VocabularyPickerField takeSelector;
    private VocabularyPickerField dropSelector;

    @Autowired
    public SpecialWordsView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        setSizeFull();
        createGUI();
    }

    private void createGUI() {
        NativeLabel titleLabel = new NativeLabel("Special Words");
        titleLabel.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("margin-bottom", "var(--lumo-space-m)");

        NativeLabel descriptionLabel = new NativeLabel(
                "Configure special words that are used throughout the adventure for common actions.");
        descriptionLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "var(--lumo-space-l)");

        takeSelector = new VocabularyPickerField("Taker");
        takeSelector.setHelperText(
                "A verb used for taking items. Can be changed unless used as a command in any item.");
        takeSelector.setWidth("400px");
        takeSelector.addValueChangeListener(event -> {
            Word newValue = event.getValue();
            if (newValue != null && !event.isFromClient()) {
                // Programmatic change, allow it
                return;
            }

            Word oldValue = event.getOldValue();

            // Check if trying to clear or change an existing value
            if (checkIfValueAlreadyExists(oldValue, newValue, "Take", takeSelector)) return;

            // Allow setting or changing the value
            vocabularyData.setTakeWord(newValue);
        });

        dropSelector = new VocabularyPickerField("Dropper");
        dropSelector.setHelperText(
                "A verb used for dropping items. Can be changed unless used as a command in any item.");
        dropSelector.setWidth("400px");
        dropSelector.addValueChangeListener(event -> {
            Word newValue = event.getValue();
            if (newValue != null && !event.isFromClient()) {
                // Programmatic change, allow it
                return;
            }

            Word oldValue = event.getOldValue();

            // Check if trying to clear or change an existing value
            if (checkIfValueAlreadyExists(oldValue, newValue, "Drop", dropSelector)) return;

            // Allow setting or changing the value
            vocabularyData.setDropWord(newValue);
        });

        back = new Button("Back", event -> {
            UI.getCurrent().navigate(VocabularyMenuView.class,
                    new RouteParameters(
                            new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))
            ).ifPresent(editor -> editor.setAdventureData(adventureData));
        });
        back.addClickShortcut(Key.ESCAPE);

        save = new Button("Save", event -> {
            persistData();
            Notification.show("Special words saved", 2000, Notification.Position.BOTTOM_START);
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(back, save);
        buttonLayout.setSpacing(true);

        VerticalLayout mainLayout = new VerticalLayout(
                titleLabel,
                descriptionLabel,
                takeSelector,
                dropSelector,
                buttonLayout
        );
        mainLayout.setMaxWidth("600px");
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        add(mainLayout);
    }

    private boolean checkIfValueAlreadyExists(final Word oldValue, final Word newValue, final String wordType,
                                              final VocabularyPickerField selector) {
        if (oldValue != null && (newValue == null || !oldValue.getId().equals(newValue.getId()))) {
            // Check if the old value is used in any item commands
            List<WordUsage> usages = wordUsageTracker.checkWordIsNotUsedInLocations(oldValue);
            if (!usages.isEmpty()) {
                showWordIsBusyNotification(wordType, newValue, oldValue, usages);
                selector.setValue(oldValue);
                return true;
            }
        }
        return false;
    }

    private void showWordIsBusyNotification(final String aWordType, final Word newValue, final Word oldValue,
                                            final List<WordUsage> aUsageList) {
        String action = newValue == null ? "cleared" : "changed";
        String notificationText = aWordType + " verb '" + oldValue.getText() + "' cannot be " + action +
                " because it is used as a command in one or more items" +
                wordUsageTracker.createUsagesText(aUsageList, 5);

        Notification.show(notificationText, 4000, Notification.Position.MIDDLE);
    }

    public void setAdventureData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        vocabularyData = adventureData.getVocabularyData();
        wordUsageTracker = new WordUsageTracker(adventureData, vocabularyData);

        updateGui();
    }

    @Override
    public void updateGui() {
        takeSelector.populate(
                vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());
        if (vocabularyData.getTakeWord() != null) {
            takeSelector.setValue(vocabularyData.getTakeWord());
        }

        dropSelector.populate(
                vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());
        if (vocabularyData.getDropWord() != null) {
            dropSelector.setValue(vocabularyData.getDropWord());
        }
    }

    @Override
    public void persistData() {
        adventureService.saveAdventureData(adventureData);
    }
}
