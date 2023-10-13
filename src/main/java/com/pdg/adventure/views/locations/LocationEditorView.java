package com.pdg.adventure.views.locations;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.PaperSlider;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.commands.CommandsMenuView;
import com.pdg.adventure.views.directions.DirectionsMenuView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "adventures/:adventureId/locations/:locationId/edit", layout = LocationsMainLayout.class)
public class LocationEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {
    private transient final AdventureService adventureService;
    private final Binder<LocationData> binder;

    private Button saveButton;
    private String pageTitle;

    private LocationData locationData;
    private AdventureData adventureData;

    @Autowired
    public LocationEditorView(AdventureService anAdventureService) {

        adventureService = anAdventureService;
        binder = new Binder<>(LocationData.class);

        saveButton = new Button("Save");
        saveButton.addClickListener(e -> {
            validateSave(locationData);
        });

        TextField locationIdTF = getLocationIdTF();
        TextField adventureIdTF = getAdventureIdTF();
        TextField noun = getNounTextField();
        TextField adjective = getAdjectiveTextField();
        TextArea shortDescription = getShortDescTextArea();
        TextArea longDescription = getLongDescTextArea();
        IntegerField lumen = getLumenField();
        Button resetButton = new Button("Reset", event -> binder.readBean(locationData));

        // PaperSlider lumen = getSlider();
        // Slider lumen = new Slider();

        HorizontalLayout h1 = new HorizontalLayout(noun, adjective);
        HorizontalLayout h2 = new HorizontalLayout(lumen);

        VerticalLayout hl = new VerticalLayout(h1, h2);

        Button manageCommands = new Button("Manage Commands", event -> UI.getCurrent().navigate(CommandsMenuView.class,
            new RouteParameters(
                new RouteParam("locationId", locationData.getId()),
                new RouteParam("adventureId", adventureIdTF.getValue()))
            )
        );

        Button manageItems = new Button("Manage Items");
        manageItems.setEnabled(false);

        Button manageExits = new Button("Manage Exits", event -> UI.getCurrent().navigate(DirectionsMenuView.class,
             new RouteParameters(
                new RouteParam("locationId", locationData.getId()),
                new RouteParam("adventureId", adventureIdTF.getValue()))
            ).ifPresent(e -> e.setData(adventureData, locationData))
        );

        Button backButton = new Button("Back", event ->
        {
//            AdventureData adventureData = adventureService.findAdventureById(adventureIdTF.getValue());
            UI.getCurrent().navigate(LocationsMenuView.class).ifPresent(
                    editor -> editor.setAdventureData(adventureData));
        });
        //                        ,
//                        new RouteParameters(
//                                new RouteParam("adventureId", adventureIdTF.getValue()))
//                )
//        );
        add(backButton);

        setMargin(true);
        setPadding(true);

        HorizontalLayout commandRow = new HorizontalLayout(manageCommands, manageItems, manageExits);
        HorizontalLayout idRow = new HorizontalLayout(locationIdTF, adventureIdTF);
        HorizontalLayout mainRow = new HorizontalLayout(resetButton, backButton, saveButton);
        add(idRow, hl, shortDescription, longDescription, commandRow, mainRow);
    }

    private IntegerField getLumenField() {
        IntegerField field = new IntegerField("Lighting (Lumen)");
        field.setMax(100);
        field.setMin(0);
        field.setStep(1);
        field.setTooltipText("Set the lighting of this location. (100 = max, 0 = total darkness)");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, LocationData::getLumen, LocationData::setLumen);
        return field;
    }

    private PaperSlider getSlider() {
        final PaperSlider slider = new PaperSlider();
        //        slider.setValue(50);
        slider.setMax(100);
        return slider;
    }

    private TextField getLocationIdTF() {
        TextField locationIdTF = new TextField("Location ID");
        locationIdTF.setReadOnly(true);
        binder.bind(locationIdTF, LocationData::getId, LocationData::setId);
        return locationIdTF;
    }

    private TextField getAdventureIdTF() {
        TextField adventureIdTF = new TextField("Adventure ID");
        adventureIdTF.setReadOnly(true);
        binder.bind(adventureIdTF, locationData -> locationData.getAdventure().getId(),
                    (locationData, adventureId) -> locationData.getAdventure().setId(adventureId));
        return adventureIdTF;
    }

//    private String getAdventuerId() {
//        return locationData.getAdventureId().getId();
//    }

    public void loadLocation(String aLocationId) {
        locationData = adventureService.findLocationById(aLocationId);
    }

    private void validateSave(LocationData aLocationData) {
        try {
            binder.writeBean(aLocationData);

            BinderValidationStatus<LocationData> status = binder.validate();

            if (status.hasErrors()) {
                throw new RuntimeException("Status Error: " + status.getValidationErrors());
            }

            if (aLocationData.getDescriptionData().getNoun().isEmpty()) {
                throw new RuntimeException("Alles Mist");
            }

            if (adventureData.getLocationData().add(aLocationData)) {
                adventureService.saveAdventureData(adventureData);
            }
            adventureService.saveLocationData((aLocationData));

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
        binder.bind(field, aLocationData -> aLocationData.getDescriptionData().getLongDescription(),
                    (aLocationData, description) -> aLocationData.getDescriptionData().setLongDescription(description));
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
        binder.bind(field, aLocationData -> aLocationData.getDescriptionData().getShortDescription(),
                    (aLocationData, description) -> aLocationData.getDescriptionData()
                                                                 .setShortDescription(description));
        return field;
    }

    private TextField getAdjectiveTextField() {
        TextField field = new TextField("Adjective");
        field.setTooltipText("The qualifier for this location.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        binder.bind(field, aLocationData -> aLocationData.getDescriptionData().getAdjective(),
                    (aLocationData, anAdjective) -> aLocationData.getDescriptionData().setAdjective(anAdjective));
        return field;
    }

    private TextField getNounTextField() {
        TextField field = new TextField("Noun");
        field.setTooltipText("The main theme of this location.");
        //        noun.setRequired(true);
        //        Label nounStatus = new Label();
        //        nounStatus.getStyle().setColor(SolidColor.RED);
        field.setValueChangeMode(ValueChangeMode.EAGER);
        binder.forField(field).asRequired("You must provide a noun.");
        binder.forField(field).bind(aLocationData -> aLocationData.getDescriptionData().getNoun(),
                    (aLocationData, aNoun) -> aLocationData.getDescriptionData().setNoun(aNoun));
        field.addValueChangeListener(event -> checkIfSaveAvailable());
        return field;
    }

    private void checkIfSaveAvailable() {
        if (binder.validate().isOk()) {
            saveButton.setEnabled(!binder.getBean().getDescriptionData().getNoun().isEmpty());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> locId = event.getRouteParameters().get("locationId");
        if (locId.isPresent()) {
            setUpLoading(locId.get());
        } else {
            setUpNewEdit();
        }

        Optional<String> advId = event.getRouteParameters().get("adventureId");
        adventureData = adventureService.findAdventureById(advId.get());
        locationData.setAdventure(adventureData);

        binder.setBean(locationData);
        saveButton.setEnabled(false);
    }

    private void setUpNewEdit() {
        locationData = new LocationData();
        pageTitle = "New Location #" + locationData.getId();
    }

    private void setUpLoading(String aLocationId) {
        loadLocation(aLocationId);
        pageTitle = "Edit Location #" + aLocationId;
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
    }
}
