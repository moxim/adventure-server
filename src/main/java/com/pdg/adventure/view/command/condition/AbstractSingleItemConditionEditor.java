package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public abstract class AbstractSingleItemConditionEditor<T extends PreConditionData>
        extends ConditionEditorComponent<T> {

    protected final T typedCondition;
    protected final AdventureData adventureData;
    private final String label;
    private final String placeholder;
    private final String errorMessage;
    private ComboBox<ItemData> itemSelector;

    protected AbstractSingleItemConditionEditor(T conditionData, AdventureData adventureData,
            String label, String placeholder, String errorMessage) {
        super(conditionData);
        this.typedCondition = conditionData;
        this.adventureData = adventureData;
        this.label = label;
        this.placeholder = placeholder;
        this.errorMessage = errorMessage;
    }

    protected List<ItemData> provideItems() {
        return ViewSupporter.collectAllItems(adventureData);
    }

    protected abstract String currentItemId();

    protected abstract void applyItemId(String id);

    @Override
    protected final void buildUI() {
        List<ItemData> items = provideItems();
        itemSelector = new ComboBox<>(label);
        itemSelector.setItems(items);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder(placeholder);
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        String currentId = currentItemId();
        if (currentId != null) {
            items.stream().filter(i -> i.getId().equals(currentId))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        itemSelector.addValueChangeListener(e ->
            applyItemId(e.getValue() != null ? e.getValue().getId() : null));

        add(itemSelector);
    }

    @Override
    public final boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        itemSelector.setInvalid(!valid);
        if (!valid) itemSelector.setErrorMessage(errorMessage);
        return valid;
    }

    @Override
    public final String getConditionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }
}
