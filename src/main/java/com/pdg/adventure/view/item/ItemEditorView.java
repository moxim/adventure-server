package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.pdg.adventure.model.Word.Type.ADJECTIVE;
import static com.pdg.adventure.model.Word.Type.NOUN;

import com.pdg.adventure.model.*;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.command.CommandsMenuView;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations/:locationId/items/:itemId/edit", layout = ItemsMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/locations/:locationId/items/new", layout = ItemsMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class ItemEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(ItemEditorView.class);

    private static final int MIN_LUMEN = 0;
    private static final int MAX_LUMEN = 100;
    private static final int LUMEN_STEP = 1;

    private final transient AdventureService adventureService;
    private final transient ItemService itemService;
    private final transient AdventureAccessService accessService;
    private final Binder<ItemViewModel> binder;
    private final VocabularyPickerField adjectiveSelector;
    private final VocabularyPickerField nounSelector;

    private Button saveButton;
    private Button resetButton;
    private Button commandsButton;
    private String pageTitle;

    private transient String itemId;
    private transient ItemData itemData;
    private transient ItemViewModel ivm;
    private transient AdventureData adventureData;
    private transient LocationData locationData;

    public ItemEditorView(AdventureService anAdventureService, ItemService anItemService,
                          AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        itemService = anItemService;
        accessService = anAccessService;
        binder = new Binder<>(ItemViewModel.class);

        itemData = new ItemData();
        itemId = itemData.getId();

        adjectiveSelector = new VocabularyPickerField("Adjective", "The qualifier for this item.");
        nounSelector = new VocabularyPickerField("Noun", "The main theme of this item.");
        nounSelector.setPlaceholder("Select a noun (required)");

        TextField itemIdTF = getItemIdTF();
        TextField adventureIdTF = getAdventureIdTF();
        TextField locationIdTF = getLocationIdTF();
        TextArea shortDescription = getShortDescTextArea();
        TextArea longDescription = getLongDescTextArea();
        IntegerField lumen = getLumenField();

        // Checkboxes for item properties
        final var isContainableCheckbox = createIsContainableCheckbox();

        Checkbox isWearableCheckbox = new Checkbox("Is wearable");
        isWearableCheckbox.setTooltipText("If checked, this item can be worn by the player.");

        Checkbox isWornCheckbox = new Checkbox("Is worn");
        isWornCheckbox.setTooltipText("If checked, this item starts out being worn.");

        commandsButton = new Button("Manage Commands", _ ->
                UI.getCurrent().navigate(CommandsMenuView.class, new RouteParameters(
                        new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                        new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                        new RouteParam(RouteIds.ITEM_ID.getValue(), itemId)))
        );
        commandsButton.setEnabled(false);

        final ResetBackSaveView resetBackSaveView = setUpNavigationButtons();

        // Bind fields
        binder.forField(nounSelector).asRequired("Noun is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a noun with text")
              .bind(ItemViewModel::getNoun, ItemViewModel::setNoun);
        binder.forField(adjectiveSelector).bind(ItemViewModel::getAdjective, ItemViewModel::setAdjective);
        binder.bind(shortDescription, ItemViewModel::getShortDescription, ItemViewModel::setShortDescription);
        binder.bind(longDescription, ItemViewModel::getLongDescription, ItemViewModel::setLongDescription);
        binder.bind(isContainableCheckbox, ItemViewModel::isContainable, ItemViewModel::setContainable);
        binder.bind(isWearableCheckbox, ItemViewModel::isWearable, ItemViewModel::setWearable);
        binder.bind(isWornCheckbox, ItemViewModel::isWorn, ItemViewModel::setWorn);
        binder.bind(lumen, ItemViewModel::getLumen, ItemViewModel::setLumen);
        binder.bindReadOnly(itemIdTF, ItemViewModel::getId);
        binder.bindReadOnly(locationIdTF, ItemViewModel::getLocationId);
        binder.bindReadOnly(adventureIdTF, ItemViewModel::getAdventureId);

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
            resetButton.setEnabled(hasChanges);
        });

        HorizontalLayout h1 = new HorizontalLayout(adjectiveSelector, nounSelector);
        HorizontalLayout h2 = new HorizontalLayout(lumen);
        HorizontalLayout checkboxRow = new HorizontalLayout(isContainableCheckbox, isWearableCheckbox, isWornCheckbox,
                                                            commandsButton);
        checkboxRow.setSpacing(true);
        checkboxRow.setAlignItems(Alignment.CENTER);

        setMargin(true);
        setPadding(true);

        HorizontalLayout idRow = new HorizontalLayout(itemIdTF, locationIdTF, adventureIdTF);
        add(idRow, h1, h2, shortDescription, longDescription, checkboxRow, resetBackSaveView);
    }

    private Checkbox createIsContainableCheckbox() {
        Checkbox isContainableCheckbox = new Checkbox("Can be picked up / Is containable");
        isContainableCheckbox.setTooltipText("If checked, this item can be picked up and placed in containers.");

        // Add value change listener to handle both checking and unchecking
        isContainableCheckbox.addValueChangeListener(event -> {
            // Only process user interactions, not programmatic changes
            if (!event.isFromClient()) {
                return;
            }

            if (Boolean.TRUE.equals(event.getValue())) {
                binder.writeBeanIfValid(ivm);
            }
        });

        return isContainableCheckbox;
    }

    private TextField getItemIdTF() {
        TextField field = new TextField("Item ID");
        field.setReadOnly(true);
        return field;
    }

    private TextField getAdventureIdTF() {
        TextField field = new TextField("Adventure ID");
        field.setReadOnly(true);
        return field;
    }

    private TextField getLocationIdTF() {
        TextField field = new TextField("Location ID");
        field.setReadOnly(true);
        return field;
    }

    private TextArea getShortDescTextArea() {
        TextArea field = new TextArea("Short description");
        field.setWidth("95%");
        field.setMinHeight("100px");
        field.setMaxHeight("150px");
        field.setTooltipText("If left empty, this will be derived from the provided noun and adjective.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        return field;
    }

    private TextArea getLongDescTextArea() {
        TextArea field = new TextArea("Long description");
        field.setWidth("95%");
        field.setMinHeight("200px");
        field.setMaxHeight("350px");
        field.setTooltipText("If left empty, this will be derived from the short description.");
        field.setValueChangeMode(ValueChangeMode.EAGER);
        return field;
    }

    private IntegerField getLumenField() {
        IntegerField field = new IntegerField("Lighting (Lumen)");
        field.setMax(MAX_LUMEN);
        field.setMin(MIN_LUMEN);
        field.setStep(LUMEN_STEP);
        field.setTooltipText(
                "Set how much light this item emits, e.g. a lit torch. (" + MAX_LUMEN + " = max, " + MIN_LUMEN
                + " = none)");
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
        saveButton.addClickListener(_ -> validateSave(ivm));
        resetButton.addClickListener(_ -> binder.readBean(ivm));
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void navigateBack() {
        UI.getCurrent().navigate(ItemsMenuView.class, new RouteParameters(
//                  new RouteParam(RouteIds.ITEM_ID.getValue(), itemId),
                  new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                  new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
    }

    private void validateSave(ItemViewModel anItemViewModel) {
        try {
            if (binder.validate().isOk()) {
                binder.writeBean(anItemViewModel);
                final ItemData modelItemData = anItemViewModel.getData();

                // Set adventure and location IDs
                modelItemData.setAdventureId(adventureData.getId());
                modelItemData.setLocationId(locationData.getId());
                modelItemData.setParentContainerId(locationData.getItemContainerData().getId());

                // Save item to items collection first (required for @DBRef to work)
                ItemData savedItem = itemService.saveItem(modelItemData);

                // Update or add item reference to the in-memory container
                List<ItemData> items = locationData.getItemContainerData().getItems();
                boolean itemExists = items.stream().filter(item -> item != null)  // Filter out any null items
                                          .anyMatch(item -> item.getId().equals(savedItem.getId()));

                if (!itemExists) {
                    items.add(savedItem);
                } else {
                    // Update existing item reference
                    for (int i = 0; i < items.size(); i++) {
                        ItemData item = items.get(i);
                        if (item != null && item.getId().equals(savedItem.getId())) {
                            items.set(i, savedItem);
                            break;
                        }
                    }
                }

                // Save the adventure data (updates the @DBRef references)
                adventureData.getLocationData().put(locationData.getId(), locationData);
                adventureService.saveAdventureData(adventureData);

                navigateBack();
            }
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
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        optionalItemId.ifPresent(id -> itemId = id);
        setData(resolvedAdventure.get(), resolvedLocation.get());
        pageTitle = optionalItemId.isPresent()
                ? "Edit Item: " + ViewSupporter.getDescriptionText(itemData.getDescriptionData())
                : "New Item";
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    private void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

        // Load item from in-memory container or create new. A new ItemData already carries a
        // (ULID) id, so "is this an existing item?" must be answered by container membership,
        // not by whether the id is non-empty.
        Optional<ItemData> existingItem = (itemId != null && !itemId.isEmpty())
                ? locationData.getItemContainerData().getItems().stream()
                              .filter(item -> item.getId().equals(itemId)).findFirst()
                : Optional.empty();
        itemData = existingItem.orElseGet(ItemData::new);
        itemId = itemData.getId();
        itemData.setAdventureId(adventureData.getId());
        itemData.setLocationId(locationData.getId());

        VocabularyData vocabularyData = adventureData.getVocabularyData();
        adjectiveSelector.populate(vocabularyData.getWords(ADJECTIVE));
        if (itemData.getDescriptionData() != null && itemData.getDescriptionData().getAdjective() == null) {
            adjectiveSelector.setHelperText("Why not give this a descriptive adjective?");
        }
        nounSelector.populate(vocabularyData.getWords(NOUN));
        if (itemData.getDescriptionData() != null && itemData.getDescriptionData().getNoun() == null) {
            nounSelector.setHelperText("Give this a descriptive name.");
        }

        saveButton.setEnabled(false);
        // Commands live on the persisted item; only offer them for an item already in the container.
        // Saving commands for an unsaved item would create a dangling document.
        commandsButton.setEnabled(existingItem.isPresent());
        ivm = new ItemViewModel(itemData);

        binder.readBean(ivm);
    }
}
