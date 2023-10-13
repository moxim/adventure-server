package com.pdg.adventure.views.adventure;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.locations.LocationsMenuView;
import com.pdg.adventure.views.vocabulary.VocabularyMenuView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "adventures/:adventureId/edit", layout = AdventuresMainLayout.class)
public class AdventureEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private final Button saveButton = new Button("Save");
    private final Button testButton = new Button("Test");
    private String pageTitle;

    private final Binder<AdventureData> binder;
    private transient final AdventureService adventureService;

    AdventureData adventureData;

    @Autowired
    public AdventureEditorView(AdventureService anAdventureService) {

        adventureService = anAdventureService;
        binder = new Binder<>(AdventureData.class);

        Button editLocationsButton = new Button("Manage Locations");
        editLocationsButton.addClickListener(event -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent()
                  .navigate(LocationsMenuView.class
//                          , new RouteParameters(
//                      new RouteParam("adventureId", adventureData.getId())));
//            }});
                  ).ifPresent(editor -> editor.setAdventureData(adventureData));
        }});

        Button editVocabularyButton = new Button("Manage Vocabulary", e -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(VocabularyMenuView.class,
                                new RouteParameters(
                                        new RouteParam("adventureId", adventureData.getId())))
                        .ifPresent(editor -> editor.setAdventureData(adventureData));
            }
        });

        Button workflowButton = new Button("Manage Workflow");
        workflowButton.setEnabled(false);

        saveButton.setEnabled(false);
        saveButton.addClickListener(e -> {
            validateSave(adventureData);
        });

        testButton.setEnabled(false);

        TextField adventureIdTF = getAdventureIdTF();
        TextField title = getTitleField();
        TextArea longDescription = getNotesArea();

        setMargin(true);
        setPadding(true);

        final HorizontalLayout editRow = new HorizontalLayout(editVocabularyButton, editLocationsButton, workflowButton);
        final HorizontalLayout testSaveRow = new HorizontalLayout(testButton, saveButton);

        add(adventureIdTF, title, longDescription, editRow, testSaveRow);
        setHorizontalComponentAlignment(Alignment.CENTER, testButton, saveButton);
    }

    private TextField getAdventureIdTF() {
        TextField field = new TextField("Adventure ID");
        field.setReadOnly(true);
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, AdventureData::getId, AdventureData::setId);
        return field;
    }

    public void loadAdventure(String aLocationId) {
        adventureData = adventureService.findAdventureById(aLocationId);
        binder.setBean(adventureData);
    }

    private void validateSave(AdventureData adventureData) {
        try {
            binder.writeBean(adventureData);
            BinderValidationStatus<AdventureData> status = binder.validate();

            if (status.hasErrors()) {
                throw new RuntimeException("Status Error: " + status.getValidationErrors());
            }

            if (adventureData.getTitle().isEmpty()) {
                throw new RuntimeException("Alles Mist");
            }

            adventureService.saveAdventureData(adventureData);
            saveButton.setEnabled(false);
        } catch (ValidationException ve) {
            ve.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private TextArea getNotesArea() {
        TextArea field = new TextArea("Notes");
        field.setWidth("95%");
        field.setMinHeight("200px");
        field.setMaxHeight("350px");
        field.setTooltipText("Use this to jot down notes about your adventure while developing it.");
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, AdventureData::getNotes, AdventureData::setNotes);
        return field;
    }

    private TextField getTitleField() {
        TextField field = new TextField("Title");
        field.setTooltipText("The title of this adventure.");
        field.setErrorMessage("The title is required");
        binder.forField(field).asRequired("You must provide a title.");
        binder.forField(field).bind(AdventureData::getTitle, AdventureData::setTitle);
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        return field;
    }

    private void checkIfSaveAvailable() {
        if (binder.validate().isOk()) {
            saveButton.setEnabled(!binder.getBean().getTitle().isEmpty());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> adventureId = event.getRouteParameters().get("adventureId");
        if (adventureId.isPresent() && !adventureId.equals("new")) {
            setUpLoading(adventureId.get());
        } else {
            setUpNewEdit();
        }
        checkIfSaveAvailable();
    }

    private void setUpNewEdit() {
        adventureData = new AdventureData();
        binder.setBean(adventureData);
        pageTitle = "New Adventure #" + adventureData.getId();
    }

    private void setUpLoading(String anAdventureId) {
        loadAdventure(anAdventureId);
        pageTitle = "Edit Adventure #" + anAdventureId;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }
}
