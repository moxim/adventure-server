package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

/** Test helper: the labels a {@link ConditionSelector} offers. */
public final class ConditionSelectorProbe {

    public static final String CLICKED_LABEL = "Clicked (matches a click on the picture)";

    private ConditionSelectorProbe() {
    }

    @SuppressWarnings("unchecked")
    public static ComboBox<Object> comboOf(ConditionSelector aSelector) {
        return (ComboBox<Object>) aSelector.getChildren().filter(ComboBox.class::isInstance).findFirst().orElseThrow();
    }

    public static List<String> labels(ConditionSelector aSelector) {
        ComboBox<Object> combo = comboOf(aSelector);
        return combo.getListDataView().getItems().map(combo.getItemLabelGenerator()::apply).toList();
    }
}
