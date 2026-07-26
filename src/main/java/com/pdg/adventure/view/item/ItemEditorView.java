package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

import static com.pdg.adventure.model.Word.Type.ADJECTIVE;
import static com.pdg.adventure.model.Word.Type.NOUN;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.RemoveActionData;
import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.command.CommandsMenuView;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ShowNotification;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations/:locationId/items/:itemId/edit", layout = ItemsMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/locations/:locationId/items/new", layout = ItemsMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class ItemEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(ItemEditorView.class);

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
        HorizontalLayout checkboxRow = new HorizontalLayout(isContainableCheckbox, isWearableCheckbox, isWornCheckbox,
                                                            commandsButton);
        checkboxRow.setSpacing(true);
        checkboxRow.setAlignItems(Alignment.CENTER);

        setMargin(true);
        setPadding(true);

        HorizontalLayout idRow = new HorizontalLayout(itemIdTF, locationIdTF, adventureIdTF);
        add(idRow, h1, shortDescription, longDescription, checkboxRow, resetBackSaveView);
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
                // Checkbox was checked - show verb selection dialog
                if (!tryToAddPickUpCommands(itemData, adventureData.getVocabularyData())) {
                    // Revert checkbox state if adding commands failed
                    isContainableCheckbox.setValue(false);
                }
            } else {
                // Checkbox was unchecked - remove take/drop commands
                removePickupCommands(itemData, ShowNotification.SHOW_NOTIFICATION);
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

    private boolean tryToAddPickUpCommands(final ItemData anItemData, final VocabularyData aVocabularyData) {
        Word takeVerb = aVocabularyData.getTakeWord();
        Word dropVerb = aVocabularyData.getDropWord();
        if (dropVerb == null || takeVerb == null) {
            Notification notification = Notification.show(
                    "Please select verbs to allow a player to handle this item in the vocabulary section.",
                    5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        } else {
            LOG.info("Selected verbs: {} and {} for item: {}", takeVerb.getText(), dropVerb.getText(),
                     anItemData.getId());
            createPickupCommands(takeVerb, dropVerb, anItemData);
            Notification notification = Notification.show("Take and drop commands added", 2000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        return true;
    }

    private void removePickupCommands(ItemData anItemData, final ShowNotification aRequestForNotification) {
        if (anItemData == null || anItemData.getCommandProviderData() == null) {
            return;
        }

        CommandProviderData commandProvider = anItemData.getCommandProviderData();

        // Iterate through all command chains and remove take/drop actions
        commandProvider.getAvailableCommands().entrySet().removeIf(entry -> {
            CommandChainData commandChain = entry.getValue();
            if (commandChain == null || commandChain.getCommands() == null) {
                return false;
            }

            // Remove commands with TakeActionData or DropActionData
            commandChain.getCommands().removeIf(command -> {
                if (command.getActions().isEmpty()) {
                    return false;
                }
                final CommandData rawTakeCommandData = getRawCommandData(
                        adventureData.getVocabularyData().getTakeWord(), anItemData);
                final CommandData rawDropCommandData = getRawCommandData(
                        adventureData.getVocabularyData().getDropWord(), anItemData);
                return command.getCommandDescription().getCommandSpecification()
                              .equals(rawTakeCommandData.getCommandDescription().getCommandSpecification())
                       ||
                       command.getCommandDescription().getCommandSpecification()
                              .equals(rawDropCommandData.getCommandDescription().getCommandSpecification());
            });

            // Remove the entire command chain if it's now empty
            return commandChain.getCommands().isEmpty();
        });

        LOG.info("Removed take/drop commands from item: {}", anItemData.getId());
        if (aRequestForNotification.equals(ShowNotification.SHOW_NOTIFICATION)) {
            Notification notification = Notification.show("Take and drop commands removed", 2000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void createPickupCommands(final Word aTakeVerb, final Word aDropVerb, final ItemData anItemData) {
        // First remove any existing take/drop commands to avoid duplicates
        removePickupCommands(anItemData, ShowNotification.HIDE_NOTIFICATION);

        String itemDescription = ViewSupporter.formatDescription(anItemData);

        CarriedConditionData carriedCondition = new CarriedConditionData();
        carriedCondition.setItemId(anItemData.getId());
        NotConditionData notCarriedCondition = new NotConditionData();
        notCarriedCondition.setPreCondition(carriedCondition);

        HereConditionData hereCondition = new HereConditionData();
        hereCondition.setThingId(anItemData.getId());
        NotConditionData notHereCondition = new NotConditionData();
        notHereCondition.setPreCondition(hereCondition);

        final CommandData takeCommandFailedBecauseAlreadyCarried = createTakeCommandData(aTakeVerb, anItemData);
        takeCommandFailedBecauseAlreadyCarried.getPreConditions().add(carriedCondition);
        MessageActionData messageDataBecauseAlreadyCarried = new MessageActionData();
        messageDataBecauseAlreadyCarried.setMessageId("You already have the %s.".formatted(itemDescription));
        takeCommandFailedBecauseAlreadyCarried.setActions(new ArrayList<>(List.of(messageDataBecauseAlreadyCarried)));
        anItemData.getCommandProviderData().add(takeCommandFailedBecauseAlreadyCarried);

        final CommandData takeCommandFailedBecauseNotHere = createTakeCommandData(aTakeVerb, anItemData);
        takeCommandFailedBecauseNotHere.getPreConditions().add(notHereCondition);
        MessageActionData messageDataBecauseNotHere = new MessageActionData();
        messageDataBecauseNotHere.setMessageId("The %s is not here.".formatted(itemDescription));
        takeCommandFailedBecauseNotHere.setActions(new ArrayList<>(List.of(messageDataBecauseNotHere)));
        anItemData.getCommandProviderData().add(takeCommandFailedBecauseNotHere);

        final CommandData takeCommandData = createTakeCommandData(aTakeVerb, anItemData);
        takeCommandData.getPreConditions().add(hereCondition);
        anItemData.getCommandProviderData().add(takeCommandData);

        final CommandData dropCommandFailedBecauseNotCarried = createDropCommandData(aDropVerb, anItemData);
        dropCommandFailedBecauseNotCarried.getPreConditions().add(notCarriedCondition);
        MessageActionData messageDataBecauseNotCarried = new MessageActionData();
        messageDataBecauseNotCarried.setMessageId("You don't have the %s.".formatted(itemDescription));
        dropCommandFailedBecauseNotCarried.setActions(new ArrayList<>(List.of(messageDataBecauseNotCarried)));
        anItemData.getCommandProviderData().add(dropCommandFailedBecauseNotCarried);

        final CommandData dropCommandData = createDropCommandData(aDropVerb, anItemData);
        RemoveActionData removeActionData = new RemoveActionData();
        removeActionData.setThingId(anItemData.getId());
        dropCommandData.getActions().add(removeActionData);
        anItemData.getCommandProviderData().add(dropCommandData);
    }

    private CommandData createTakeCommandData(final Word aVerb, final ItemData anItem) {
        final var takeCommandData = getRawCommandData(aVerb, anItem);
        final var takeActionData = new TakeActionData();
        takeActionData.setThingId(anItem.getId());
        takeCommandData.setActions(new ArrayList<>(List.of(takeActionData)));
        return takeCommandData;
    }

    private CommandData createDropCommandData(final Word aVerb, final ItemData anItem) {
        final var dropCommandData = getRawCommandData(aVerb, anItem);
        DropActionData dropActionData = new DropActionData();
        dropActionData.setThingId(anItem.getId());
        dropCommandData.setActions(new ArrayList<>(List.of(dropActionData)));
        return dropCommandData;
    }

    private CommandData getRawCommandData(final Word aTakeVerb, final ItemData anItem) {
        DescriptionData itemDescription = anItem.getDescriptionData();
        CommandDescriptionData commandDescription = new CommandDescriptionData(aTakeVerb,
                                                                               itemDescription.getAdjective(),
                                                                               itemDescription.getNoun());
        return new CommandData(commandDescription);
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
                ? "Edit Item: " + ViewSupporter.formatDescription(itemData)
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
        nounSelector.populate(vocabularyData.getWords(NOUN));

        saveButton.setEnabled(false);
        // Commands live on the persisted item; only offer them for an item already in the container.
        // Saving commands for an unsaved item would create a dangling document.
        commandsButton.setEnabled(existingItem.isPresent());
        ivm = new ItemViewModel(itemData);

        binder.readBean(ivm);
    }
}
