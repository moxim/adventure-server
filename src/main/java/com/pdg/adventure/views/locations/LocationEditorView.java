package com.pdg.adventure.views.locations;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.pdg.adventure.model.Word.Type.ADJECTIVE;
import static com.pdg.adventure.model.Word.Type.NOUN;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.commands.CommandsMenuView;
import com.pdg.adventure.views.components.ResetBackSaveView;
import com.pdg.adventure.views.components.VocabularyPicker;
import com.pdg.adventure.views.components.VocabularyPickerField;
import com.pdg.adventure.views.directions.DirectionsMenuView;
import com.pdg.adventure.views.support.RouteIds;
import com.pdg.adventure.views.support.ViewSupporter;

@Route(value = "adventures/:adventureId/locations/:locationId/edit", layout = LocationsMainLayout.class)
@RouteAlias(value = "adventures/:adventureId/locations/new", layout = LocationsMainLayout.class)
public class LocationEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {
    private static final int MIN_LUMEN = 0;
    private static final int MAX_LUMEN = 100;
    private static final int LUMEN_STEP = 1;

    private final transient AdventureService adventureService;
    private final Binder<LocationViewModel> binder;
    private final VocabularyPicker nounSelection;
    private final VocabularyPicker adjectiveSelection;

    private final VocabularyPickerField nounPicker;

    private Button saveButton;
    private Button resetButton;
    private String pageTitle;

    private transient String locationId;
    private transient LocationData locationData;
    private transient LocationViewModel lvm;
    private transient AdventureData adventureData;

    @Autowired
    public LocationEditorView(AdventureService anAdventureService) {

        setSizeFull();

        adventureService = anAdventureService;
        binder = new Binder<>(LocationViewModel.class);

        locationData = new LocationData();
        locationId = locationData.getId();

        nounSelection = getWordBox("Noun", "The main theme of this location.");
        nounSelection.setPlaceholder("Select a noun (required)");
        adjectiveSelection = getWordBox("Adjective", "The qualifier for this location.");

        nounPicker = new VocabularyPickerField("Noun", "The main theme of this location.", NOUN, new VocabularyData());
        nounPicker.bind(binder, w -> "Noun is required", "noun");
        nounPicker.setPlaceholder("Select a noun (required)");

        TextField locationIdTF = getLocationIdTF();
        TextField adventureIdTF = getAdventureIdTF();
        TextArea shortDescription = getShortDescTextArea();
        TextArea longDescription = getLongDescTextArea();
        IntegerField lumen = getLumenField();
        IntegerField exits = getExitsField();

        final ResetBackSaveView resetBackSaveView = setUpNavidationButtons();

        // Bind fields
        binder.forField(nounSelection)
              .asRequired("Noun is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a noun with text")
              .bind(LocationViewModel::getNoun, LocationViewModel::setNoun);
        ViewSupporter.bindField(binder, adjectiveSelection, ADJECTIVE);
        binder.bind(shortDescription, LocationViewModel::getShortDescription, LocationViewModel::setShortDescription);
        binder.bind(longDescription, LocationViewModel::getLongDescription, LocationViewModel::setLongDescription);
        binder.bind(lumen, LocationViewModel::getLumen, LocationViewModel::setLumen);
        binder.bindReadOnly(locationIdTF, LocationViewModel::getId);
        binder.bindReadOnly(adventureIdTF, LocationViewModel::getAdventureId);
        binder.bindReadOnly(exits, LocationViewModel::getDefaultExits);

        binder.addStatusChangeListener(event -> {
            binder.addStatusChangeListener(e -> {
                saveButton.setEnabled(e.getBinder().hasChanges() && e.getBinder().isValid());
                resetButton.setEnabled(e.getBinder().hasChanges());
            });
            /* fix swivel */
//            boolean isValid = event.getBinder().isValid();
//            boolean hasChanges = event.getBinder().hasChanges();
//
//            saveButton.setEnabled(hasChanges && isValid);
//            resetButton.setEnabled(hasChanges);
            /* fix swivel */
        });

        HorizontalLayout h1 = new HorizontalLayout(adjectiveSelection, nounSelection, nounPicker);
        HorizontalLayout h2 = new HorizontalLayout(lumen, exits);

        VerticalLayout hl = new VerticalLayout(h1, h2);

        Button manageCommands = new Button("Manage Commands");
        manageCommands.addClickListener(event -> UI.getCurrent().navigate(CommandsMenuView.class,
                                                                          new RouteParameters(
                                                                                  new RouteParam(
                                                                                          RouteIds.LOCATION_ID.getValue(),
                                                                                          locationData.getId()),
                                                                                  new RouteParam(
                                                                                          RouteIds.ADVENTURE_ID.getValue(),
                                                                                          adventureData.getId()))
        ).ifPresent(e -> e.setData(adventureData, locationData)));

        Button manageItems = new Button("Manage Items");
        manageItems.setEnabled(false);

        Button manageExits = new Button("Manage Exits");
        manageExits.addClickListener(event -> UI.getCurrent().navigate(DirectionsMenuView.class,
                                                                       new RouteParameters(
                                                                               new RouteParam(
                                                                                       RouteIds.LOCATION_ID.getValue(),
                                                                                       locationData.getId()),
                                                                               new RouteParam(
                                                                                       RouteIds.ADVENTURE_ID.getValue(),
                                                                                       adventureData.getId()))
        ).ifPresent(e -> e.setData(adventureData, locationData)));

        setMargin(true);
        setPadding(true);

        HorizontalLayout commandRow = new HorizontalLayout(manageCommands, manageItems, manageExits);
        HorizontalLayout idRow = new HorizontalLayout(locationIdTF, adventureIdTF);
        add(idRow, hl, shortDescription, longDescription, commandRow, resetBackSaveView);
    }

    private ResetBackSaveView setUpNavidationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        saveButton = resetBackSaveView.getSave();
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);

        backButton.addClickListener(event -> UI.getCurrent().navigate(LocationsMenuView.class).ifPresent(
                                editor -> editor.setAdventureData(adventureData)));
        saveButton.addClickListener(event -> validateSave(lvm));
        resetButton.addClickListener(event -> binder.readBean(lvm));
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private IntegerField getExitsField() {
        IntegerField field = new IntegerField("Number of exits");
        field.setReadOnly(true);
        binder.bind(field, LocationViewModel::getDefaultExits, null);
        return field;
    }

    private IntegerField getLumenField() {
        IntegerField field = new IntegerField("Lighting (Lumen)");
        field.setMax(MAX_LUMEN);
        field.setMin(MIN_LUMEN);
        field.setStep(LUMEN_STEP);
        field.setTooltipText(
                "Set the lighting of this location. (" + MAX_LUMEN + " = max, " + MIN_LUMEN + " = total darkness)");
        field.setValueChangeMode(ValueChangeMode.EAGER);
//        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, LocationViewModel::getLumen, LocationViewModel::setLumen);
        return field;
    }

    private TextField getLocationIdTF() {
        TextField field = new TextField("Location ID");
        field.setReadOnly(true);
        binder.bindReadOnly(field, LocationViewModel::getId);
        return field;
    }

    private TextField getAdventureIdTF() {
        TextField field = new TextField("Adventure ID");
        field.setReadOnly(true);
        binder.bindReadOnly(field, LocationViewModel::getAdventureId);
        return field;
    }

    private void validateSave(LocationViewModel aLocationViewModel) {
        /* fix swivel */
//        try {
            binder.getBean();  // TODO: would mean to use new lvm after validate to save
//            binder.writeBean(aLocationViewModel);
            /* fix swivel */
            if (binder.validate().isOk()) {
                adventureService.saveLocationData(aLocationViewModel.getData());
                adventureData.getLocationData().put(aLocationViewModel.getId(), aLocationViewModel.getData());
                adventureService.saveAdventureData(adventureData);
                saveButton.setEnabled(false);
            }
            /* fix swivel */
//        } catch (ValidationException e) {
//            e.printStackTrace();
//        }
        /* fix swivel */
    }

    private TextArea getLongDescTextArea() {
        TextArea field = new TextArea("Long description");
        field.setWidth("95%");
        field.setMinHeight("200px");
        field.setMaxHeight("350px");
        field.setTooltipText("If left empty, this will be derived from the short description.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
//        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, LocationViewModel::getLongDescription,
                    LocationViewModel::setLongDescription);
        return field;
    }

    private TextArea getShortDescTextArea() {
        TextArea field = new TextArea("Short description");
        field.setWidth("95%");
        field.setMinHeight("100px");
        field.setMaxHeight("150px");
        field.setTooltipText("If left empty, this will be derived from the provided noun and verb.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        binder.bind(field, LocationViewModel::getShortDescription,
                    LocationViewModel::setShortDescription);
        return field;
    }

    private VocabularyPicker getWordBox(String label, String tooltipText) {
        VocabularyPicker wordBox = new VocabularyPicker(label);
        wordBox.setTooltipText(tooltipText);
        return wordBox;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalLocationId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue());
        if (optionalLocationId.isPresent()) {
            locationId = optionalLocationId.get();
            pageTitle = "Edit Location #" + locationId;
        } else {
            pageTitle = "New Location";
        }
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    public void setAdventureData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        locationData = adventureData.getLocationData().getOrDefault(locationId, new LocationData());
        locationData.setAdventure(adventureData);

        VocabularyData vocabularyData = adventureData.getVocabularyData();
        nounSelection.populate(vocabularyData.getWords(NOUN));
        adjectiveSelection.populate(vocabularyData.getWords(ADJECTIVE));
        nounPicker.populate(vocabularyData.getWords(NOUN));

        saveButton.setEnabled(false);
        lvm = new LocationViewModel(locationData);

        /* fix swivel */
                binder.setBean(lvm);
//        binder.readBean(lvm);
        /* fix swivel */

        //        binder.validate();
//        if (nounSelection.isInvalid()) {
//            nounSelection.focus(); // Draw user attention
//            nounSelection.setErrorMessage("you must select a noun");
//        }
    }

}
