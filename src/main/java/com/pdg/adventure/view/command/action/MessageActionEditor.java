package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.MessageActionData;

/**
 * Editor component for MessageActionData.
 * Allows entering a message that will be displayed to the player.
 */
public class MessageActionEditor extends ActionEditorComponent {
    private final MessageActionData messageActionData;
    private TextArea messageField;

    public MessageActionEditor(MessageActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.messageActionData = actionData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Message Action");
        Span description = new Span("Display a message to the player");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        messageField = new TextArea("Message");
        messageField.setPlaceholder("Enter the message to display");
        messageField.setWidthFull();
        messageField.setRequired(true);
        messageField.setMinHeight("100px");
        messageField.setMaxHeight("300px");

        // Pre-fill if action already has a message
        if (messageActionData.getMessageId() != null) {
            messageField.setValue(messageActionData.getMessageId());
        }

        // Update action data when message changes
        messageField.addValueChangeListener(e -> {
            if (e.getValue() != null && !e.getValue().trim().isEmpty()) {
                messageActionData.setMessageId(e.getValue().trim());
            } else {
                messageActionData.setMessageId(null);
            }
        });

        add(title, description, messageField);
    }

    @Override
    public boolean validate() {
        boolean isValid = messageField.getValue() != null && !messageField.getValue().trim().isEmpty();
        if (!isValid) {
            messageField.setErrorMessage("Please enter a message");
            messageField.setInvalid(true);
        } else {
            messageField.setInvalid(false);
        }
        return isValid;
    }
}
