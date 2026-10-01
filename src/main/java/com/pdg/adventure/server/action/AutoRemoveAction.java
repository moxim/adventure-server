package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Optional;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * AUTOR - the remove counterpart of {@link AutoTakeAction}: takes off the worn item the player
 * named with the noun of the current sub-command. The item stays in the pocket.
 * <p>
 * Outcomes: removed (SM38); carried or here but not worn (SM50); no noun, or an item that is
 * elsewhere (SM23); the noun is a vocabulary word but no item (SM8).
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AutoRemoveAction extends AbstractNounItemAction {

    public AutoRemoveAction(GameContext aGameContext, Map<String, Item> anAllItems) {
        super(aGameContext, anAllItems);
    }

    @Override
    public ExecutionResult execute() {
        if (!hasNoun()) {
            return failure(SystemMessageKey.SM23.defaultText());
        }
        Optional<Item> candidate = findIn(pocket()).or(() -> findIn(here()));
        if (candidate.isEmpty()) {
            return failure(isKnownObject() ? SystemMessageKey.SM23.defaultText() : SystemMessageKey.SM8.defaultText());
        }
        Item item = candidate.get();
        if (!item.isWorn() || !pocket().contains(item)) {
            return failure(SystemMessageKey.SM50.defaultText().formatted(item.getStrippedBasicDescription()));
        }
        return new RemoveAction(item).execute();
    }
}
