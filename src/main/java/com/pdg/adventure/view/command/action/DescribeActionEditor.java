package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Editor component for DescribeActionData.
 * Allows selecting either an item or a location as the target to describe.
 */
public class DescribeActionEditor extends ActionEditorComponent {

    private sealed interface DescribableTarget permits ItemTarget, LocationTarget {
        String id();
    }

    private record ItemTarget(ItemData data) implements DescribableTarget {
        @Override
        public String id() {
            return data.getId();
        }
    }

    private record LocationTarget(LocationData data) implements DescribableTarget {
        @Override
        public String id() {
            return data.getId();
        }
    }

    private final AdventureData adventureData;
    private final DescribeActionData describeActionData;
    private ComboBox<DescribableTarget> targetSelector;

    public DescribeActionEditor(DescribeActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.describeActionData = actionData;
        this.adventureData = adventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Describe Action");
        Span description = new Span("Select an item or location to describe");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<DescribableTarget> allTargets = collectAllTargets();

        targetSelector = new ComboBox<>("Target to Describe");
        targetSelector.setItems(allTargets);
        targetSelector.setItemLabelGenerator(this::formatTargetLabel);
        targetSelector.setPlaceholder("Select target to describe");
        targetSelector.setWidthFull();
        targetSelector.setRequired(true);

        // Pre-select if action already has a target
        if (describeActionData.getTargetId() != null) {
            allTargets.stream()
                      .filter(t -> t.id().equals(describeActionData.getTargetId()))
                      .findFirst()
                      .ifPresent(targetSelector::setValue);
        }

        // Update action data when target changes
        targetSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                describeActionData.setTargetId(e.getValue().id());
            } else {
                describeActionData.setTargetId(null);
            }
        });

        add(title, description, targetSelector);
    }

    @Override
    public boolean validate() {
        return targetSelector.getValue() != null;
    }

    @Override
    public String getActionSummary() {
        if (targetSelector == null || targetSelector.getValue() == null) return "(none)";
        return switch (targetSelector.getValue()) {
            case ItemTarget it -> ViewSupporter.formatDescription(it.data());
            case LocationTarget lt -> ViewSupporter.formatDescription(lt.data());
        };
    }

    private List<DescribableTarget> collectAllTargets() {
        List<DescribableTarget> targets = new ArrayList<>();

        // Add all items from all location containers and player pocket
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                List<ItemData> items = location.getItemContainerData().getItems();
                if (items != null) {
                    for (ItemData item : items) {
                        targets.add(new ItemTarget(item));
                    }
                }
            }
        }

        if (adventureData.getPlayerPocket() != null) {
            List<ItemData> pocketItems = adventureData.getPlayerPocket().getItems();
            if (pocketItems != null) {
                for (ItemData item : pocketItems) {
                    targets.add(new ItemTarget(item));
                }
            }
        }

        // Add all locations
        for (LocationData location : adventureData.getLocationData().values()) {
            targets.add(new LocationTarget(location));
        }

        return targets;
    }

    private String formatTargetLabel(DescribableTarget target) {
        return switch (target) {
            case ItemTarget it -> "Item: " + ViewSupporter.formatDescription(it.data());
            case LocationTarget lt -> "Location: " + ViewSupporter.formatDescription(lt.data());
        };
    }
}
