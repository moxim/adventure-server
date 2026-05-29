package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.*;

public class ConditionEditorFactory {

    public static ConditionEditorComponent createEditor(PreConditionData data, AdventureData adventureData) {
        ConditionEditorComponent editor = switch (data) {
            case CarriedConditionData d -> new CarriedConditionEditor(d, adventureData);
            case HereConditionData d -> new HereConditionEditor(d, adventureData);
            case WornConditionData d -> new WornConditionEditor(d, adventureData);
            case PlayerAtConditionData d -> new PlayerAtConditionEditor(d, adventureData);
            case ItemAtConditionData d -> new ItemAtConditionEditor(d, adventureData);
            case EqualsConditionData d -> new EqualsConditionEditor(d);
            case GreaterThanConditionData d -> new GreaterThanConditionEditor(d);
            case LowerThanConditionData d -> new LowerThanConditionEditor(d);
            case SameConditionData d -> new SameConditionEditor(d);
            default -> throw new UnsupportedOperationException(
                    "No editor available for condition type: " + data.getClass().getSimpleName());
        };
        editor.initialize();
        return editor;
    }
}
