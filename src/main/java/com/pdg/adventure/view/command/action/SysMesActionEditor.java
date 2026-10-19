package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.Arrays;
import java.util.List;

import com.pdg.adventure.model.action.SysMesActionData;
import com.pdg.adventure.server.action.MesAction;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.PlaceholderSpec;

/**
 * Editor component for SysMesActionData, which the authors know as "SystemMsg": one of the engine's system messages,
 * shown without a line break, like {@link MesAction}.
 */
@AutoRegisterActionEditor
public class SysMesActionEditor extends ActionEditorComponent<SysMesActionData> {
    private final SysMesActionData sysMesActionData;
    private ComboBox<SystemMessageKey> messageComboBox;
    private Div preview;

    public SysMesActionEditor(SysMesActionData actionData) {
        super(actionData);
        this.sysMesActionData = actionData;
    }

    /**
     * The messages an author can pick: every one that prints as it is. A message with a %s in it needs an argument
     * (an item's name, a slot count) that only the engine code at its place of use can supply.
     */
    static List<SystemMessageKey> choices() {
        return Arrays.stream(SystemMessageKey.values())
                     .filter(key -> PlaceholderSpec.of(key.defaultText()).argumentPositions().isEmpty())
                     .toList();
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("SystemMsg Action");
        Span description = new Span("Display one of the system messages without ending the line, so the next text "
                                    + "continues on the same line");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        messageComboBox = new ComboBox<>("System Message");
        messageComboBox.setPlaceholder("Select a system message");
        messageComboBox.setItems(choices());
        messageComboBox.setItemLabelGenerator(SysMesActionEditor::label);
        messageComboBox.setWidthFull();
        messageComboBox.setRequired(true);
        SystemMessageKey.fromId(sysMesActionData.getMessageKeyId()).ifPresent(messageComboBox::setValue);

        preview = new Div();
        preview.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
        updatePreview(messageComboBox.getValue());

        messageComboBox.addValueChangeListener(e -> {
            sysMesActionData.setMessageKeyId(e.getValue() == null ? null : e.getValue().id());
            updatePreview(e.getValue());
        });

        add(title, description, messageComboBox, preview);
    }

    private void updatePreview(SystemMessageKey aKey) {
        preview.setText(aKey == null ? "(No system message selected)" : aKey.description());
    }

    private static String label(SystemMessageKey aKey) {
        return aKey.id() + ": " + aKey.defaultText();
    }

    @Override
    public boolean validate() {
        boolean valid = messageComboBox.getValue() != null;
        if (!valid) {
            messageComboBox.setErrorMessage("Please select a system message");
        }
        messageComboBox.setInvalid(!valid);
        return valid;
    }

    @Override
    public String getActionSummary() {
        SystemMessageKey key = messageComboBox == null ? null : messageComboBox.getValue();
        return key == null ? "(none)" : label(key);
    }

    /** Shown to the authors in the action row; "SysMes" is only the engine's name. */
    @Override
    public String getActionTypeName() {
        return "SystemMsgActionData";
    }
}
