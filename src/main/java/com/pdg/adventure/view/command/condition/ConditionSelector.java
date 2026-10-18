package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.pdg.adventure.model.condition.*;

public class ConditionSelector extends HorizontalLayout {
    private final ComboBox<ConditionTypeDescriptor> typeSelector;

    @Setter
    private transient ConditionSelectedListener conditionSelectedListener;


    /** The selector for a list where a click on the picture cannot happen: CLICKED is not offered. */
    public ConditionSelector() {
        this(false);
    }

    /** @param offersClicked whether CLICKED is offered; only the Processes can be run by a click */
    public ConditionSelector(boolean offersClicked) {
        typeSelector = new ComboBox<>("Add Condition");
        typeSelector.setItems(availableTypes(offersClicked));
        typeSelector.setItemLabelGenerator(ConditionTypeDescriptor::displayName);
        typeSelector.setPlaceholder("Choose condition type…");
        typeSelector.setWidthFull();
        typeSelector.getStyle().set("--vaadin-combo-box-overlay-width", "28em");

        Button addButton = new Button("Add");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setEnabled(false);

        typeSelector.addValueChangeListener(e -> addButton.setEnabled(e.getValue() != null));

        addButton.addClickListener(_ -> {
            ConditionTypeDescriptor selected = typeSelector.getValue();
            if (selected != null && conditionSelectedListener != null) {
                conditionSelectedListener.onConditionSelected(selected.createData());
                typeSelector.clear();
                addButton.setEnabled(false);
            }
        });

        setWidthFull();
        setAlignItems(Alignment.END);
        add(typeSelector, addButton);
        expand(typeSelector);
    }

    private List<ConditionTypeDescriptor> availableTypes(boolean offersClicked) {
        Stream<ConditionTypeDescriptor> clicked = offersClicked
            ? Stream.of(new ConditionTypeDescriptor("Clicked (matches a click on the picture)", ClickedConditionData::new))
            : Stream.empty();
        return Stream.concat(clicked, Stream.of(
            new ConditionTypeDescriptor("Carried (item in inventory)", CarriedConditionData::new),
            new ConditionTypeDescriptor("Here (item at current location)", HereConditionData::new),
            new ConditionTypeDescriptor("Worn (item being worn)", WornConditionData::new),
            new ConditionTypeDescriptor("Player At (location)", PlayerAtConditionData::new),
            new ConditionTypeDescriptor("Item At (item + location)", ItemAtConditionData::new),
            new ConditionTypeDescriptor("Equals (variable = value)", EqualsConditionData::new),
            new ConditionTypeDescriptor("Greater Than (variable > value)", GreaterThanConditionData::new),
            new ConditionTypeDescriptor("Less Than (variable < value)", LessThanConditionData::new),
            new ConditionTypeDescriptor("Same (variable = variable)", SameConditionData::new),
            new ConditionTypeDescriptor("Chance (1-100)", ChanceConditionData::new),
            new ConditionTypeDescriptor("Preposition (matches typed preposition)", PrepositionConditionData::new),
            new ConditionTypeDescriptor("Adverb (matches typed adverb)", AdverbConditionData::new),
            new ConditionTypeDescriptor("Noun 2 (matches typed second noun)", Noun2ConditionData::new),
            new ConditionTypeDescriptor("Adjective 2 (matches typed second adjective)", Adjective2ConditionData::new)
        )).sorted(Comparator.comparing(ConditionTypeDescriptor::displayName, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @FunctionalInterface
    public interface ConditionSelectedListener {
        void onConditionSelected(PreConditionData data);
    }

    private record ConditionTypeDescriptor(String displayName, Supplier<PreConditionData> factory) {
        PreConditionData createData() { return factory.get(); }
    }
}
