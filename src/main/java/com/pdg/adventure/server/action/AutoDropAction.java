package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Optional;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * AutoDrop - the adventure builder's counterpart of PAW's AUTOD: drops the carried item the player
 * named with the noun of the current sub-command into the current location.
 * <p>
 * Outcomes: dropped (SM39); carried but worn (SM24 - unlike DropAction this refuses instead of
 * silently un-wearing, as PAW does); the item is here but not carried (SM49); no noun, or an item
 * that is elsewhere (SM28); the noun is a vocabulary word but no item (SM8).
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AutoDropAction extends AbstractNounItemAction {

    public AutoDropAction(GameContext aGameContext, Map<String, Item> anAllItems) {
        super(aGameContext, anAllItems);
    }

    @Override
    public ExecutionResult execute() {
        if (!hasNoun()) {
            return failure(SystemMessageKey.SM28.defaultText());
        }
        // strict: a carried "blue suit" is not the "neoprene suit" the player typed
        Optional<Item> carried = findExactlyIn(pocket());
        if (carried.isEmpty()) {
            Optional<Item> here = findIn(here());
            if (here.isPresent()) {
                return failure(SystemMessageKey.SM49.defaultText().formatted(here.get().getStrippedBasicDescription()));
            }
            return failure(isKnownObject() ? SystemMessageKey.SM28.defaultText() : SystemMessageKey.SM8.defaultText());
        }
        Item item = carried.get();
        if (item.isWorn()) {
            return failure(SystemMessageKey.SM24.defaultText().formatted(item.getStrippedBasicDescription()));
        }
        ExecutionResult result = new MoveItemAction(item, here()).execute();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            result.setResultMessage(SystemMessageKey.SM39.defaultText().formatted(item.getStrippedBasicDescription()));
        }
        return result;
    }
}
