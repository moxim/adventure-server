package com.pdg.adventure.view.command;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Dropdown for choosing a variable by name, fed from the variables the adventure defines.
 * <p>
 * Constructed with {@code allowNew = false} (conditions, Increment/Decrement) it only accepts
 * defined names; with {@code allowNew = true} (Set Variable, the action that defines a variable)
 * the author may also type a new name. The names come from a {@link Supplier} that is re-read
 * whenever the field gets focus, so a variable defined in another editor on the same page shows
 * up without rebuilding this one. A {@code null} supplier means "no adventure context" and leaves
 * the choice unrestricted.
 */
public class VariableNameSelector extends ComboBox<String> {

    private final Supplier<? extends Collection<String>> definedNames;
    private final boolean allowNew;

    public VariableNameSelector(String aLabel, Supplier<? extends Collection<String>> someDefinedNames,
                                boolean isNewNameAllowed, String aCurrentValue) {
        super(aLabel);
        definedNames = someDefinedNames;
        allowNew = isNewNameAllowed;

        setItems(itemsFor(aCurrentValue));
        setWidthFull();
        setRequired(true);
        setAllowCustomValue(allowNew);
        if (allowNew) {
            addCustomValueSetListener(event -> {
                String typed = event.getDetail() == null ? "" : event.getDetail().trim();
                if (!typed.isEmpty()) {
                    if (getListDataView().getItems().noneMatch(typed::equals)) {
                        getListDataView().addItem(typed);
                    }
                    setValue(typed);
                }
            });
        }
        addFocusListener(_ -> setItems(itemsFor(getValue())));
        if (aCurrentValue != null && !aCurrentValue.isBlank()) {
            setValue(aCurrentValue);
        }
    }

    // A selected name that is no longer defined must still be listed, or the field would blank it
    // out and the author could not see what is wrong; isAcceptable() flags it.
    private List<String> itemsFor(String aSelectedName) {
        List<String> items = new ArrayList<>(definedNames == null ? List.of() : definedNames.get());
        if (aSelectedName != null && !aSelectedName.isBlank() && !items.contains(aSelectedName)) {
            items.add(aSelectedName);
        }
        return items;
    }

    /** The selected name, or an empty string if nothing is selected. */
    public String selectedName() {
        return getValue() == null ? "" : getValue();
    }

    /** True if something is selected and it is a variable the author is allowed to use here. */
    public boolean isAcceptable() {
        String name = selectedName();
        return !name.isBlank() && (definedNames == null || allowNew || definedNames.get().contains(name));
    }

    /** Marks the field invalid with a fitting message if not {@link #isAcceptable()}. */
    public boolean validateSelection() {
        boolean acceptable = isAcceptable();
        if (!acceptable) {
            setErrorMessage(selectedName().isBlank()
                            ? "Please select a variable"
                            : "Variable '" + selectedName() + "' isn't defined - set it with a Set Variable "
                              + "action first");
        }
        setInvalid(!acceptable);
        return acceptable;
    }
}
