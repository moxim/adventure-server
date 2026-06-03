package com.pdg.adventure.view.command;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.model.condition.LowerThanConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Renders preconditions and actions to compact, single-line text for the commands listing grid
 * (e.g. "NOT_HERE dragon", "SETVAR b_fill 1"). Pure: depends only on the supplied AdventureData
 * for id -> display-name resolution. No Vaadin UI, so it is unit-testable in isolation.
 */
public class PreconditionActionFormatter {
    private final Map<String, ItemData> itemsById;
    private final Map<String, LocationData> locationsById;

    public PreconditionActionFormatter(AdventureData adventureData) {
        itemsById = indexItems(adventureData);
        locationsById = adventureData.getLocationData() == null ? Map.of() : adventureData.getLocationData();
    }

    public List<String> formatConditions(List<PreConditionData> conditions) {
        if (conditions == null) {
            return List.of();
        }
        return conditions.stream().map(this::formatCondition).collect(Collectors.toList());
    }

    public String formatCondition(PreConditionData c) {
        if (c == null) {
            return "?";
        }
        if (c instanceof NotConditionData not) {
            return "NOT_" + formatCondition(not.getPreCondition());
        }
        if (c instanceof HereConditionData here) {
            return "HERE " + resolveName(here.getThingId());
        }
        if (c instanceof CarriedConditionData carried) {
            return "CARRIED " + resolveName(carried.getItemId());
        }
        if (c instanceof WornConditionData worn) {
            return "WORN " + resolveName(worn.getThingId());
        }
        if (c instanceof PlayerAtConditionData playerAt) {
            return "PLAYER_AT " + resolveName(playerAt.getLocationId());
        }
        if (c instanceof ItemAtConditionData itemAt) {
            return "ITEM_AT " + resolveName(itemAt.getThingId()) + " " + resolveName(itemAt.getLocationId());
        }
        if (c instanceof EqualsConditionData eq) {
            return "EQ " + txt(eq.getVariableName()) + " " + txt(eq.getValue());
        }
        if (c instanceof GreaterThanConditionData gt) {
            return "GT " + txt(gt.getVariableName()) + " " + num(gt.getValue());
        }
        if (c instanceof LowerThanConditionData lt) {
            return "LT " + txt(lt.getVariableName()) + " " + num(lt.getValue());
        }
        if (c instanceof SameConditionData same) {
            return "SAME " + txt(same.getVariableNameOne()) + " " + txt(same.getVariableNameTwo());
        }
        return c.getPreconditionName().replace("ConditionData", "").toUpperCase(Locale.ROOT);
    }

    private String resolveName(String id) {
        if (id == null || id.isBlank()) {
            return "?";
        }
        ItemData item = itemsById.get(id);
        if (item != null) {
            return ViewSupporter.formatDescription(item);
        }
        LocationData location = locationsById.get(id);
        if (location != null) {
            return ViewSupporter.getLocationsShortedDescription(location);
        }
        return id;
    }

    private static String txt(String s) {
        return (s == null || s.isBlank()) ? "?" : s;
    }

    private static String num(Number n) {
        return n == null ? "?" : String.valueOf(n);
    }

    private static Map<String, ItemData> indexItems(AdventureData data) {
        Map<String, ItemData> map = new HashMap<>();
        if (data.getLocationData() != null) {
            for (LocationData loc : data.getLocationData().values()) {
                ItemContainerData container = loc.getItemContainerData();
                if (container != null && container.getItems() != null) {
                    container.getItems().forEach(i -> map.put(i.getId(), i));
                }
            }
        }
        if (data.getPlayerPocket() != null && data.getPlayerPocket().getItems() != null) {
            data.getPlayerPocket().getItems().forEach(i -> map.put(i.getId(), i));
        }
        return map;
    }
}
