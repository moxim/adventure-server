package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.tangible.Item;

/**
 * Base for the AutoTake/AutoDrop actions, which - unlike Take/Drop - are not bound to one item: they
 * resolve the item from the noun (and adjective) the player typed in the current sub-command,
 * the way the original PAW's AUTOG/AUTOD look the noun up in the object word table.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
abstract class AbstractNounItemAction extends AbstractAction {

    private final transient GameContext gameContext;
    private final transient Map<String, Item> allItems;

    AbstractNounItemAction(GameContext aGameContext, Map<String, Item> anAllItems) {
        gameContext = aGameContext;
        allItems = anAllItems;
    }

    GameContext gameContext() {
        return gameContext;
    }

    Container pocket() {
        return gameContext.getPocket();
    }

    Container here() {
        return gameContext.getCurrentLocation().getItemContainer();
    }

    String noun() {
        return gameContext.getCurrentNoun();
    }

    boolean hasNoun() {
        return !VocabularyData.EMPTY_STRING.equals(noun());
    }

    Optional<Item> findIn(Container aContainer) {
        return findMatching(aContainer.getContents());
    }

    /** True if the typed noun names an item anywhere in the adventure (i.e. it is an "object"). */
    boolean isKnownObject() {
        return findMatching(allItems.values()).isPresent();
    }

    // The adjective narrows the match when it finds something, but is otherwise ignored - the
    // same leniency ItemIdentifier applies to ordinary item commands.
    private Optional<Item> findMatching(Collection<? extends Containable> aCandidates) {
        String adjective = gameContext.getCurrentAdjective();
        Optional<Item> byNoun = Optional.empty();
        for (Containable candidate : aCandidates) {
            if (candidate instanceof Item item && item.getNoun().equals(noun())) {
                if (!VocabularyData.EMPTY_STRING.equals(adjective) && item.getAdjective().equals(adjective)) {
                    return Optional.of(item);
                }
                if (byNoun.isEmpty()) {
                    byNoun = Optional.of(item);
                }
            }
        }
        return byNoun;
    }

    static ExecutionResult failure(String aMessage) {
        return new CommandExecutionResult(ExecutionResult.State.FAILURE, aMessage);
    }
}
