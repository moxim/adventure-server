package com.pdg.adventure.server.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.action.DropAction;
import com.pdg.adventure.server.action.RemoveAction;
import com.pdg.adventure.server.action.TakeAction;
import com.pdg.adventure.server.action.WearAction;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * Makes the take, drop, wear and remove commands authored on items pick the same item the AutoTake/AutoDrop/AutoWear/
 * AutoRemove actions would (see {@code AbstractNounItemAction#resolve}): take honours the <b>location</b>, the others
 * the <b>pocket</b>; a typed adjective wins over the container; only a clash <i>inside</i> the honoured container is a
 * question; items that differ in nothing (same adjective and noun) are one. For wear and remove only fitting items
 * count: wearable and not yet worn, respectively worn - an item that is not carried can be neither worn nor removed.
 * <p>
 * It looks at the candidate chains before {@link CommandExecutor} ranks them, and recognises a chain by the
 * {@link TakeAction}/{@link DropAction}/{@link WearAction}/{@link RemoveAction} inside it - a chain built from other
 * actions is left to the ranking.
 */
final class ItemChainResolver {

    private enum Kind { TAKE, DROP, WEAR, REMOVE }

    private record ItemChain(CommandChain chain, Item item, Kind kind) {
        String identity() {
            return item.getAdjective() + '\u0000' + item.getNoun();
        }

        // whether the command can do something to this item right now, apart from where it is
        boolean fits() {
            return switch (kind) {
                case WEAR -> item.isWearable() && !item.isWorn();
                case REMOVE -> item.isWorn();
                case TAKE, DROP -> true;
            };
        }
    }

    private ItemChainResolver() {
        // don't instantiate me
    }

    /**
     * Removes from {@code someAvailableChains} the take/drop chains that must not take part, and keeps one chain per
     * set of identical items.
     *
     * @param somePocketChains   the candidate chains that came from the pocket
     * @param someLocationChains the candidate chains that came from the location
     * @return a failure to report as it is (the match is only in the container the command does not honour and, for
     * take/drop, is not a single item; or several carried items none of which wear/remove can act on), or empty when
     * the executor should carry on with what is left
     */
    static Optional<ExecutionResult> narrow(List<CommandChain> somePocketChains, List<CommandChain> someLocationChains,
                                            List<CommandChain> someAvailableChains, CommandDescription aCommand) {
        if (VocabularyData.EMPTY_STRING.equals(aCommand.getNoun())) {
            return Optional.empty(); // a bare "take" asks "what?" - there is no item to resolve
        }
        List<ItemChain> itemChains = someAvailableChains.stream().map(ItemChainResolver::describe)
                                                        .flatMap(Optional::stream).toList();
        if (itemChains.isEmpty() || itemChains.stream().map(ItemChain::kind).distinct().count() > 1) {
            return Optional.empty();
        }
        Kind kind = itemChains.getFirst().kind();

        itemChains = applyTypedAdjective(itemChains, someAvailableChains, aCommand.getAdjective());

        Set<CommandChain> honouredOrigin = Collections.newSetFromMap(new IdentityHashMap<>());
        honouredOrigin.addAll(kind == Kind.TAKE ? someLocationChains : somePocketChains);
        List<ItemChain> honoured = itemChains.stream().filter(c -> honouredOrigin.contains(c.chain())).toList();

        if (!honoured.isEmpty()) {
            itemChains.stream().filter(c -> !honouredOrigin.contains(c.chain()))
                      .forEach(c -> someAvailableChains.remove(c.chain()));
            return narrowHonoured(kind, honoured, someAvailableChains, aCommand);
        }
        return narrowOther(kind, itemChains, someAvailableChains, aCommand);
    }

    // The honoured container holds candidates: for wear/remove only the fitting ones count.
    private static Optional<ExecutionResult> narrowHonoured(Kind aKind, List<ItemChain> someHonoured,
                                                            List<CommandChain> someAvailableChains,
                                                            CommandDescription aCommand) {
        List<ItemChain> candidates = someHonoured;
        if (aKind == Kind.WEAR || aKind == Kind.REMOVE) {
            List<ItemChain> fitting = someHonoured.stream().filter(ItemChain::fits).toList();
            if (!fitting.isEmpty()) {
                someHonoured.stream().filter(c -> !c.fits()).forEach(c -> someAvailableChains.remove(c.chain()));
                candidates = fitting;
            } else if (distinctItems(someHonoured) > 1) {
                // several carried suits, none of which the command can act on: say so for all of them
                SystemMessageKey key = aKind == Kind.REMOVE ? SystemMessageKey.SM50
                                                            : someHonoured.getFirst().item().isWorn()
                                                              ? SystemMessageKey.SM29 : SystemMessageKey.SM40;
                return Optional.of(refusal(key, aCommand.getNoun()));
            }
        }
        keepOnePerIdenticalItem(candidates, someAvailableChains);
        return Optional.empty();
    }

    // Candidates only in the container the command does not honour.
    private static Optional<ExecutionResult> narrowOther(Kind aKind, List<ItemChain> someItemChains,
                                                         List<CommandChain> someAvailableChains,
                                                         CommandDescription aCommand) {
        if (aKind == Kind.WEAR || aKind == Kind.REMOVE) {
            // an item that is not carried can be neither worn nor taken off, whatever the item's own command says
            SystemMessageKey key = aKind == Kind.WEAR ? SystemMessageKey.SM49 : SystemMessageKey.SM50;
            String name = distinctItems(someItemChains) == 1
                          ? someItemChains.getFirst().item().getStrippedBasicDescription() : aCommand.getNoun();
            return Optional.of(refusal(key, name));
        }
        if (distinctItems(someItemChains) > 1) {
            SystemMessageKey key = aKind == Kind.TAKE ? SystemMessageKey.SM25 : SystemMessageKey.SM49;
            return Optional.of(refusal(key, aCommand.getNoun()));
        }
        keepOnePerIdenticalItem(someItemChains, someAvailableChains);
        return Optional.empty();
    }

    private static ExecutionResult refusal(SystemMessageKey aKey, String aName) {
        return new CommandExecutionResult(ExecutionResult.State.FAILURE, aKey.defaultText().formatted(aName));
    }

    // A typed adjective picks its items in either container; only when none has it is the adjective ignored.
    private static List<ItemChain> applyTypedAdjective(List<ItemChain> someItemChains,
                                                       List<CommandChain> someAvailableChains, String anAdjective) {
        if (VocabularyData.EMPTY_STRING.equals(anAdjective)
            || someItemChains.stream().noneMatch(c -> c.item().getAdjective().equals(anAdjective))) {
            return someItemChains;
        }
        someItemChains.stream().filter(c -> !c.item().getAdjective().equals(anAdjective))
                      .forEach(c -> someAvailableChains.remove(c.chain()));
        return someItemChains.stream().filter(c -> c.item().getAdjective().equals(anAdjective)).toList();
    }

    private static void keepOnePerIdenticalItem(List<ItemChain> someItemChains, List<CommandChain> someAvailableChains) {
        Set<String> seen = new HashSet<>();
        for (ItemChain itemChain : someItemChains) {
            if (!seen.add(itemChain.identity())) {
                someAvailableChains.remove(itemChain.chain());
            }
        }
    }

    private static long distinctItems(List<ItemChain> someItemChains) {
        return someItemChains.stream().map(ItemChain::identity).distinct().count();
    }

    private static Optional<ItemChain> describe(CommandChain aChain) {
        for (Command command : aChain.getCommands()) {
            for (Action action : command.getActions()) {
                if (action instanceof TakeAction take) {
                    return Optional.of(new ItemChain(aChain, take.getItem(), Kind.TAKE));
                }
                if (action instanceof DropAction drop) {
                    return Optional.of(new ItemChain(aChain, drop.getItem(), Kind.DROP));
                }
                if (action instanceof WearAction wear && wear.getThing() instanceof Item item) {
                    return Optional.of(new ItemChain(aChain, item, Kind.WEAR));
                }
                if (action instanceof RemoveAction remove && remove.getThing() instanceof Item item) {
                    return Optional.of(new ItemChain(aChain, item, Kind.REMOVE));
                }
            }
        }
        return Optional.empty();
    }
}
