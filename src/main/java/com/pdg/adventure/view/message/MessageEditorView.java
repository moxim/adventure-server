package com.pdg.adventure.view.message;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.MessageService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "adventures/:adventureId/messages/:messageId/edit", layout = MessagesMainLayout.class)
@RouteAlias(value = "adventures/:adventureId/messages/new", layout = MessagesMainLayout.class)
public class MessageEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(MessageEditorView.class);

    private final transient AdventureService adventureService;
    private final transient MessageService messageService;
    private final Binder<MessageViewModel> binder;

    private Button saveButton;
    private Button resetButton;
    private final TextField messageIdField;
    private final TextArea messageTextField;
    private final Div previewDiv;
    private final Div usageInfoDiv;
    private String pageTitle;

    private transient String messageId;
    private transient String originalMessageId; // Store original ID for updates
    private transient AdventureData adventureData;
    private transient MessageViewModel mvm;

    @Autowired
    public MessageEditorView(AdventureService anAdventureService, MessageService aMessageService) {
        setSizeFull();

        adventureService = anAdventureService;
        messageService = aMessageService;
        binder = new Binder<>(MessageViewModel.class);

        // Build UI
        H4 title = new H4("Message Editor");

        messageIdField = new TextField("Message ID");
        messageIdField.setPlaceholder("e.g., welcome_message, door_locked");
        messageIdField.setHelperText("Alphanumeric characters and underscores only");
        messageIdField.setWidthFull();
        messageIdField.setRequired(true);
        messageIdField.setValueChangeMode(ValueChangeMode.EAGER);

        messageTextField = new TextArea("Message Text");
        messageTextField.setPlaceholder("Enter the message text that will be displayed to the player");
        messageTextField.setWidthFull();
        messageTextField.setRequired(true);
        messageTextField.setMinHeight("150px");
        messageTextField.setMaxHeight("400px");
        messageTextField.setValueChangeMode(ValueChangeMode.EAGER);

        // Preview section
        Span previewLabel = new Span("Preview:");
        previewLabel.getStyle().set("font-weight", "bold");
        previewDiv = new Div();
        previewDiv.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                  .set("border-radius", "var(--lumo-border-radius-m)").set("padding", "var(--lumo-space-m)")
                  .set("background-color", "var(--lumo-contrast-5pct)").set("min-height", "60px")
                  .set("margin-top", "var(--lumo-space-s)");

        VerticalLayout previewSection = new VerticalLayout(previewLabel, previewDiv);
        previewSection.setPadding(false);
        previewSection.setSpacing(false);

        // Usage info section
        Span usageLabel = new Span("Usage:");
        usageLabel.getStyle().set("font-weight", "bold");
        usageInfoDiv = new Div();
        usageInfoDiv.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("border-radius", "var(--lumo-border-radius-m)").set("padding", "var(--lumo-space-m)")
                    .set("background-color", "var(--lumo-contrast-5pct)").set("margin-top", "var(--lumo-space-s)");

        VerticalLayout usageSection = new VerticalLayout(usageLabel, usageInfoDiv);
        usageSection.setPadding(false);
        usageSection.setSpacing(false);

        final ResetBackSaveView resetBackSaveView = setUpNavigationButtons();

        // Bind fields
        binder.forField(messageIdField).asRequired("Message ID is required")
              .withValidator(id -> id != null && id.matches("^[a-zA-Z0-9_]+$"),
                             "Message ID must contain only letters, numbers, and underscores")
              .withValidator(id -> isMessageIdUnique(id), "A message with this ID already exists")
              .bind(MessageViewModel::getId, MessageViewModel::setId);

        binder.forField(messageTextField).asRequired("Message text is required")
              .withValidator(text -> text != null && !text.trim().isEmpty(), "Message text cannot be empty")
              .bind(MessageViewModel::getMessageText, MessageViewModel::setMessageText);

        // Update preview when message text changes
        messageTextField.addValueChangeListener(e -> updatePreview());

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
            resetButton.setEnabled(hasChanges);
        });

        HorizontalLayout fieldsLayout = new HorizontalLayout(messageIdField, messageTextField);
        fieldsLayout.setWidthFull();

        add(title, messageIdField, messageTextField, previewSection, usageSection, resetBackSaveView);
    }

    private ResetBackSaveView setUpNavigationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        saveButton = resetBackSaveView.getSave();
        saveButton.setEnabled(false);
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);

        backButton.addClickListener(event -> UI.getCurrent().navigate(MessagesMenuView.class, new RouteParameters(
                                                       new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
                                               .ifPresent(e -> e.setData(adventureData)));

        saveButton.addClickListener(event -> validateAndSave());

        resetButton.addClickListener(event -> {
            binder.readBean(mvm);
            updatePreview();
        });

        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private boolean isMessageIdUnique(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        // If we're editing an existing message with the same ID, that's okay
        if (originalMessageId != null && originalMessageId.equals(id)) {
            return true;
        }

        // Check if another message with this ID exists
        return adventureData == null || !messageService.messageExists(adventureData.getId(), id);
    }

    private void updatePreview() {
        String text = messageTextField.getValue();
        if (text == null || text.trim().isEmpty()) {
            previewDiv.setText("(empty message)");
            previewDiv.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");
        } else {
            previewDiv.setText(text);
            previewDiv.getStyle().set("font-style", "normal").set("color", "var(--lumo-body-text-color)");
        }
    }

    private void validateAndSave() {
        try {
            if (binder.validate().isOk()) {
                binder.writeBean(mvm);

                // Create or update message
                MessageData message;
                if (mvm.isNew()) {
                    // Create new message
                    message = new MessageData(adventureData.getId(), mvm.getId(), mvm.getMessageText());
                } else {
                    // Get existing message or create new one
                    message = adventureData.getMessages()
                                           .get(originalMessageId != null ? originalMessageId : mvm.getId());
                    if (message == null) {
                        message = new MessageData(adventureData.getId(), mvm.getId(), mvm.getMessageText());
                    } else {
                        message.setMessageId(mvm.getId());
                        message.setText(mvm.getMessageText());
                        message.touch();
                    }
                }

                // Update message properties
                if (mvm.getCategory() != null) {
                    message.setCategory(mvm.getCategory());
                }
                if (mvm.getNotes() != null) {
                    message.setNotes(mvm.getNotes());
                }

                // If message ID changed, remove old entry
                if (originalMessageId != null && !originalMessageId.equals(mvm.getId())) {
                    adventureData.getMessages().remove(originalMessageId);
                }

                // Add/update message in adventure's messages Map
                adventureData.getMessages().put(mvm.getId(), message);

                // Save adventure (triggers cascade save for message via @CascadeSave)
                adventureService.saveAdventureData(adventureData);

                // Update tracking variables
                originalMessageId = mvm.getId();
                messageId = mvm.getId();

                // Reset change tracking
                saveButton.setEnabled(false);
                resetButton.setEnabled(false);

                // Mark as no longer new
                mvm.setNew(false);

                // Update usage info
                updateUsageInfo();
            }
        } catch (ValidationException e) {
            LOG.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle service exceptions (e.g., duplicate ID)
            LOG.error(e.getMessage());
            // TODO: Show error notification to user
        }
    }

    private void updateUsageInfo() {
        if (messageId == null || messageId.isEmpty()) {
            usageInfoDiv.removeAll();
            usageInfoDiv.add(new Span("This is a new message. Usage information will be available after saving."));
            usageInfoDiv.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");
            return;
        }

        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData, messageId);

        usageInfoDiv.removeAll();
        usageInfoDiv.getStyle().set("font-style", "normal").set("color", "var(--lumo-body-text-color)");

        if (usages.isEmpty()) {
            Span noUsageSpan = new Span("This message is not currently used anywhere in the adventure.");
            noUsageSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
            usageInfoDiv.add(noUsageSpan);
        } else {
            Span usageCountSpan = new Span("Used in " + usages.size() + " location(s):");
            usageCountSpan.getStyle().set("font-weight", "bold").set("display", "block").set("margin-bottom", "0.5em");
            usageInfoDiv.add(usageCountSpan);

            VerticalLayout usageList = new VerticalLayout();
            usageList.setPadding(false);
            usageList.setSpacing(false);
            usageList.getStyle().set("margin-left", "1em");

            for (MessageUsageTracker.MessageUsage usage : usages) {
                Span usageItem = new Span("• " + usage.getDisplayText());
                usageItem.getStyle().set("font-size", "0.9em").set("display", "block");
                usageList.add(usageItem);
            }

            usageInfoDiv.add(usageList);
        }
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalMessageId = event.getRouteParameters().get(RouteIds.MESSAGE_ID.getValue());
        if (optionalMessageId.isPresent()) {
            messageId = optionalMessageId.get();
            pageTitle = "Edit Message: " + messageId;
        } else {
            messageId = null;
            pageTitle = "New Message";
        }
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    public void setData(AdventureData anAdventureData) {
        adventureData = anAdventureData;

        // Load existing message or create new one
        if (messageId != null && !messageId.isEmpty()) {
            // Load from adventure's messages Map (loaded via @DBRef)
            MessageData messageData = adventureData.getMessages().get(messageId);
            if (messageData != null) {
                mvm = new MessageViewModel(messageData);
                originalMessageId = messageId;
            } else {
                // Message not found, create new one
                mvm = new MessageViewModel();
            }
        } else {
            // Creating a new message
            mvm = new MessageViewModel();
        }

        binder.readBean(mvm);
        updatePreview();
        updateUsageInfo();
        saveButton.setEnabled(false);
        resetButton.setEnabled(false);
    }
}
