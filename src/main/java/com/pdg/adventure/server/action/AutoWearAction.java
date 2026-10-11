package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Map;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * AutoWear - the wear counterpart of {@link AutoTakeAction}: puts on the carried item the player named
 * with the noun of the current sub-command.
 * <p>
 * Honours the pocket first: with one matching item carried and another here, the carried one is worn; with several
 * carried, the player is asked which (SM61). Worn and unwearable items are not candidates while a wearable unworn one
 * is carried.
 * <p>
 * Outcomes: worn (SM37); already worn (SM29); carried but not wearable (SM40); several carried (SM61); the item is here
 * but not carried (SM49); no noun, or an item that is elsewhere (SM28); the noun is a vocabulary
 * word but no item (SM8).
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AutoWearAction extends AbstractNounItemAction {

    public AutoWearAction(GameContext aGameContext, Map<String, Item> anAllItems) {
        super(aGameContext, anAllItems);
    }

    @Override
    public ExecutionResult execute() {
        if (!hasNoun()) {
            return failure(SystemMessageKey.SM28.defaultText());
        }
        // honours the pocket: an unworn, wearable carried item is worn; one that is only here is not ours to wear
        Resolution resolution = resolve(pocket(), here(), item -> item.isWearable() && !item.isWorn());
        return switch (resolution.kind()) {
            case ELSEWHERE -> failure(SystemMessageKey.SM49.defaultText().formatted(resolution.name()));
            case AMBIGUOUS -> ambiguity();
            case UNSUITABLE -> resolution.item().isWorn()
                               ? failure(SystemMessageKey.SM29.defaultText().formatted(resolution.name()))
                               : new WearAction(resolution.item()).execute();
            case NONE -> failure(isKnownObject() ? SystemMessageKey.SM28.defaultText() : SystemMessageKey.SM8.defaultText());
            case FOUND -> new WearAction(resolution.item()).execute();
        };
    }
}
