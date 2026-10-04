package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Optional;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * AUTOT - the adventure builder's counterpart of PAW's AUTOG: picks up the item the player named
 * with the noun of the current sub-command, moving it from the location into the pocket.
 * <p>
 * Outcomes: taken (SM36); already carried or worn (SM25); the noun names an item, but not one
 * that is here (SM26); no noun at all - the parser drops words it doesn't know, so "take xyzzy"
 * arrives here as a bare "take" (SM26); the noun is a vocabulary word but no item (SM8); pocket
 * full (SM27).
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AutoTakeAction extends AbstractNounItemAction {

    public AutoTakeAction(GameContext aGameContext, Map<String, Item> anAllItems) {
        super(aGameContext, anAllItems);
    }

    @Override
    public ExecutionResult execute() {
        if (!hasNoun()) {
            return failure(SystemMessageKey.SM26.defaultText());
        }
        Optional<Item> carried = findIn(pocket());
        if (carried.isPresent()) {
            return failure(SystemMessageKey.SM25.defaultText().formatted(carried.get().getStrippedBasicDescription()));
        }
        Optional<Item> here = findIn(here());
        if (here.isEmpty()) {
            return failure(isKnownObject() ? SystemMessageKey.SM26.defaultText() : SystemMessageKey.SM8.defaultText());
        }
        Item item = here.get();
        if (!item.isContainable()) {
            return failure(SystemMessageKey.SM8.defaultText());
        }
        if (pocket().getSize() >= pocket().getMaxSize()) {
            return failure(SystemMessageKey.SM27.defaultText());
        }
        // TODO: Review needed — the original PAW also refuses when the object's weight would exceed
        //  the player's limit; this adventure builder has no weight system yet, so only the
        //  item-count limit above applies.
        ExecutionResult result = new MoveItemAction(item, pocket()).execute();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            result.setResultMessage(SystemMessageKey.SM36.defaultText().formatted(item.getStrippedBasicDescription()));
        }
        return result;
    }
}
