package com.pdg.adventure.view.command;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        return conditions.stream().map(this::formatCondition).toList();
    }

    public String formatCondition(PreConditionData c) {
        return switch (c) {
            case null -> "?";
            case NotConditionData not -> "NOT_" + formatCondition(not.getPreCondition());
            case HereConditionData here -> "HERE " + resolveName(here.getThingId());
            case CarriedConditionData carried -> "CARRIED " + resolveName(carried.getItemId());
            case WornConditionData worn -> "WORN " + resolveName(worn.getThingId());
            case PlayerAtConditionData playerAt -> "PLAYER_AT " + resolveName(playerAt.getLocationId());
            case ItemAtConditionData itemAt -> "ITEM_AT " + resolveName(itemAt.getThingId()) + " " + resolveName(itemAt.getLocationId());
            case EqualsConditionData eq -> "EQ " + txt(eq.getVariableName()) + " " + num(eq.getValue());
            case GreaterThanConditionData gt -> "GT " + txt(gt.getVariableName()) + " " + num(gt.getValue());
            case LessThanConditionData lt -> "LT " + txt(lt.getVariableName()) + " " + num(lt.getValue());
            case SameConditionData same -> "SAME " + txt(same.getVariableNameOne()) + " " + txt(same.getVariableNameTwo());
            case ChanceConditionData chance -> "CHANCE " + num(chance.getValue());
            case PrepositionConditionData preposition -> "PREPOSITION " + txt(preposition.getPrepositionText());
            case AdverbConditionData adverb -> "ADVERB " + txt(adverb.getAdverbText());
            case Noun2ConditionData noun2 -> "NOUN2 " + txt(noun2.getNoun2Text());
            case Adjective2ConditionData adjective2 -> "ADJECTIVE2 " + txt(adjective2.getAdjective2Text());
            default -> c.getPreconditionName().replace("ConditionData", "").toUpperCase(Locale.ROOT);
        };
    }

    public List<String> formatActions(List<ActionData> actions) {
        if (actions == null) {
            return List.of();
        }
        return actions.stream().map(this::formatAction).toList();
    }

    public String formatAction(ActionData a) {
        return switch (a) {
            case null -> "?";
            case SetVariableActionData sv -> "SETVAR " + txt(sv.getVariableName()) + " " + num(sv.getVariableValue());
            case IncrementVariableActionData iv -> "INCVAR " + txt(iv.getName()) + " " + num(iv.getValue());
            case DecrementVariableActionData dv -> "DECVAR " + txt(dv.getName()) + " " + num(dv.getValue());
            case MessageActionData m -> "MESSAGE " + resolveMessage(m.getMessageId());
            case CreateActionData cr -> "CREATE " + resolveName(cr.getThingId());
            case DestroyActionData d -> "DESTROY " + resolveName(d.getThingId());
            case DropActionData d -> "DROP " + resolveName(d.getThingId());
            case TakeActionData t -> "TAKE " + resolveName(t.getThingId());
            case WearActionData w -> "WEAR " + resolveName(w.getThingId());
            case RemoveActionData r -> "REMOVE " + resolveName(r.getThingId());
            case LightActionData l -> "LIGHT " + resolveName(l.getThingId()) + " " + num(l.getLumen());
            case MoveItemActionData mi -> "MOVE_ITEM " + resolveName(mi.getThingId()) + " " + resolveName(mi.getDestinationId());
            case MovePlayerActionData mp -> "MOVE_PLAYER " + resolveName(mp.getLocationId());
            case DescribeActionData de -> "DESCRIBE " + resolveName(de.getTargetId());
            case PictureActionData p -> "PICTURE " + resolvePictureName(p.getPictureId());
            case LookActionData _ -> "LOOK";
            case ExamineActionData _ -> "EXAMINE";
            case AutoTakeActionData _ -> "AUTOTAKE";
            case AutoDropActionData _ -> "AUTODROP";
            case AutoWearActionData _ -> "AUTOWEAR";
            case AutoRemoveActionData _ -> "AUTOREMOVE";
            case SaveGameActionData _ -> "SAVE";
            case LoadGameActionData _ -> "LOAD";
            case InventoryActionData _ -> "INVENTORY";
            case QuitActionData _ -> "QUIT";
            case BreakActionData _ -> "BREAK";
            default -> a.getActionName().replace("ActionData", "").toUpperCase(Locale.ROOT);
        };
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
