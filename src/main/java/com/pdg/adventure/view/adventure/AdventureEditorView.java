package com.pdg.adventure.view.adventure;

import com.vaadin.flow.component.Key;
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
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.item.AllItemsMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.message.MessagesMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;
import com.pdg.adventure.view.vocabulary.VocabularyMenuView;
import com.pdg.adventure.view.workflow.WorkflowEditorView;

@Route(value = "author/adventures/:adventureId/edit", layout = AdventuresMainLayout.class)
@RouteAlias(value = "author/adventures/new", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class AdventureEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(AdventureEditorView.class);

    private final Button saveButton = new Button("Save");
    private final Button testButton = new Button("Test");
    private final TextField startLocation;
    private final TextField numberOfLocations;
    private final TextField numberOfItems;
    private final Binder<AdventureData> binder;
    private final transient AdventureAccessService accessService;
    AdventureData adventureData;
    private String pageTitle;
    private boolean isNewAdventure;

    public AdventureEditorView(AdventureAccessService anAccessService) {

        accessService = anAccessService;
        binder = new Binder<>(AdventureData.class);

        Button editLocationsButton = new Button("Manage Locations");
        editLocationsButton.addClickListener(_ -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(LocationsMenuView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())));
            }
        });

        Button editVocabularyButton = new Button("Manage Vocabulary", _ -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(VocabularyMenuView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())));
            }
        });

        Button editMessagesButton = new Button("Manage Messages", _ -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(MessagesMenuView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())));
            }
        });

        Button editItemsButton = new Button("Manage Items", _ -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(AllItemsMenuView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())));
            }
        });

        Button workflowButton = new Button("Manage Workflow", _ -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(WorkflowEditorView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())));
            }
        });

        saveButton.setEnabled(false);
        saveButton.addClickListener(_ -> validateSave(adventureData));

        testButton.setEnabled(false);

        TextField adventureIdTF = getAdventureIdTF();
        TextField title = getTitleField();
        startLocation = getReadOnlyTextField("Start Location");
        numberOfLocations = getReadOnlyTextField("Total Locations");
        numberOfItems = getReadOnlyTextField("Total Items");
        HorizontalLayout titleStartRow = new HorizontalLayout(adventureIdTF, title,
                                                              startLocation,
                                                              numberOfLocations,
                                                              numberOfItems);
        TextArea longDescription = getNotesArea();

        setMargin(true);
        setPadding(true);

        final HorizontalLayout editRow = new HorizontalLayout(editVocabularyButton, editMessagesButton, editItemsButton,
                                                              editLocationsButton, workflowButton);

        Button backButton = new Button("Back", _ -> UI.getCurrent().navigate(AdventuresMenuView.class));
        backButton.addClickShortcut(Key.ESCAPE);
        final HorizontalLayout testSaveRow = new HorizontalLayout(backButton, testButton, saveButton);

        add(titleStartRow, longDescription, editRow, testSaveRow);
        setHorizontalComponentAlignment(Alignment.CENTER, testButton, saveButton);
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

            if (isNewAdventure) {
                accessService.createAdventure(adventureData, ViewSupporter.getCurrentUser());
                isNewAdventure = false;
            } else {
                accessService.saveAdventureData(adventureData, ViewSupporter.getCurrentUser());
            }
            saveButton.setEnabled(false);
        } catch (ValidationException ve) {
            LOG.error(ve.getMessage());
        } catch (RuntimeException e) {
            LOG.error(e.getMessage());
        }
    }

    private TextField getAdventureIdTF() {
        TextField field = new TextField("Adventure ID");
        field.setReadOnly(true);
        field.addValueChangeListener(_ -> checkIfSaveAvailable());
        binder.bind(field, AdventureData::getId, AdventureData::setId);
        return field;
    }

    private TextField getTitleField() {
        TextField field = new TextField("Title");
        field.setTooltipText("The title of this adventure.");
        field.setErrorMessage("The title is required");
        binder.forField(field).asRequired("You must provide a title.");
        binder.forField(field).bind(AdventureData::getTitle, AdventureData::setTitle);
        field.addValueChangeListener(_ -> checkIfSaveAvailable());
        return field;
    }

    private TextField getReadOnlyTextField(String aTitle) {
        TextField field = new TextField(aTitle);
        field.setReadOnly(true);
        return field;
    }

    private TextArea getNotesArea() {
        TextArea field = new TextArea("Notes");
        field.setWidth("95%");
        field.setMinHeight("200px");
        field.setMaxHeight("350px");
        field.setTooltipText("Use this to jot down notes about your adventure while developing it.");
        field.addValueChangeListener(_ -> checkIfSaveAvailable());
        binder.bind(field, AdventureData::getNotes, AdventureData::setNotes);
        return field;
    }

    private void checkIfSaveAvailable() {
        if (binder.validate().isOk()) {
            saveButton.setEnabled(!binder.getBean().getTitle().isEmpty());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> adventureId = event.getRouteParameters().get(RouteIds.ADVENTURE_ID.getValue());
        if (adventureId.isPresent()) {
            Optional<AdventureData> resolvedAdventure =
                    AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
            if (resolvedAdventure.isEmpty()) {
                return;
            }
            setUpLoadedAdventure(resolvedAdventure.get());
        } else {
            setUpNewEdit();
        }
        checkIfSaveAvailable();
    }

    private void setUpLoadedAdventure(AdventureData aLoadedAdventure) {
        adventureData = aLoadedAdventure;
        startLocation.setValue(ViewSupporter.getLocationsShortedDescription(
                adventureData.getLocationData().get(adventureData.getCurrentLocationId())));
        numberOfLocations.setValue(adventureData.getLocationData().size() + "");
        numberOfItems.setValue(ViewSupporter.getItemLocationPairs(adventureData.getLocationData().values()).size() + "");
        binder.setBean(adventureData);
        isNewAdventure = false;
        pageTitle = "Edit Adventure: " + adventureData.getTitle();
    }

    private void setUpNewEdit() {
        adventureData = new AdventureData();
        ItemContainerData playerPocket = adventureData.getPlayerPocket();
        playerPocket.getDescriptionData().setShortDescription("your pocket");
        playerPocket.setMaxSize(666);
        binder.setBean(adventureData);
        isNewAdventure = true;
        pageTitle = "A new adventure awaits!";
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
