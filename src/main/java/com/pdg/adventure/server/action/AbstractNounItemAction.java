package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
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

    String verb() {
        return gameContext.getCurrentVerb();
    }

    /** The typed noun (narrowed by the adjective when one matches) among the items of one container; first match wins. */
    Optional<Item> findIn(Container aContainer) {
        String adjective = gameContext.getCurrentAdjective();
        Optional<Item> byNoun = Optional.empty();
        for (Item item : itemsNamedByNoun(aContainer.getContents())) {
            if (!VocabularyData.EMPTY_STRING.equals(adjective) && item.getAdjective().equals(adjective)) {
                return Optional.of(item);
            }
            if (byNoun.isEmpty()) {
                byNoun = Optional.of(item);
            }
        }
        return byNoun;
    }

    /** True if the typed noun names an item anywhere in the adventure (i.e. it is an "object"). */
    boolean isKnownObject() {
        return !itemsNamedByNoun(allItems.values()).isEmpty();
    }

    /** The message for a clash inside the honoured container, e.g. "Which suit should I take?" (SM61). */
    ExecutionResult ambiguity() {
        return failure(SystemMessageKey.SM61.defaultText().formatted(noun(), verb()));
    }

    /**
     * Finds the item the player means, the way the original PAW tells AUTOG/AUTOD apart: every action <b>honours one
     * container first</b> (AutoTake the location, the others the pocket) and only a clash <i>inside</i> that container
     * is ambiguous - the other container is consulted only to explain why nothing happened.
     * <ol>
     *   <li>Candidates are the items of both containers named by the typed noun. A typed adjective narrows them to
     *   the exact-adjective ones whenever any exists in either container; otherwise it is ignored.</li>
     *   <li>If the honoured container holds candidates, those that are <i>suitable</i> (e.g. worn, for remove) decide:
     *   one distinct item is {@link Kind#FOUND}, several are {@link Kind#AMBIGUOUS}, none is {@link Kind#UNSUITABLE}.
     *   Items with the same adjective count as one - identical coins are not a question.</li>
     *   <li>Otherwise candidates in the other container are {@link Kind#ELSEWHERE}; no candidates at all is
     *   {@link Kind#NONE}.</li>
     * </ol>
     */
    Resolution resolve(Container aHonoured, Container anOther, Predicate<Item> aSuitable) {
        List<Item> honoured = itemsNamedByNoun(aHonoured.getContents());
        List<Item> other = itemsNamedByNoun(anOther.getContents());
        String adjective = gameContext.getCurrentAdjective();
        if (!VocabularyData.EMPTY_STRING.equals(adjective)
            && Stream.concat(honoured.stream(), other.stream()).anyMatch(item -> item.getAdjective().equals(adjective))) {
            honoured = honoured.stream().filter(item -> item.getAdjective().equals(adjective)).toList();
            other = other.stream().filter(item -> item.getAdjective().equals(adjective)).toList();
        }
        if (!honoured.isEmpty()) {
            List<Item> suitable = honoured.stream().filter(aSuitable).toList();
            if (suitable.isEmpty()) {
                return new Resolution(Kind.UNSUITABLE, honoured.getFirst(), nameOf(honoured));
            }
            Kind kind = distinctAdjectives(suitable) > 1 ? Kind.AMBIGUOUS : Kind.FOUND;
            return new Resolution(kind, suitable.getFirst(), nameOf(suitable));
        }
        if (!other.isEmpty()) {
            return new Resolution(Kind.ELSEWHERE, other.getFirst(), nameOf(other));
        }
        return new Resolution(Kind.NONE, null, noun());
    }

    private List<Item> itemsNamedByNoun(Collection<? extends Containable> aCandidates) {
        return aCandidates.stream()
                          .filter(Item.class::isInstance)
                          .map(Item.class::cast)
                          .filter(item -> item.getNoun().equals(noun()))
                          .toList();
    }

    private static long distinctAdjectives(List<Item> someItems) {
        return someItems.stream().map(Item::getAdjective).distinct().count();
    }

    // One distinct item can be named; with several the player only gave a noun, so the message uses just that.
    private String nameOf(List<Item> someItems) {
        return distinctAdjectives(someItems) == 1 ? someItems.getFirst().getStrippedBasicDescription() : noun();
    }

    enum Kind {
        /** No item of that noun in either container. */
        NONE,
        /** Exactly one distinct, suitable item in the honoured container. */
        FOUND,
        /** Several distinct, suitable items in the honoured container. */
        AMBIGUOUS,
        /** Items in the honoured container, but none is suitable (e.g. all worn, for drop). */
        UNSUITABLE,
        /** Items only in the other container. */
        ELSEWHERE
    }

    /**
     * @param item the item found, or the first candidate for UNSUITABLE/ELSEWHERE; null for NONE
     * @param name what to call it in a message: its description, or the typed noun when several were possible
     */
    record Resolution(Kind kind, Item item, String name) {
    }

    static ExecutionResult failure(String aMessage) {
        return new CommandExecutionResult(ExecutionResult.State.FAILURE, aMessage);
    }
}
