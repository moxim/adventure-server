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


    SM0(0, "It's too dark to see.", "is used instead of the location description when it is dark."),
    SM1(1, "I can also see:", "is printed by LISTOBJ if at least one object is present."),
    SM3(3, "What next?", "is selected randomly unless flag 42 is set to be a valid \n" +
                         "message number."),
    SM4(4, "What should I do now?", "is selected randomly unless flag 42 is set to be a valid message number."),
    SM5(5, "What should I do next?", "is selected randomly unless flag 42 is set to be a valid \n" +
                                     "message number."),
    SM6(6, "I was not able to understand any of that. Please try again.", "is produced by the parser when no further phrase can be understood."),
    SM7(7, "I can't go in that direction.", "is produced if no action was carried out (or NOTDONE was) in Response when the Verb is < 14"),
    SM8(8, "I can't do that.", "is produced if no action was carried out (or NOTDONE was) in Response when the Verb is > 13"),
    SM9(9, "I have with me:", "is printed when there is at least one item carried."),
    SM10(10, " (worn)", "is printed alongside items when they are worn."),
    SM11(11, "nothing at all", "is printed when there are no items."),
    SM12(12, "Are you sure?", "is printed by the QUIT action."),
    SM13(13, "Would you like another go?", "is printed by the END action."),
    SM14(14, "Goodbye...", "is printed by the END action."),
    SM15(15, "OK.", "is printed by the OK action."),
    SM16(16, "Press any key to continue.", "is printed by the ANYKEY action."),
    SM17(17, "You have taken ", "is printed by the TURNS action."),
    SM18(18, "turn", "is printed by the TURNS action."),
    SM19(19, "s", "is printed by the TURNS action."),
    SM20(20, ".", "is printed by the TURNS action."),
    SM21(21, "You have scored ", "is the SCORE action messages."),
    SM22(22, "%", "is part of the SCORE action messages."),
    SM23(23, "I am not wearing any of those.", "is printed when the player tries to remove an item that is not worn."),
    SM24(24, "I can't. I am wearing the %s.", "is printed when the player tries to drop an item that is worn."),
    SM25(25, "I already have the %s.", "is printed when the player tries to take an item that they already have."),
    SM26(26, "There isn't one of those here.", "is printed when the player tries to take an item that isn't present."),
    SM27(27, "I can't carry any more things.", "is printed when the player tries to take an item but their inventory is full."),
    SM28(28, "I don't have one of those.", "is printed when the player tries to drop an item that they don't have."),
    SM29(29, "I'm already wearing the %s.", "is printed when the player tries to perform an action that is not allowed."),
    SM30(30, "Y", "is the positive response expected by END and QUIT."),
    SM31(31, "N", "is the negative response expected by END and QUIT."),
    SM32(32, "More....", "is produced when a screen full of text has appeared."),
    SM33(33, "> ", "is the input marker."),
    SM34(34, "|", "is the cursor."),
    SM35(35, "Time passes...", "is displayed when a timeout occurs"),
    SM36(36, "I now have the %s.", "is printed when the player picks up an item."),
    SM37(37, "I am now wearing the %s.", "is printed when the player wears an item."),
    SM38(38, "I've removed the %s", "is printed when the player removes an item."),
    SM39(39, "I've dropped the %s.", "is printed when the player drops an item."),
    SM40(40, "I can't wear the %s.", "is printed when the player tries to wear an item they can't wear."),
    SM41(41, "I can't remove the %s.", "is printed when the player tries to remove an item they can't remove."),
    SM42(42, "I can't remove the %s. My hands are full.", "is printed when the player tries to remove an item but their hands are full."),
    SM43(43, "The %s weighs too much for me.", "is printed when the player tries to pick up an item that is too heavy."),
    SM44(44, "The %s is in the ", "is printed when the player tries to take an item that is in a container."),
    SM45(45, "The %s isn't in the .", "is printed when the player tries to take an item that isn't in a container."),
    SM46(46, ",", "is the link between objects when listing continuously"),
    SM47(47, " and ", "is the final link between the last two objects when listing"),
    SM48(48, "'", "is the termination of a list of objects (printed by both LISTOBJ and LISTAT, so take care.)"),
    SM49(49, "I don't have the %s.", "is printed when the player  yet more object messages"),
    SM50(50, "I'm not wearing the %s.", "are yet more object messages"),
    SM51(51, ".", "is the termination for a compound sentence on PUTIN / TAKEOUT (and AUTOP / AUTOT)"),
    SM52(52, "There isn't one of those in the ", "is a final object message"),
    SM53(53, "nothing.", "is the message for LISTAT action if no objects found."),

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

    private static final String DEFAULT_RANDOM_MESSAGE = "is selected randomly unless flag 42 is set to be a valid nmessage number.";

    SystemMessageKey(int anId, String aDefaultText, String aDescription) {
        this(Integer.toString(anId), aDefaultText, "PAW", aDescription);
    }

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
