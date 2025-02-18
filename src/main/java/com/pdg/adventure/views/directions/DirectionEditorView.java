package com.pdg.adventure.views.directions;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.pdg.adventure.model.Word.Type.*;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.commands.CommandsMenuView;
import com.pdg.adventure.views.support.ViewSupporter;

@Route(value = "adventures/:adventureId/locations/:locationId/direction/:directionId/edit", layout = DirectionsMainLayout.class)
@RouteAlias(value = "adventures/:adventureId/locations/:locationId/direction/new",  layout = DirectionsMainLayout.class)
public class DirectionEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {
    private static final String ADVENTURE_ID = "adventureId";

    private final transient AdventureService adventureService;
    private final Binder<DirectionData> binder;

    private final Button saveButton;
    private String pageTitle;

    private final TextField directionIdTF;
    private final TextField locationIdTF;
    private final TextField adventureIdTF;

    private final ComboBox<String> verbEntry;
    private final ComboBox<String> nounEntry;
    private final ComboBox<String> adjectiveEntry;
    private final TextArea shortDescriptionTA;
    private final TextArea longDescriptionTA;

    private final Grid<LocationData> grid;

    private transient AdventureData adventureData;
    private transient VocabularyData vocabulary;
    private transient LocationData locationData;
    private transient DirectionData directionData;
    private transient Optional<LocationData> targetLocation;
    private transient String directionId;

    private MapperSupporter mapperSupporter;

    @Autowired
    public DirectionEditorView(AdventureService anAdventureService) {

        adventureService = anAdventureService;
        binder = new Binder<>(DirectionData.class);

        saveButton = new Button("Save");
        saveButton.addClickListener(e -> validateSave(directionData));

        directionIdTF = getDirectionIdTF();
        locationIdTF = getLocationIdTF();
        adventureIdTF = getAdventureIdTF();

        verbEntry = getVerbTextField();
        nounEntry = getNounTextField();
        adjectiveEntry = getAdjectiveTextField();
        shortDescriptionTA = getShortDescTextArea();
        longDescriptionTA = getLongDescTextArea();

        targetLocation = Optional.empty();

        Button resetButton = new Button("Reset", event -> binder.readBean(directionData));

        grid = new Grid<>();
        SelectionListener<Grid<LocationData>, LocationData> listener  = selectionEvent -> {
            targetLocation = selectionEvent.getFirstSelectedItem();
            if (targetLocation.isEmpty()) {
                return;
            }
            directionData.setDestinationId(targetLocation.get().getId());
            checkIfSaveAvailable();
        };
        grid.addSelectionListener(listener);

        Div gridContainer = new Div();
        gridContainer.add(grid);
        gridContainer.setWidth("100%");
        gridContainer.setHeight("100%");

        HorizontalLayout hlTop = new HorizontalLayout(verbEntry, adjectiveEntry, nounEntry);
        VerticalLayout vlBottom = new VerticalLayout(gridContainer);

        VerticalLayout hl = new VerticalLayout(hlTop, vlBottom);

        Button manageCommands = new Button("Manage Commands", event ->
                UI.getCurrent().navigate(CommandsMenuView.class,
                        new RouteParameters(
                                new RouteParam("locationId", locationData.getId()),
                                new RouteParam(ADVENTURE_ID, adventureData.getId()))
                )
        );
        Button backButton = new Button("Back", event ->
                UI.getCurrent().navigate(DirectionsMenuView.class,
                    new RouteParameters(
                            new RouteParam(ADVENTURE_ID, adventureData.getId()),
                            new RouteParam("locationId", locationData.getId()))
                    ).ifPresent(e -> e.setData(adventureData, locationData))
        );
        backButton.addClickShortcut(Key.ESCAPE);
        add(backButton);

        setMargin(true);
        setPadding(true);

        HorizontalLayout commandRow = new HorizontalLayout(manageCommands);
        HorizontalLayout idRow = new HorizontalLayout(directionIdTF, locationIdTF, adventureIdTF);
        HorizontalLayout mainRow = new HorizontalLayout(resetButton, backButton, saveButton);
        add(idRow, hl, shortDescriptionTA, longDescriptionTA, commandRow, mainRow);
    }

    private TextField getDirectionIdTF() {
        TextField field = new TextField("Direction ID");
        field.setReadOnly(true);
        binder.bind(field, DirectionData::getId, DirectionData::setId);
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

    private void validateSave(DirectionData aDirectionData) {
        binder.setBean(aDirectionData);

        BinderValidationStatus<DirectionData> status = binder.validate();

        if (status.hasErrors()) {
            throw new RuntimeException("Status Error: " + status.getValidationErrors());
        }

        CommandData//<MovePlayerActionData>
                commandData = aDirectionData.getCommandData();
        if (commandData.getCommandDescription().getVerb() == null) {
            throw new RuntimeException("666 - Holy crap!");
        }

        // TODO: fill this in
        final MovePlayerActionData movePlayerActionData = new MovePlayerActionData();
        movePlayerActionData.setLocationId(aDirectionData.getDestinationId());
        commandData.setAction(movePlayerActionData);

        final Set<DirectionData> directionsData = locationData.getDirectionsData();
        directionsData.add(aDirectionData);

        adventureService.saveLocationData(locationData);
        saveButton.setEnabled(false);
    }

    private TextArea getLongDescTextArea() {
        TextArea field = new TextArea("Long description");
        field.setWidth("95%");
        field.setMinHeight("200px");
        field.setMaxHeight("350px");
        field.setTooltipText("If left empty, this will be derived from the short description.");
        return field;
    }

    private TextArea getShortDescTextArea() {
        TextArea field = new TextArea("Short description");
        field.setWidth("95%");
        field.setMinHeight("100px");
        field.setMaxHeight("150px");
        field.setTooltipText("If left empty, this will be derived from the provided noun and verb.");
        return field;
    }

    private ComboBox<String> getAdjectiveTextField() {
        ComboBox<String> field = new ComboBox<>("Adjective");
        field.setTooltipText("The qualifier for this direction.");
        return field;
    }

    private ComboBox<String> getNounTextField() {
        ComboBox<String> field = new ComboBox<>("Noun");
        field.setTooltipText("The noun for this direction.");
        return field;
    }

    private ComboBox<String> getVerbTextField() {
        ComboBox<String> field = new ComboBox<>("Verb");
        field.setHelperText("Provide at least a verb and a location to set up a direction.");
        field.setTooltipText("The main theme of this direction.");
        field.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                checkIfSaveAvailable();
            }
        });
        return field;
    }


    private void checkIfSaveAvailable() {
        try {
            binder.writeBean(directionData);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Word verb = directionData.getCommandData().getCommandDescription().getVerb();
        if (binder.validate().isOk()) {
            saveButton.setEnabled(targetLocation.isPresent() && verb != null && !verb.getText().isBlank());
        } else {
            BinderValidationStatus<DirectionData> status = binder.validate();
            System.out.println("Validate is not OK");
            System.out.println("Status Error: " + status.getValidationErrors());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> providedDirectionId = event.getRouteParameters().get("directionId");
        if (providedDirectionId.isPresent()) {
            setUpLoading(providedDirectionId.get());
        } else {
            setUpNewEdit();
        }
        binder.setBean(directionData);

        saveButton.setEnabled(false);
    }

    private void setUpNewEdit() {
        directionData = new DirectionData();
        directionId = directionData.getId();
        pageTitle = "New Direction";
    }

    private void setUpLoading(String aDirectionId) {
        directionId = aDirectionId;
        pageTitle = "Edit Direction #" + aDirectionId;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    public void setData(LocationData aLocationData, AdventureData anAdventureData) {
        locationData = aLocationData;
        adventureData = anAdventureData;
        locationIdTF.setValue(locationData.getId());
        adventureIdTF.setValue(adventureData.getId());
        vocabulary = adventureData.getVocabularyData();

        if (directionData == null) {
            directionData = locationData.getDirectionsData().stream().filter(direction -> direction.getId().equals(directionId)).findFirst().orElseThrow();
        }

        fillGUI();
    }

    private void fillGUI() {
        Predicate<? super LocationData> predicate = aLocationData -> !(aLocationData.getId().equals(locationData.getId()));
        List<LocationData> locations = List.copyOf(adventureData.getLocationData().values().
                stream().toList().stream().filter(predicate).toList());

        grid.setItems(locations);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(ViewSupporter::formatDescription).setHeader("Short Description").setSortable(true)
                .setAutoWidth(true);

        List<String> verbs = new ArrayList<>();
        List<String> adjectives = new ArrayList<>();
        List<String> nouns = new ArrayList<>();

        for (Word word : vocabulary.getWords()) {
            switch(word.getType()) {
                case NOUN -> nouns.add(word.getText());
                case ADJECTIVE -> adjectives.add(word.getText());
                case VERB -> verbs.add(word.getText());
            }
        }

        verbEntry.setItems(verbs);
        nounEntry.setItems(nouns);
        adjectiveEntry.setItems(adjectives);

        locationIdTF.setHelperText(locationData.getDescriptionData().getShortDescription());
        directionIdTF.setValue(directionData.getId());

        setUpGUI();
        setUpBindings();

        checkIfSaveAvailable();
    }

    private void setUpGUI() {
        CommandDescriptionData commandDescriptionData = directionData.getCommandData().getCommandDescription();
        verbEntry.setValue(ViewSupporter.getWordText(ViewSupporter.getWord(commandDescriptionData, VERB)));
        adjectiveEntry.setValue(ViewSupporter.getWordText(ViewSupporter.getWord(commandDescriptionData, ADJECTIVE)));
        nounEntry.setValue(ViewSupporter.getWordText(ViewSupporter.getWord(commandDescriptionData, NOUN)));
        shortDescriptionTA.setValue(directionData.getDescriptionData().getShortDescription());
        longDescriptionTA.setValue(directionData.getDescriptionData().getLongDescription());
        grid.select(adventureData.getLocationData().get(directionData.getDestinationId()));
    }

    private void setUpBindings() {
        binder.forField(verbEntry).asRequired("You must provide a verb.");
        ViewSupporter.bindField(binder, verbEntry, vocabulary, VERB, directionData.getCommandData().getCommandDescription());
        ViewSupporter.bindField(binder, nounEntry, vocabulary, ADJECTIVE, directionData.getCommandData().getCommandDescription());
        ViewSupporter.bindField(binder, adjectiveEntry, vocabulary, NOUN, directionData.getCommandData().getCommandDescription());
        binder.bind(longDescriptionTA, aDirectionData -> aDirectionData.getDescriptionData().getLongDescription(),
                    (aDirectionData, description) -> aDirectionData.getDescriptionData().setLongDescription(description));
        binder.bind(shortDescriptionTA, aDirectionData -> aDirectionData.getDescriptionData().getShortDescription(),
                    (aDirectionData, description) -> aDirectionData.getDescriptionData()
                                                                 .setShortDescription(description));
    }

    public void setDirectionData(DirectionData aDirectionData) {
//        directionData.setLocationData(anAdventureData);
    }
}
