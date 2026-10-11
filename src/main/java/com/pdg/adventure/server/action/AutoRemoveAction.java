package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Map;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * AutoRemove - the remove counterpart of {@link AutoTakeAction}: takes off the worn item the player
 * named with the noun of the current sub-command. The item stays in the pocket.
 * <p>
 * Honours the pocket first: only worn items count, so wearing one suit and carrying another removes the worn one;
 * with several worn matches the player is asked which (SM61).
 * <p>
 * Outcomes: removed (SM38); several worn (SM61); carried or here but not worn (SM50); no noun, or an item that is
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
        // honours the pocket: a worn carried item is taken off; one that is only here is not worn by definition
        Resolution resolution = resolve(pocket(), here(), Item::isWorn);
        return switch (resolution.kind()) {
            case ELSEWHERE, UNSUITABLE -> failure(SystemMessageKey.SM50.defaultText().formatted(resolution.name()));
            case AMBIGUOUS -> ambiguity();
            case NONE -> failure(isKnownObject() ? SystemMessageKey.SM23.defaultText() : SystemMessageKey.SM8.defaultText());
            case FOUND -> new RemoveAction(resolution.item()).execute();
        };
    }
}
