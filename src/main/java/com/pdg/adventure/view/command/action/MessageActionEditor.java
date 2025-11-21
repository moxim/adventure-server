package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Style;

import java.util.List;
import java.util.stream.Collectors;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.action.MessageActionData;

/**
 * Editor component for MessageActionData.
 * Allows selecting a message from the message catalog.
 */
public class MessageActionEditor extends ActionEditorComponent {
    private final MessageActionData messageActionData;
    private final AdventureData adventureData;
    private ComboBox<String> messageIdComboBox;
    private Div messagePreview;

    public MessageActionEditor(MessageActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.messageActionData = actionData;
        this.adventureData = adventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Message Action");
        Span description = new Span("Display a message to the player from the message catalog");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Load available messages from adventure's messages Map (loaded via @DBRef)
        List<String> messageIds = adventureData.getMessages().values().stream()
                .map(MessageData::getMessageId)
                .sorted()
                .collect(Collectors.toList());

        messageIdComboBox = new ComboBox<>("Message ID");
        messageIdComboBox.setPlaceholder("Select a message from the catalog");
        messageIdComboBox.setItems(messageIds);
        messageIdComboBox.setWidthFull();
        messageIdComboBox.setRequired(true);
        messageIdComboBox.setAllowCustomValue(true);

        // Add custom value handling for manual entry
        messageIdComboBox.addCustomValueSetListener(e -> {
            String customValue = e.getDetail();
            if (customValue != null && !customValue.trim().isEmpty()) {
                messageIdComboBox.setValue(customValue.trim());
            }
        });

        // Pre-fill if action already has a message
        if (messageActionData.getMessageId() != null) {
            messageIdComboBox.setValue(messageActionData.getMessageId());
        }

        // Message preview
        messagePreview = new Div();
        messagePreview.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("margin-top", "var(--lumo-space-s)")
                .set("min-height", "60px");

        Span previewLabel = new Span("Message Preview:");
        previewLabel.getStyle().set("font-weight", "bold");

        // Update action data and preview when selection changes
        messageIdComboBox.addValueChangeListener(e -> {
            String selectedMessageId = e.getValue();
            if (selectedMessageId != null && !selectedMessageId.trim().isEmpty()) {
                messageActionData.setMessageId(selectedMessageId.trim());
                updateMessagePreview(selectedMessageId.trim());
            } else {
                messageActionData.setMessageId(null);
                messagePreview.removeAll();
                Span emptySpan = new Span("(No message selected)");
                setStyle(emptySpan.getStyle());
                messagePreview.add(emptySpan);
            }
        });

        // Initialize preview
        if (messageActionData.getMessageId() != null) {
            updateMessagePreview(messageActionData.getMessageId());
        } else {
            Span emptySpan = new Span("(No message selected)");
            setStyle(emptySpan.getStyle());
            messagePreview.add(emptySpan);
        }

        add(title, description, messageIdComboBox, previewLabel, messagePreview);
    }

    private void updateMessagePreview(String messageId) {
        messagePreview.removeAll();

        // Get message from adventure's messages Map
        MessageData message = adventureData.getMessages().get(messageId);
        if (message != null) {
            Span textSpan = new Span(message.getText());
            messagePreview.add(textSpan);
        } else {
            Span warningSpan = new Span("⚠ Message ID '" + messageId + "' not found in catalog");
            setStyle(warningSpan.getStyle());
            messagePreview.add(warningSpan);
        }
    }

    private void setStyle(Style aStyle) {
        aStyle.set("font-style", "italic")
              .set("color", "var(--lumo-secondary-text-color)");
    }

    @Override
    public boolean validate() {
        boolean isValid = messageIdComboBox.getValue() != null && !messageIdComboBox.getValue().trim().isEmpty();
        if (!isValid) {
            messageIdComboBox.setErrorMessage("Please select or enter a message ID");
            messageIdComboBox.setInvalid(true);
        } else {
            messageIdComboBox.setInvalid(false);
        }
        return isValid;
    }
}
