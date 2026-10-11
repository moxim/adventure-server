package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Map;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * AutoDrop - the adventure builder's counterpart of PAW's AUTOD: drops the carried item the player
 * named with the noun of the current sub-command into the current location.
 * <p>
 * Honours the pocket first: with one matching item carried and another here, the carried one is dropped; with
 * several carried, the player is asked which (SM61). Worn items are not candidates while an unworn one is carried.
 * <p>
 * Outcomes: dropped (SM39); carried but worn (SM24 - unlike DropAction this refuses instead of
 * silently un-wearing, as PAW does); several carried (SM61); the item is here but not carried (SM49); no noun, or an
 * item that is elsewhere (SM28); the noun is a vocabulary word but no item (SM8).
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
        // honours the pocket: an unworn carried item is dropped; one that is only here is not ours to drop
        Resolution resolution = resolve(pocket(), here(), item -> !item.isWorn());
        return switch (resolution.kind()) {
            case ELSEWHERE -> failure(SystemMessageKey.SM49.defaultText().formatted(resolution.name()));
            case AMBIGUOUS -> ambiguity();
            case UNSUITABLE -> failure(SystemMessageKey.SM24.defaultText().formatted(resolution.name()));
            case NONE -> failure(isKnownObject() ? SystemMessageKey.SM28.defaultText() : SystemMessageKey.SM8.defaultText());
            case FOUND -> drop(resolution.item());
        };
    }

    private ExecutionResult drop(Item anItem) {
        ExecutionResult result = new MoveItemAction(anItem, here()).execute();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            result.setResultMessage(SystemMessageKey.SM39.defaultText().formatted(anItem.getStrippedBasicDescription()));
        }
        return result;
    }
}
