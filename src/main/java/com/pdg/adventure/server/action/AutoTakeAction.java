package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Map;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * AutoTake - the adventure builder's counterpart of PAW's AUTOG: picks up the item the player named
 * with the noun of the current sub-command, moving it from the location into the pocket.
 * <p>
 * Honours the location first: with one matching item here and another in the pocket, the one here is taken; with
 * several matching items here, the player is asked which (SM61). Only when nothing matching is here does a
 * matching item in the pocket answer "already have it" (SM25).
 * <p>
 * Outcomes: taken (SM36); already carried or worn (SM25); several here (SM61); the noun names an item, but not
 * one that is here (SM26); no noun at all - the parser drops words it doesn't know, so "take xyzzy"
 * arrives here as a bare "take" (SM26); the noun is a vocabulary word but no item, or a fixed item (SM8); pocket
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
        // honours the location: an item in the pocket only matters when nothing suitable is here
        Resolution resolution = resolve(here(), pocket(), Item::isContainable);
        return switch (resolution.kind()) {
            case ELSEWHERE -> failure(SystemMessageKey.SM25.defaultText().formatted(resolution.name()));
            case AMBIGUOUS -> ambiguity();
            case UNSUITABLE -> failure(SystemMessageKey.SM8.defaultText());
            case NONE -> failure(isKnownObject() ? SystemMessageKey.SM26.defaultText() : SystemMessageKey.SM8.defaultText());
            case FOUND -> take(resolution.item());
        };
    }

    private ExecutionResult take(Item anItem) {
        if (pocket().getSize() >= pocket().getMaxSize()) {
            return failure(SystemMessageKey.SM27.defaultText());
        }
        // TODO: Review needed — the original PAW also refuses when the object's weight would exceed
        //  the player's limit; this adventure builder has no weight system yet, so only the
        //  item-count limit above applies.
        ExecutionResult result = new MoveItemAction(anItem, pocket()).execute();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            result.setResultMessage(SystemMessageKey.SM36.defaultText().formatted(anItem.getStrippedBasicDescription()));
        }
        return result;
    }
}
