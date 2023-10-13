package com.pdg.adventure.views.directions;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.commands.CommandsMenuView;
import com.pdg.adventure.views.support.ViewSupporter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Route(value = "adventures/:adventureId/locations/:locationId/direction/edit", layout = DirectionsMainLayout.class)
@RouteAlias(value = "adventures/directions",  layout = DirectionsMainLayout.class)
public class DirectionEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {
    private transient final AdventureService adventureService;
    private final Binder<DirectionData> binder;

    private final Button saveButton;
    private String pageTitle;

    private final TextField directionIdTF;
    private final TextField locationIdTF;
    private final TextField adventureIdTF;

    private final TextField verbTF;
    private final TextField nounTF;
    private final TextField adjectiveTF;
    private final TextArea shortDescriptionTA;
    private final TextArea longDescriptionTA;

    private final Grid<LocationData> grid = new Grid<>();

    private DirectionData directionData;
    private LocationData locationData;
    private AdventureData adventureData;


    // TODO:
    //  - select target location (detination) through grid(?)
    @Autowired
    public DirectionEditorView(AdventureService anAdventureService) {

        adventureService = anAdventureService;
        binder = new Binder<>(DirectionData.class);

        saveButton = new Button("Save");
        saveButton.addClickListener(e -> {
            validateSave(directionData);
        });

        directionIdTF = getDirectionIdTF();
        locationIdTF = getLocationIdTF();
        adventureIdTF = getAdventureIdTF();

        verbTF = getVerbTextField();
        nounTF = getNounTextField();
        adjectiveTF = getAdjectiveTextField();
        shortDescriptionTA = getShortDescTextArea();
        longDescriptionTA = getLongDescTextArea();

        Button resetButton = new Button("Reset", event -> binder.readBean(directionData));

        Div gridContainer = new Div();
        gridContainer.add(grid);
        gridContainer.setWidth("100%");
        gridContainer.setHeight("100%");

        HorizontalLayout hlTop = new HorizontalLayout(verbTF, adjectiveTF, nounTF);
        VerticalLayout vlBottom = new VerticalLayout(gridContainer);

        VerticalLayout hl = new VerticalLayout(hlTop, vlBottom);

        Button manageCommands = new Button("Manage Commands", event ->
                UI.getCurrent().navigate(CommandsMenuView.class,
                        new RouteParameters(
                                new RouteParam("locationId", locationData.getId()),
                                new RouteParam("adventureId", adventureData.getId()))
                )
        );
        Button backButton = new Button("Back", event ->
                UI.getCurrent().navigate(DirectionsMenuView.class,
                    new RouteParameters(
                            new RouteParam("adventureId", adventureData.getId()),
                            new RouteParam("locationId", locationData.getId()))
                    ).ifPresent(e -> e.setData(adventureData, locationData))
        );
        add(backButton);

        setMargin(true);
        setPadding(true);

        HorizontalLayout commandRow = new HorizontalLayout(manageCommands);
        HorizontalLayout idRow = new HorizontalLayout(locationIdTF, adventureIdTF);
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
        TextField locationIdTF = new TextField("Location ID");
        locationIdTF.setReadOnly(true);
        return locationIdTF;
    }

    private TextField getAdventureIdTF() {
        TextField adventureIdTF = new TextField("Adventure ID");
        adventureIdTF.setReadOnly(true);
        return adventureIdTF;
    }

//    private String getAdventuerId() {
//        return locationData.getAdventureId().getId();
//    }

//    public void loadDirection(String aDirectionId) {
//        directionData = adventureService.findLocationById(aDirectionId);
//    }

    private void validateSave(DirectionData aDirectionData) {
        try {
            BinderValidationStatus<DirectionData> status = binder.validate();

            if (status.hasErrors()) {
                throw new RuntimeException("Status Error: " + status.getValidationErrors());
            }

            binder.writeBean(aDirectionData);

            CommandData commandData = aDirectionData.getCommandData();
            if (commandData.getCommandDescription().getVerb().isEmpty()) {
                throw new RuntimeException("Alles Mist");
            }

            DirectionData localData = new DirectionData();
            binder.readBean(localData);
            System.out.println(localData);

            final Set<DirectionData> directionsData = locationData.getDirectionsData();
            System.out.println(locationData);
            CommandDescriptionData cdd = commandData.getCommandDescription();
            System.out.println(aDirectionData);
            System.out.println(commandData.getCommandDescription().getNoun());

//            locationData.getDirectionsData().getCommandProviderData().getAvailableCommands().put(cdd, null);

//            directionsData.setCommandProviderData(aDirectionData.getCommandDescriptionData());

            adventureService.saveLocationData(locationData);
            saveButton.setEnabled(false);
        } catch (ValidationException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private TextArea getLongDescTextArea() {
        TextArea field = new TextArea("Long description");
        field.setWidth("95%");
        field.setMinHeight("200px");
        field.setMaxHeight("350px");
        field.setTooltipText("If left empty, this will be derived from the short description.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, aDirectionData -> aDirectionData.getDescriptionData().getLongDescription(),
                    (aDirectionData, description) -> aDirectionData.getDescriptionData().setLongDescription(description));
        return field;
    }

    private TextArea getShortDescTextArea() {
        TextArea field = new TextArea("Short description");
        field.setWidth("95%");
        field.setMinHeight("100px");
        field.setMaxHeight("150px");
        field.setTooltipText("If left empty, this will be derived from the provided noun and verb.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, aDirectionData -> aDirectionData.getDescriptionData().getShortDescription(),
                    (aDirectionData, description) -> aDirectionData.getDescriptionData()
                                                                 .setShortDescription(description));
        return field;
    }

    private TextField getAdjectiveTextField() {
        TextField field = new TextField("Adjective");
        field.setTooltipText("The qualifier for this direction.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, aDirectionData -> aDirectionData.getCommandData().getCommandDescription().getAdjective(),
                    (aDirectionData, anAdjective) -> aDirectionData.getCommandData().getCommandDescription().setAdjective(anAdjective));
        return field;
    }

    private TextField getNounTextField() {
        TextField field = new TextField("Noun");
        field.setTooltipText("The noun for this direction.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, aDirectionData -> aDirectionData.getCommandData().getCommandDescription().getNoun(),
                    (aDirectionData, anNoun) -> aDirectionData.getCommandData().getCommandDescription().setNoun(anNoun));
        return field;
    }

    private TextField getVerbTextField() {
        TextField field = new TextField("Verb");
        field.setTooltipText("The main theme of this direction.");
        //        noun.setRequired(true);
        //        Label nounStatus = new Label();
        //        nounStatus.getStyle().setColor(SolidColor.RED);
        field.setValueChangeMode(ValueChangeMode.EAGER);
        binder.forField(field).asRequired("You must provide a verb.");
        binder.forField(field).bind(aDirectionData -> aDirectionData.getCommandData().getCommandDescription().getVerb(),
                    (aDirectionData, aVerb) -> aDirectionData.getCommandData().getCommandDescription().setVerb(aVerb));
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        return field;
    }

    private void checkIfSaveAvailable() {
        if (binder.validate().isOk()) {
            String verb = directionData.getCommandData().getCommandDescription().getVerb();
            // TODO: also check that the target location is not null
            saveButton.setEnabled(verb != null && !verb.isEmpty());
        } else {
            BinderValidationStatus<DirectionData> status = binder.validate();
            System.out.println("Validate is not OK");
            System.out.println("Status Error: " + status.getValidationErrors());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> directionId = event.getRouteParameters().get("directionId");
        if (directionId.isPresent()) {
            setUpLoading(directionId.get());
            setUpGUI();
        } else {
            setUpNewEdit();
        }

        Optional<String> advId = event.getRouteParameters().get("adventureId");
//        adventureData = adventureService.findAdventureById(advId.get());
//        locationData.setAdventureId(advId.get());

        directionIdTF.setValue(directionData.getId());
        binder.setBean(directionData);
        saveButton.setEnabled(false);
    }

    private void setUpGUI() {
        verbTF.setValue(directionData.getCommandData().getCommandDescription().getVerb());
        adjectiveTF.setValue(directionData.getCommandData().getCommandDescription().getAdjective());
        nounTF.setValue(directionData.getCommandData().getCommandDescription().getNoun());
        shortDescriptionTA.setValue(directionData.getDescriptionData().getShortDescription());
        longDescriptionTA.setValue(directionData.getDescriptionData().getLongDescription());
    }

    private void setUpNewEdit() {
        directionData = new DirectionData();
        pageTitle = "New Direction #" + directionData.getId();
    }

    private void setUpLoading(String aDirectionId) {
//        loadDirection(aDirectionId);
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
        locationIdTF.setValue(locationData.getId());
        adventureIdTF.setValue(locationData.getAdventure().getId());
        adventureData = anAdventureData;

        fillGUI();
    }

    private void fillGUI() {
        List<LocationData> locations = List.copyOf(adventureData.getLocationData());

        grid.setItems(locations);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(ViewSupporter::formatDescription).setHeader("Short Description").setSortable(true)
                .setAutoWidth(true);

        checkIfSaveAvailable();
    }

    public void setDirectionData(DirectionData aDirectionData) {
//        directionData.setLocationData(anAdventureData);
    }
}
