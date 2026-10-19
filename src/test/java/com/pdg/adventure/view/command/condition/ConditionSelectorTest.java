package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.ClickedConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

class ConditionSelectorTest {

    private static final String CLICKED_LABEL = ConditionSelectorProbe.CLICKED_LABEL;

    private static ComboBox<Object> comboOf(ConditionSelector selector) {
        return ConditionSelectorProbe.comboOf(selector);
    }

    private static List<String> labels(ConditionSelector selector) {
        return ConditionSelectorProbe.labels(selector);
    }

    @Test
    void clicked_isOfferedWhenTheSelectorOffersIt() {
        assertThat(labels(new ConditionSelector(true))).contains(CLICKED_LABEL);
    }

    @Test
    void clicked_isNotOfferedByDefault() {
        assertThat(labels(new ConditionSelector())).doesNotContain(CLICKED_LABEL);
        assertThat(labels(new ConditionSelector(false))).doesNotContain(CLICKED_LABEL);
    }

    @Test
    void offeringClicked_addsExactlyThatOneEntry() {
        assertThat(labels(new ConditionSelector(true))).hasSize(labels(new ConditionSelector(false)).size() + 1);
    }

    @Test
    void choosingClicked_handsOutAFreshClickedConditionData() {
        ConditionSelector selector = new ConditionSelector(true);
        List<PreConditionData> chosen = new java.util.ArrayList<>();
        selector.setConditionSelectedListener(chosen::add);
        ComboBox<Object> combo = comboOf(selector);
        Object clicked = combo.getListDataView().getItems()
                              .filter(item -> CLICKED_LABEL.equals(combo.getItemLabelGenerator().apply(item)))
                              .findFirst().orElseThrow();
        combo.setValue(clicked);

        selector.getChildren().filter(com.vaadin.flow.component.button.Button.class::isInstance)
                .map(com.vaadin.flow.component.button.Button.class::cast).findFirst().orElseThrow().click();

        assertThat(chosen).singleElement().isInstanceOf(ClickedConditionData.class);
    }
}
