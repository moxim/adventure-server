package com.pdg.adventure.view.direction;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.pdg.adventure.model.Word.Type.*;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations/:locationId/direction/:directionId/edit", layout = DirectionsMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/locations/:locationId/direction/new", layout = DirectionsMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class DirectionEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {
    private static final Logger LOG = LoggerFactory.getLogger(DirectionEditorView.class);

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final Binder<DirectionViewModel> binder;
    private final VocabularyPickerField verbSelector;
    private final VocabularyPickerField nounSelector;
    private final VocabularyPickerField adjectiveSelector;
    private final Grid<LocationData> destinationGrid;

    private Button saveButton;
    private Button resetButton;
    private String pageTitle;

    private transient String directionId;
    private transient String selectedDestinationId;
    private transient DirectionData directionData;
    private transient DirectionViewModel dvm;
    private transient AdventureData adventureData;
    private transient LocationData locationData;

    public DirectionEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(DirectionViewModel.class);

        directionData = new DirectionData();
        directionId = directionData.getId();

        verbSelector = new VocabularyPickerField("Verb", "The action needed to follow this direction.");
        verbSelector.setPlaceholder("Select a verb (required)");
        adjectiveSelector = new VocabularyPickerField("Adjective", "The qualifier for this direction.");
        nounSelector = new VocabularyPickerField("Noun", "A descriptive noun for this direction.");

        TextField directionIdTF = getDirectionIdTF();
        TextField locationIdTF = getLocationIdTF();
        TextField adventureIdTF = getAdventureIdTF();
        TextArea shortDescription = getShortDescTextArea();
        TextArea longDescription = getLongDescTextArea();

        destinationGrid = new Grid<>();
        destinationGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        destinationGrid.addColumn(ViewSupporter::formatDescription).setHeader("Short Description").setSortable(true)
                       .setAutoWidth(true);
        destinationGrid.addSelectionListener(selectionEvent -> {
            selectedDestinationId = selectionEvent.getFirstSelectedItem().map(LocationData::getId).orElse(null);
        });
        destinationGrid.setSizeFull();

        Div gridContainer = new Div();
        gridContainer.add(destinationGrid);
        gridContainer.setWidth("90%");
        gridContainer.setMinHeight("200px");

        final ResetBackSaveView resetBackSaveView = setUpNavigationButtons();

        // Bind fields
        binder.forField(verbSelector).asRequired("Verb is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a verb with text")
              .bind(DirectionViewModel::getVerb, DirectionViewModel::setVerb);
        binder.forField(adjectiveSelector).bind(DirectionViewModel::getAdjective, DirectionViewModel::setAdjective);
        binder.forField(nounSelector).bind(DirectionViewModel::getNoun, DirectionViewModel::setNoun);
        binder.bind(shortDescription, DirectionViewModel::getShortDescription, DirectionViewModel::setShortDescription);
        binder.bind(longDescription, DirectionViewModel::getLongDescription, DirectionViewModel::setLongDescription);
        binder.bindReadOnly(directionIdTF, DirectionViewModel::getId);
        binder.bindReadOnly(locationIdTF, DirectionViewModel::getLocationId);
        binder.bindReadOnly(adventureIdTF, DirectionViewModel::getAdventureId);
        binder.forField(destinationGrid.asSingleSelect()).asRequired("Destination is required").bind(
                // Getter: convert destinationId String to LocationData object
                dvm -> {
                    String destId = dvm.getDestinationId();
                    if (destId == null || adventureData == null) return null;
                    return adventureData.getLocationData().get(destId);
                },
                // Setter: convert LocationData object to destinationId String
                (dvm, location) -> {
                    dvm.setDestinationId(location != null ? location.getId() : null);
                });

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
            resetButton.setEnabled(hasChanges);
        });

        HorizontalLayout h1 = new HorizontalLayout(verbSelector, adjectiveSelector, nounSelector);

        setMargin(true);
        setPadding(true);

        HorizontalLayout idRow = new HorizontalLayout(directionIdTF, locationIdTF, adventureIdTF);
        add(idRow, h1, gridContainer, shortDescription, longDescription, resetBackSaveView);
    }

    private TextField getDirectionIdTF() {
        TextField field = new TextField("Direction ID");
        field.setReadOnly(true);
        return field;
    }

    private TextField getLocationIdTF() {
        TextField field = new TextField("Location ID");
        field.setReadOnly(true);
        return field;
    }

    private TextField getAdventureIdTF() {
        TextField field = new TextField("Adventure ID");
        field.setReadOnly(true);
        return field;
    }

    private TextArea getShortDescTextArea() {
        TextArea field = new TextArea("Short description");
        field.setWidth("90%");
        field.setMinHeight("100px");
        field.setMaxHeight("150px");
        field.setTooltipText("If left empty, this will be derived from the provided noun and verb.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        return field;
    }

    private TextArea getLongDescTextArea() {
        TextArea field = new TextArea("Long description");
        field.setWidth("90%");
        field.setMinHeight("200px");
        field.setMaxHeight("350px");
        field.setTooltipText("If left empty, this will be derived from the short description.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        return field;
    }

    private ResetBackSaveView setUpNavigationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        saveButton = resetBackSaveView.getSave();
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);

        backButton.addClickListener(_ -> navigateBack());
        saveButton.addClickListener(_ -> validateSave(dvm));
        resetButton.addClickListener(_ -> binder.readBean(dvm));
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void navigateBack() {
        final UI ui = UI.getCurrent();
        if (ui != null && adventureData != null && locationData != null) {
            ui.navigate(DirectionsMenuView.class, new RouteParameters(
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId())));
        }
    }

    protected void validateSave(DirectionViewModel aDirectionViewModel) {
        if (binder.validate().isOk()) {
            askUserIfLocationLoopsAreOK(aDirectionViewModel);
        }
    }

    private void askUserIfLocationLoopsAreOK(DirectionViewModel aDirectionViewModel) {
        if (selectedDestinationId.equals(locationData.getId())) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Dangerous Destination");
            dialog.setText(
                    "The selected destination is the same as the current location. This will trap the player if they follow this direction. Do you want to proceed?");
            dialog.setCancelable(true);
            dialog.setConfirmText("Proceed");
            dialog.addConfirmListener(_ -> {
                // UserData confirmed, do nothing and allow save to proceed
                saveData(aDirectionViewModel);
            });
            dialog.addCancelListener(_ -> {
                // UserData cancelled, throw an exception to prevent save
                destinationGrid.select(null);
            });
            dialog.open();
        } else {
            // Destination is fine, proceed with save
            saveData(aDirectionViewModel);
        }
    }

    private void saveData(DirectionViewModel aDirectionViewModel) {
        try {
            binder.writeBean(aDirectionViewModel);
            final DirectionData directionData = aDirectionViewModel.getData();
            final DescriptionData locationDescriptionData = locationData.getDescriptionData();
            final DescriptionData directionDescriptionData = directionData.getDescriptionData();
            directionDescriptionData.setAdjective(locationDescriptionData.getAdjective());
            directionDescriptionData.setNoun(locationDescriptionData.getNoun());

            final MovePlayerActionData movePlayerActionData = new MovePlayerActionData();
            movePlayerActionData.setLocationId(aDirectionViewModel.getDestinationId());
            directionData.getCommandData().setActions(new ArrayList<>(List.of(movePlayerActionData)));

            final Set<DirectionData> directionsData = locationData.getDirectionsData();
            directionsData.add(directionData);

            adventureService.saveLocationData(locationData);

            navigateBack();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocationOrForward(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            return;
        }
        final Optional<String> optionalDirectionId = event.getRouteParameters().get(RouteIds.DIRECTION_ID.getValue());
        optionalDirectionId.ifPresent(id -> directionId = id);
        setData(resolvedLocation.get(), resolvedAdventure.get());
        pageTitle = optionalDirectionId.isPresent()
                ? "Edit Direction: " + ViewSupporter.formatDescription(directionData.getDescriptionData())
                : "New Direction";
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    private void setData(LocationData aLocationData, AdventureData anAdventureData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

        directionData = locationData.getDirectionsData().stream()
                                    .filter(direction -> direction.getId().equals(directionId)).findFirst()
                                    .orElse(new DirectionData());

        VocabularyData vocabularyData = adventureData.getVocabularyData();
        verbSelector.populate(vocabularyData.getWords(VERB));
        adjectiveSelector.populate(vocabularyData.getWords(ADJECTIVE));
        nounSelector.populate(vocabularyData.getWords(NOUN));

        // Set up destination grid
        // TODO: should we really filter out the current location?
        Predicate<? super LocationData> predicate = loc -> !loc.getId().equals(locationData.getId());
        List<LocationData> locations = adventureData.getLocationData().values().stream()
//                .filter(predicate)
                                                    .toList();

        destinationGrid.setItems(locations);

        // Select current destination if exists
        if (directionData.getDestinationId() != null) {
            LocationData destination = adventureData.getLocationData().get(directionData.getDestinationId());
            if (destination != null) {
                destinationGrid.select(destination);
                selectedDestinationId = destination.getId();
            }
        }

        saveButton.setEnabled(false);
        dvm = new DirectionViewModel(directionData);
        dvm.setLocationId(locationData.getId());
        dvm.setAdventureId(adventureData.getId());

        binder.readBean(dvm);
    }

    // for testing purposes
    protected void setUpLoading(String s) {
        directionId = s;
    }

    // for testing purposes
    protected DirectionViewModel getViewModel() {
        return dvm;
    }
}
