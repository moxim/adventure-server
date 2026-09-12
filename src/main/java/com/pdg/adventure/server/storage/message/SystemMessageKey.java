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
    SM2(2, "What now?", "is selected randomly unless flag 42 is set to be a valid message number."),
    SM3(3, "What next?", "is selected randomly unless flag 42 is set to be a valid \n" +
                         "message number."),
    SM4(4, "What should I do now?", "is selected randomly unless flag 42 is set to be a valid message number."),
    SM5(5, "What should I do next?", "is selected randomly unless flag 42 is set to be a valid \n" +
                                     "message number."),
    SM6(6, "I was not able to understand any of that. Please try again.",
        "is produced by the parser when no further phrase can be understood."),
    SM7(7, "I can't go in that direction.",
        "is produced if no action was carried out (or NOTDONE was) in Response when the Verb is < 14"),
    SM8(8, "I can't do that.",
        "is produced if no action was carried out (or NOTDONE was) in Response when the Verb is > 13"),
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
    SM27(27, "I can't carry any more things.",
         "is printed when the player tries to take an item but their inventory is full."),
    SM28(28, "I don't have one of those.", "is printed when the player tries to drop an item that they don't have."),
    SM29(29, "I'm already wearing the %s.",
         "is printed when the player tries to perform an action that is not allowed."),
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
    SM42(42, "I can't remove the %s. My hands are full.",
         "is printed when the player tries to remove an item but their hands are full."),
    SM43(43, "The %s weighs too much for me.",
         "is printed when the player tries to pick up an item that is too heavy."),
    SM44(44, "The %s is in the %s.", "is printed when the player tries to take an item that is in a container."),
    SM45(45, "The %s isn't in the %s.", "is printed when the player tries to take an item that isn't in a container."),
    SM46(46, ",", "is the link between objects when listing continuously"),
    SM47(47, " and ", "is the final link between the last two objects when listing"),
    SM48(48, "'", "is the termination of a list of objects (printed by both LISTOBJ and LISTAT, so take care.)"),
    SM49(49, "I don't have the %s.", "is printed when the player wants to drop an item that is not carried."),
    SM50(50, "I'm not wearing the %s.", "is printed when the player wants to remove an item that is not worn."),
    SM51(51, ".", "is the termination for a compound sentence on PUTIN / TAKEOUT (and AUTOP / AUTOT)"),
    SM52(52, "There isn't one of those in the %s.",
         "is printed when the player tries to take an item that isn't in a container."),
    SM53(53, "nothing.", "is the message for LISTAT action if no objects found."),
    SM54(54, "The %s is full.", "is printed when the player tries to put an item into a container that is full."),
    SM55(55, "I can't take the %1s out of the %2s.",
         "is printed when the player tries to take an item out of a container that is not allowed."),
    SM56(56, "I put the %1$s into the %2$s.", "is printed when the player puts an item into a container."),
    SM57(57, "The %s evaporates into thin air.", "is printed when an item disappears."),
    SM58(58, "A %1$s appears in the %2$s.", "is printed when an item appears in a container."),
    SM59(59, "Exits are:", "is printed after a location description."),
    SM60(60, "What should I %s?", "is printed when a verb matches multiple commands and no noun was given."),
    SM61(61, "Which %1$s should I %2$s?", "is printed when a verb+noun still matches multiple commands."),
    SM62(62, "There are no obvious exits.", "is printed when a location has no exits to list."),
    SM63(63, "I don't know what 'it' refers to.", "is printed when a pronoun (it, them, ...) has no prior noun to refer to."),
    SM64(64, "The %1$s is already in the %2$s.",
         "is printed when the player tries to put an item into a container that already contains it."),
    SM65(65, "I can't put the %1s into the %2s.",
         "is printed when the player tries to put an item into a container that is not allowed."),
    SM66(66, "The %1$s's light level is now %2$s.",
         "is printed when LightAction changes an item's lumen value."),
    SM67(67, " (lit)", "is printed when an item is lit."),

    // Currently never registered for a real adventure - CommandFactory looks this id up
    // unconditionally, which NPEs outside the console demo. A live bug, outside this feature's
    // scope (that's the deferred engine-rewiring phase). Catalogued anyway so it's ready once fixed.

    HELP_TEXT("Look around, examine items, take or drop items, maybe wear items, enter or leave locations.\nOr quit.",
              "CommandFactory",
              "The full text shown for the built-in 'help' command.");

    private final String id;
    private final String defaultText;
    private final String sourceLocation;
    private final String description;

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
