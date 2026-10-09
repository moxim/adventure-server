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
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.AutoDropActionData;
import com.pdg.adventure.model.action.AutoRemoveActionData;
import com.pdg.adventure.model.action.AutoTakeActionData;
import com.pdg.adventure.model.action.ExamineActionData;
import com.pdg.adventure.model.action.LookActionData;
import com.pdg.adventure.model.action.AutoWearActionData;
import com.pdg.adventure.model.action.BreakActionData;
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.model.action.DestroyActionData;
import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.model.action.InventoryActionData;
import com.pdg.adventure.model.action.LoadGameActionData;
import com.pdg.adventure.model.action.LightActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.model.action.SaveGameActionData;
import com.pdg.adventure.model.action.RemoveActionData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.model.action.WearActionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.ChanceConditionData;
import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.model.condition.LessThanConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.Adjective2ConditionData;
import com.pdg.adventure.model.condition.AdverbConditionData;
import com.pdg.adventure.model.condition.Noun2ConditionData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.model.condition.PrepositionConditionData;
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
    private final Map<String, PictureData> picturesById;
    private final Map<String, MessageData> messagesById;

    public PreconditionActionFormatter(AdventureData adventureData) {
        itemsById = indexItems(adventureData);
        locationsById = adventureData.getLocationData() == null ? Map.of() : adventureData.getLocationData();
        picturesById = adventureData.getPictureData() == null ? Map.of() : adventureData.getPictureData();
        messagesById = adventureData.getMessages() == null ? Map.of() : adventureData.getMessages();
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
            return "EQ " + txt(eq.getVariableName()) + " " + num(eq.getValue());
        }
        if (c instanceof GreaterThanConditionData gt) {
            return "GT " + txt(gt.getVariableName()) + " " + num(gt.getValue());
        }
        if (c instanceof LessThanConditionData lt) {
            return "LT " + txt(lt.getVariableName()) + " " + num(lt.getValue());
        }
        if (c instanceof SameConditionData same) {
            return "SAME " + txt(same.getVariableNameOne()) + " " + txt(same.getVariableNameTwo());
        }
        if (c instanceof ChanceConditionData chance) {
            return "CHANCE " + num(chance.getValue());
        }
        if (c instanceof PrepositionConditionData preposition) {
            return "PREPOSITION " + txt(preposition.getPrepositionText());
        }
        if (c instanceof AdverbConditionData adverb) {
            return "ADVERB " + txt(adverb.getAdverbText());
        }
        if (c instanceof Noun2ConditionData noun2) {
            return "NOUN2 " + txt(noun2.getNoun2Text());
        }
        if (c instanceof Adjective2ConditionData adjective2) {
            return "ADJECTIVE2 " + txt(adjective2.getAdjective2Text());
        }
        return c.getPreconditionName().replace("ConditionData", "").toUpperCase(Locale.ROOT);
    }

    public List<String> formatActions(List<ActionData> actions) {
        if (actions == null) {
            return List.of();
        }
        return actions.stream().map(this::formatAction).collect(Collectors.toList());
    }

    public String formatAction(ActionData a) {
        if (a == null) {
            return "?";
        }
        if (a instanceof SetVariableActionData sv) {
            return "SETVAR " + txt(sv.getVariableName()) + " " + num(sv.getVariableValue());
        }
        if (a instanceof IncrementVariableActionData iv) {
            return "INCVAR " + txt(iv.getName()) + " " + num(iv.getValue());
        }
        if (a instanceof DecrementVariableActionData dv) {
            return "DECVAR " + txt(dv.getName()) + " " + num(dv.getValue());
        }
        if (a instanceof MessageActionData m) {
            return "MESSAGE " + resolveMessage(m.getMessageId());
        }
        if (a instanceof CreateActionData cr) {
            return "CREATE " + resolveName(cr.getThingId());
        }
        if (a instanceof DestroyActionData d) {
            return "DESTROY " + resolveName(d.getThingId());
        }
        if (a instanceof DropActionData d) {
            return "DROP " + resolveName(d.getThingId());
        }
        if (a instanceof TakeActionData t) {
            return "TAKE " + resolveName(t.getThingId());
        }
        if (a instanceof WearActionData w) {
            return "WEAR " + resolveName(w.getThingId());
        }
        if (a instanceof RemoveActionData r) {
            return "REMOVE " + resolveName(r.getThingId());
        }
        if (a instanceof LightActionData l) {
            return "LIGHT " + resolveName(l.getThingId()) + " " + num(l.getLumen());
        }
        if (a instanceof MoveItemActionData mi) {
            return "MOVE_ITEM " + resolveName(mi.getThingId()) + " " + resolveName(mi.getDestinationId());
        }
        if (a instanceof MovePlayerActionData mp) {
            return "MOVE_PLAYER " + resolveName(mp.getLocationId());
        }
        if (a instanceof DescribeActionData de) {
            return "DESCRIBE " + resolveName(de.getTargetId());
        }
        if (a instanceof PictureActionData p) {
            return "PICTURE " + resolvePictureName(p.getPictureId());
        }
        if (a instanceof LookActionData) {
            return "LOOK";
        }
        if (a instanceof ExamineActionData) {
            return "EXAMINE";
        }
        if (a instanceof AutoTakeActionData) {
            return "AUTOTAKE";
        }
        if (a instanceof AutoDropActionData) {
            return "AUTODROP";
        }
        if (a instanceof AutoWearActionData) {
            return "AUTOWEAR";
        }
        if (a instanceof AutoRemoveActionData) {
            return "AUTOREMOVE";
        }
        if (a instanceof SaveGameActionData) {
            return "SAVE";
        }
        if (a instanceof LoadGameActionData) {
            return "LOAD";
        }
        if (a instanceof InventoryActionData) {
            return "INVENTORY";
        }
        if (a instanceof QuitActionData) {
            return "QUIT";
        }
        if (a instanceof BreakActionData) {
            return "BREAK";
        }
        return a.getActionName().replace("ActionData", "").toUpperCase(Locale.ROOT);
    }

    private String resolvePictureName(String pictureId) {
        if (pictureId == null || pictureId.isBlank()) {
            return "?";
        }
        PictureData picture = picturesById.get(pictureId);
        return picture == null ? "?" : picture.getName();
    }

    /**
     * The summary of the referenced message; an id that matches no message is shown verbatim,
     * because at runtime it is then used as the literal message text.
     */
    private String resolveMessage(String id) {
        MessageData message = id == null ? null : messagesById.get(id);
        if (message != null && message.getSummary() != null && !message.getSummary().isBlank()) {
            return message.getSummary();
        }
        return txt(id);
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
