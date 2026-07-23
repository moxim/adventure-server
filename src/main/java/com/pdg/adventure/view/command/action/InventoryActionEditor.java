package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.InventoryActionData;

/**
 * Editor component for InventoryActionData.
 * The inventory action has no configurable parameters — it displays the player's
 * inventory automatically. The underlying fields (messageConsumerId, containerProviderId)
 * are broken placeholders and are intentionally not exposed in the UI.
 */
@AutoRegisterActionEditor
public class InventoryActionEditor extends ActionEditorComponent<InventoryActionData> {

    public InventoryActionEditor(InventoryActionData actionData) {
        super(actionData);
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Inventory Action");

        Span description = new Span("Display the player's inventory. This action takes no parameters.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ The inventory action is fully automatic — no configuration needed.");

        add(title, description, info);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String getActionSummary() {
        return "";
    }
}
