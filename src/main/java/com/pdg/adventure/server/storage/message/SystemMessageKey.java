package com.pdg.adventure.server.storage.message;

import java.util.Arrays;
import java.util.Optional;

import com.pdg.adventure.server.support.PlaceholderSpec;

/**
 * The fixed catalog of built-in, engine-level messages available for translation/wording edits
 * under "Manage System Messages". This enum is the single source of truth for each message's
 * default (English) text, source location, and translator-facing description.
 * <p>
 * Storage is sparse per adventure: an adventure only has a persisted {@link
 * com.pdg.adventure.model.SystemMessageData} row for a key once an author has actually edited it
 * away from the default. A key with no row for a given adventure simply reads as {@link
 * #defaultText()} - several adventures can therefore "share" an unmodified message without any
 * shared or cross-adventure document ever existing; each adventure's own edits live only in its
 * own {@code AdventureData.systemMessages} map.
 * <p>
 * Ids for entries that already exist in {@code MessagesHolder}'s negative-ID convention are kept
 * verbatim, so a future engine-rewiring phase can swap {@code MessagesHolder.getMessage(id)} for a
 * lookup against this store with no id remapping. New entries (today's raw literals scattered
 * across GameLoop, CommandExecutor, Location, Parser, Conditions, Container and Actions) get a
 * descriptive key instead.
 */
public enum SystemMessageKey {

    // LoadAdventureAction currently never registers ids -1/-2/0 for a real adventure - they're
    // only seeded for the console demo (MiniAdventureContent.setUpMessages()) - a pre-existing
    // gap outside this feature's scope. Catalogued anyway per the "full inventory now" decision.
    CANNOT_DO_THAT_DEFAULT("-1", "You can't do that.", "MiniAdventureContent",
            "Generic fallback shown when a command fails with no specific message."),
    CANNOT_UNDERSTAND_DEFAULT("-2", "I don't understand, please rephrase.", "MiniAdventureContent",
            "Generic fallback shown when input can't be parsed at all."),
    OK_DEFAULT("0", "OK.", "MiniAdventureContent",
            "Generic fallback shown when a command succeeds with no specific message."),
    CANNOT_WEAR("-6", "You can't wear %s.", "WearAction",
            "Shown when wearing an item fails. %s = the item."),
    CANNOT_REMOVE("-7", "You can't remove %s.", "RemoveAction",
            "Shown when removing a worn item fails. %s = the item."),
    CONTAINER_FULL("-8", "The %s is full.", "MoveItemAction",
            "Shown when a container has no room for an item. %s = the container."),
    PUT_INTO_CONTAINER("-9", "You put %1$s into %2$s.", "MoveItemAction",
            "Shown after successfully moving an item into a container. %1$s = the item, %2$s = the container."),
    INVENTORY_HEADER("-10", "You carry:", "InventoryAction",
            "Header printed above the player's carried items."),
    ITEM_EVAPORATES("-11", "The %s evaporates into thin air.", "DestroyAction",
            "Shown when an item is destroyed. %s = the item."),
    ITEM_APPEARS("-12", "A %1$s appears in the %2$s.", "CreateAction",
            "Shown when an item is created into a container. %1$s = the item, %2$s = the container."),
    // Currently never registered for a real adventure - CommandFactory looks this id up
    // unconditionally, which NPEs outside the console demo. A live bug, outside this feature's
    // scope (that's the deferred engine-rewiring phase). Catalogued anyway so it's ready once fixed.
    ALREADY_CARRYING("-13", "You already carry %s.", "CommandFactory",
            "Shown when trying to take an item already carried. %s = the item."),

    GAMELOOP_CANNOT_UNDERSTAND("I don't understand, please rephrase.", "GameLoop",
            "Shown when a turn's input has no verb at all."),
    GAMELOOP_CANNOT_DO_THAT("I can't do that.", "GameLoop",
            "Shown when a command is executed but fails with no specific message."),
    GAMELOOP_DONE("Done.", "GameLoop",
            "Shown when a command is executed and succeeds with no specific message."),
    QUIT_MESSAGE("Bye bye.", "QuitException",
            "Shown when the player quits the game."),
    UNRESOLVED_REFERENCE("I don't know what '%s' refers to.", "Parser",
            "Shown when a pronoun (it, them, ...) has no prior noun to refer to. %s = the pronoun's text."),
    EXECUTOR_DEFAULT_FAILURE("You can't do that.", "CommandExecutor",
            "Default failure message when a command chain fails with no specific message."),
    EXECUTOR_DEFAULT_SUCCESS("OK.", "CommandExecutor",
            "Default success message when a command chain succeeds with no specific message."),
    UNKNOWN_COMMAND("I don't know how to do that.", "CommandExecutor",
            "Shown when no command chain matches the input at all."),
    AMBIGUOUS_VERB_PROMPT("What do you want to %s?", "CommandExecutor",
            "Shown when a verb matches multiple commands and no noun was given. %s = the verb."),
    AMBIGUOUS_NOUN_PROMPT("Which %1$s do you want to %2$s?", "CommandExecutor",
            "Shown when a verb+noun still matches multiple commands. %1$s = the noun, %2$s = the verb."),
    LOCATION_CANNOT_DO_THAT("You can't do that.", "Location",
            "Default failure message for direction/interceptor-level command dispatch."),
    LOCATION_AMBIGUOUS_VERB_PROMPT("What do you want to %s?", "Location",
            "Shown when a verb matches multiple location-level commands. %s = the verb."),
    EXITS_HEADER("Exits are:", "Location",
            "Header printed above a location's list of exits."),
    NO_OBVIOUS_EXITS("There are no obvious exits.", "Location",
            "Shown when a location has no exits to list."),
    VISIBLE_ITEMS_HEADER("You also see:", "Location",
            "Header printed above the items visible in a location."),
    NOT_CARRIED("You don't have a %s.", "CarriedCondition",
            "Shown when a command requires carrying an item the player doesn't have. %s = the item."),
    NOT_WORN("You are not wearing %s.", "WornCondition",
            "Shown when a command requires wearing an item the player isn't wearing. %s = the item."),
    NOT_HERE("There is no %s here.", "HereCondition",
            "Shown when a command requires an item to be present that isn't. %s = the item."),
    ALREADY_PRESENT_IN_CONTAINER("%1$s is already present in the %2$s.", "Container",
            "Shown when adding an item that's already in the container. %1$s = the item, %2$s = the container."),
    CANNOT_PUT_IN_CONTAINER("You can't put the %1$s into the %2$s.", "Container",
            "Shown when an item can't go into a container. %1$s = the item, %2$s = the container."),
    CONTAINER_ALREADY_FULL("%s is already full.", "Container",
            "Shown when a container has no room. %s = the container."),
    CONTAINER_EMPTY_PLACEHOLDER("nothing.", "GenericContainer",
            "Shown in place of a contents list when a container is empty."),
    ITEM_NOT_IN_CONTAINER("There is no %1$s in %2$s.", "GenericContainer",
            "Shown when removing an item that isn't in the container. %1$s = the item, %2$s = the container."),
    HELP_TEXT("Look around, examine items, take or drop items, maybe wear items, enter or leave locations.\nOr quit.",
            "CommandFactory",
            "The full text shown for the built-in 'help' command."),
    NOW_CARRYING("You now carry %s.", "TakeAction",
            "Shown after successfully taking an item. %s = the item.");

    private final String id;
    private final String defaultText;
    private final String sourceLocation;
    private final String description;

    SystemMessageKey(String aDefaultText, String aSourceLocation, String aDescription) {
        this(null, aDefaultText, aSourceLocation, aDescription);
    }

    SystemMessageKey(String anId, String aDefaultText, String aSourceLocation, String aDescription) {
        id = anId != null ? anId : name();
        defaultText = aDefaultText;
        sourceLocation = aSourceLocation;
        description = aDescription;
        PlaceholderSpec.of(aDefaultText); // fail fast at class-load if a seed literal is malformed
    }

    public String id() {
        return id;
    }

    public String defaultText() {
        return defaultText;
    }

    public String sourceLocation() {
        return sourceLocation;
    }

    public String description() {
        return description;
    }

    public static Optional<SystemMessageKey> fromId(String anId) {
        return Arrays.stream(values()).filter(key -> key.id.equals(anId)).findFirst();
    }
}
