package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.pdg.adventure.model.Word.Type.VERB;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "author/adventures/:adventureId/vocabulary/special", layout = VocabularyMainLayout.class)
@PageTitle("Special Words")
@RolesAllowed("ROLE_AUTHOR")
public class SpecialWordsView extends VerticalLayout implements SaveListener, GuiListener, BeforeEnterObserver {

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private AdventureData adventureData;
    private VocabularyData vocabularyData;
    private transient WordUsageTracker wordUsageTracker;

    private Button back;
    private Button save;
    private VocabularyPickerField takeSelector;
    private VocabularyPickerField dropSelector;
    private VocabularyPickerField loadSelector;
    private VocabularyPickerField examineSelector;

    public SpecialWordsView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        setSizeFull();
        createGUI();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        setAdventureData(resolvedAdventure.get());
    }

    private void createGUI() {
        NativeLabel titleLabel = new NativeLabel("Special Words");
        titleLabel.getStyle().set("font-weight", "bold").set("font-size", "var(--lumo-font-size-xl)")
                  .set("margin-bottom", "var(--lumo-space-m)");

        NativeLabel descriptionLabel = new NativeLabel(
                "Configure special words that are used throughout the adventure for common actions.");
        descriptionLabel.getStyle().set("color", "var(--lumo-secondary-text-color)")
                        .set("margin-bottom", "var(--lumo-space-l)");

        createSpecialWordFields();

        back = new Button("Back", _ -> {
            UI.getCurrent().navigate(VocabularyMenuView.class, new RouteParameters(
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setAdventureData(adventureData));
        });
        back.addClickShortcut(Key.ESCAPE);

        save = new Button("Save", _ -> {
            persistData();
            Notification notification = Notification.show("Special words saved", 2000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(back, save);
        buttonLayout.setSpacing(true);

        VerticalLayout mainLayout = new VerticalLayout(titleLabel, descriptionLabel, takeSelector, dropSelector,
                                                       loadSelector, examineSelector, buttonLayout);
        mainLayout.setMaxWidth("600px");
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        add(mainLayout);
    }

    private void createSpecialWordFields() {
        takeSelector = createSelector("Taker", "Take", word -> vocabularyData.setTakeWord(word),
                                      "A verb used for taking items. Can be changed unless used as a command in any item.");

        dropSelector = createSelector("Dropper", "Drop", word -> vocabularyData.setDropWord(word),
                                      "A verb used for dropping items. Can be changed unless used as a command in any item.");

        loadSelector = createSelector("Loader", "Load", word -> vocabularyData.setLoadWord(word),
                                      "A verb for loading adventures. Can be changed unless used as a command.");

        examineSelector = createSelector("Examiner", "Examine", word -> vocabularyData.setExamineWord(word),
                                         "A verb used for examining things in detail. Can be changed unless used as a command.");
    }

    @Override
    public void persistData() {
        adventureService.saveAdventureData(adventureData);
    }

    private VocabularyPickerField createSelector(String aLabel, String aWordType, Consumer<Word> aWordSetter,
                                                 String aHelperText) {

        VocabularyPickerField selector = new VocabularyPickerField(aLabel);
        selector.setHelperText(aHelperText);
        selector.setWidth("400px");
        selector.addValueChangeListener(event -> {
            Word newValue = event.getValue();
            if (newValue != null && !event.isFromClient()) {
                // Programmatic change, allow it
                return;
            }

            Word oldValue = event.getOldValue();

            // Check if trying to clear or change an existing value
            if (checkIfValueAlreadyExists(oldValue, newValue, aWordType, selector)) return;

            // Allow setting or changing the value
            if (vocabularyData != null) {
                aWordSetter.accept(newValue);
            }
        });

        return selector;
    }

    private boolean checkIfValueAlreadyExists(final Word oldValue, final Word newValue, final String wordType,
                                              final VocabularyPickerField selector) {
        if (oldValue != null && (newValue == null || !oldValue.getId().equals(newValue.getId()))) {
            // Check if the old value is used in any item commands
            List<WordUsage> usages = wordUsageTracker.findWordUsagesInLocations(oldValue);
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
                                  " because it is used in a command for one or more items" +
                                  wordUsageTracker.createUsagesText(aUsageList, 5);

        Notification notification = Notification.show(notificationText, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public void setAdventureData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        vocabularyData = adventureData.getVocabularyData();
        wordUsageTracker = new WordUsageTracker(adventureData, vocabularyData);

        updateGui();
    }

    @Override
    public void updateGui() {
        populateSpecialWordSelector(takeSelector, vocabularyData.getTakeWord());
        populateSpecialWordSelector(dropSelector, vocabularyData.getDropWord());
        populateSpecialWordSelector(loadSelector, vocabularyData.getLoadWord());
        populateSpecialWordSelector(examineSelector, vocabularyData.getExamineWord());
    }

    private void populateSpecialWordSelector(VocabularyPickerField selector, Word currentWord) {
        selector.populate(vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());
        if (currentWord != null) {
            selector.setValue(currentWord);
        }
    }
}
