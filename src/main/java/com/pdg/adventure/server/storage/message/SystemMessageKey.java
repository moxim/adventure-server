package com.pdg.adventure.server.storage.message;

import java.util.Arrays;
import java.util.Map;
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

    SM0(0, "It's too dark to see.", "is used instead of the location description when it is dark. (It's too dark to see.)"),
    SM1(1, "I can also see:", "is printed by LISTOBJ if at least one object is present. (I can also see:)"),
    SM2(2, "What now?", "is selected randomly unless flag 42 is set to be a valid message number. (What now?)"),
    SM3(3, "What next?", "is selected randomly unless flag 42 is set to be a valid \n" +
                         "message number. (What next?)"),
    SM4(4, "What should I do now?", "is selected randomly unless flag 42 is set to be a valid message number. (What should I do now?)"),
    SM5(5, "What should I do next?", "is selected randomly unless flag 42 is set to be a valid \n" +
                                     "message number. (What should I do next?)"),
    SM6(6, "I was not able to understand any of that. Please try again.",
        "is produced by the parser when no further phrase can be understood. (I was not able to understand any of that. Please try again.)"),
    SM7(7, "I can't go in that direction.",
        "is produced if no action was carried out (or NOTDONE was) in Response when the Verb is < 14 (I can't go in that direction.)"),
    SM8(8, "I can't do that.",
        "is produced if no action was carried out (or NOTDONE was) in Response when the Verb is > 13 (I can't do that.)"),
    SM9(9, "I have with me:", "is printed when there is at least one item carried. (I have with me:)"),
    SM10(10, " (worn)", "is printed alongside items when they are worn. ( (worn))"),
    SM11(11, "nothing at all", "is printed when there are no items. (nothing at all)"),
    SM12(12, "Are you sure?", "is printed by the QUIT action. (Are you sure?)"),
    SM13(13, "Would you like another go?", "is printed by the END action. (Would you like another go?)"),
    SM14(14, "Goodbye...", "is printed by the END action. (Goodbye...)"),
    SM15(15, "OK.", "is printed by the OK action. (OK.)"),
    SM16(16, "Press any key to continue.", "is printed by the ANYKEY action. (Press any key to continue.)"),
    SM17(17, "You have taken ", "is printed by the TURNS action. (You have taken )"),
    SM18(18, "turn", "is printed by the TURNS action. (turn)"),
    SM19(19, "s", "is printed by the TURNS action. (s)"),
    SM20(20, ".", "is printed by the TURNS action. (.)"),
    SM21(21, "You have scored ", "is the SCORE action messages. (You have scored )"),
    SM22(22, "%", "is part of the SCORE action messages. (%)"),
    SM23(23, "I am not wearing any of those.", "is printed when the player tries to remove an item that is not worn. (I am not wearing any of those.)"),
    SM24(24, "I can't. I am wearing the %s.", "is printed when the player tries to drop an item that is worn. (I can't. I am wearing the %s.)"),
    SM25(25, "I already have the %s.", "is printed when the player tries to take an item that they already have. (I already have the %s.)"),
    SM26(26, "There isn't one of those here.", "is printed when the player tries to take an item that isn't present. (There isn't one of those here.)"),
    SM27(27, "I can't carry any more things.",
         "is printed when the player tries to take an item but their inventory is full. (I can't carry any more things.)"),
    SM28(28, "I don't have one of those.", "is printed when the player tries to drop an item that they don't have. (I don't have one of those.)"),
    SM29(29, "I'm already wearing the %s.",
         "is printed when the player tries to perform an action that is not allowed. (I'm already wearing the %s.)"),
    SM30(30, "Y", "is the positive response expected by END and QUIT. (Y)"),
    SM31(31, "N", "is the negative response expected by END and QUIT. (N)"),
    SM32(32, "More....", "is produced when a screen full of text has appeared. (More....)"),
    SM33(33, "> ", "is the input marker. (> )"),
    SM34(34, "|", "is the cursor. (|)"),
    SM35(35, "Time passes...", "is displayed when a timeout occurs (Time passes...)"),
    SM36(36, "I now have the %s.", "is printed when the player picks up an item. (I now have the %s.)"),
    SM37(37, "I am now wearing the %s.", "is printed when the player wears an item. (I am now wearing the %s.)"),
    SM38(38, "I've removed the %s", "is printed when the player removes an item. (I've removed the %s)"),
    SM39(39, "I've dropped the %s.", "is printed when the player drops an item. (I've dropped the %s.)"),
    SM40(40, "I can't wear the %s.", "is printed when the player tries to wear an item they can't wear. (I can't wear the %s.)"),
    SM41(41, "I can't remove the %s.", "is printed when the player tries to remove an item they can't remove. (I can't remove the %s.)"),
    SM42(42, "I can't remove the %s. My hands are full.",
         "is printed when the player tries to remove an item but their hands are full. (I can't remove the %s. My hands are full.)"),
    SM43(43, "The %s weighs too much for me.",
         "is printed when the player tries to pick up an item that is too heavy. (The %s weighs too much for me.)"),
    SM44(44, "The %s is in the %s.", "is printed when the player tries to take an item that is in a container. (The %s is in the %s.)"),
    SM45(45, "The %s isn't in the %s.", "is printed when the player tries to take an item that isn't in a container. (The %s isn't in the %s.)"),
    SM46(46, ",", "is the link between objects when listing continuously (,)"),
    SM47(47, " and ", "is the final link between the last two objects when listing ( and )"),
    SM48(48, "'", "is the termination of a list of objects (printed by both LISTOBJ and LISTAT, so take care.) (')"),
    SM49(49, "I don't have the %s.", "is printed when the player wants to drop an item that is not carried. (I don't have the %s.)"),
    SM50(50, "I'm not wearing the %s.", "is printed when the player wants to remove an item that is not worn. (I'm not wearing the %s.)"),
    SM51(51, ".", "is the termination for a compound sentence on PUTIN / TAKEOUT (and AUTOP / AutoTake) (.)"),
    SM52(52, "There isn't one of those in the %s.",
         "is printed when the player tries to take an item that isn't in a container. (There isn't one of those in the %s.)"),
    SM53(53, "nothing.", "is the message for LISTAT action if no objects found. (nothing.)"),
    SM54(54, "The %s is full.", "is printed when the player tries to put an item into a container that is full. (The %s is full.)"),
    SM55(55, "I can't take the %1s out of the %2s.",
         "is printed when the player tries to take an item out of a container that is not allowed. (I can't take the %1s out of the %2s.)"),
    SM56(56, "I put the %1$s into the %2$s.", "is printed when the player puts an item into a container. (I put the %1$s into the %2$s.)"),
    SM57(57, "The %s evaporates into thin air.", "is printed when an item disappears. (The %s evaporates into thin air.)"),
    SM58(58, "A %1$s appears in the %2$s.", "is printed when an item appears in a container. (A %1$s appears in the %2$s.)"),
    SM59(59, "Exits are:", "is printed after a location description. (Exits are:)"),
    SM60(60, "What should I %s?", "is printed when a verb matches multiple commands and no noun was given. (What should I %s?)"),
    SM61(61, "Which %1$s should I %2$s?", "is printed when a verb+noun still matches multiple commands. (Which %1$s should I %2$s?)"),
    SM62(62, "There are no obvious exits.", "is printed when a location has no exits to list. (There are no obvious exits.)"),
    SM63(63, "I don't know what 'it' refers to.", "is printed when a pronoun (it, them, ...) has no prior noun to refer to. (I don't know what 'it' refers to.)"),
    SM64(64, "The %1$s is already in the %2$s.",
         "is printed when the player tries to put an item into a container that already contains it. (The %1$s is already in the %2$s.)"),
    SM65(65, "I can't put the %1s into the %2s.",
         "is printed when the player tries to put an item into a container that is not allowed. (I can't put the %1s into the %2s.)"),
    SM66(66, "The %1$s's light level is now %2$s.",
         "is printed when LightAction changes an item's lumen value. (The %1$s's light level is now %2$s.)"),
    SM67(67, " (lit)", "is printed when an item is lit. ( (lit))"),

    // Currently never registered for a real adventure - CommandFactory looks this id up
    // unconditionally, which NPEs outside the console demo. A live bug, outside this feature's
    // scope (that's the deferred engine-rewiring phase). Catalogued anyway so it's ready once fixed.

    HELP_TEXT("Look around, examine items, take or drop items, maybe wear items, enter or leave locations.\nOr quit.",
              "CommandFactory",
              "The full text shown for the built-in 'help' command. (Look around, examine items, take or drop "
              + "items, maybe wear items, enter or leave locations.\nOr quit.)");

    /**
     * The overrides (id -> text) of the adventure whose turn is running on this thread, sparse - a key with no
     * entry reads as its built-in text. Overrides belong to an adventure definition (the same for every player
     * and saved game of it), but different adventures can run at the same time, so they are bound per thread for
     * the duration of a run's turn (see {@link #bindOverrides}) instead of process-wide.
     */
    private static final ThreadLocal<Map<String, String>> BOUND_OVERRIDES = new ThreadLocal<>();

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
        Map<String, String> bound = BOUND_OVERRIDES.get();
        return bound == null ? defaultText : bound.getOrDefault(id, defaultText);
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

    /**
     * Binds the given overrides to the current thread until the returned binding is closed. Closing restores
     * whatever was bound before, so nested binds are safe. Always use try-with-resources: servlet threads are
     * reused, and an unclosed binding would leak this adventure's wording into the next request on the thread.
     */
    public static Binding bindOverrides(Map<String, String> anOverridesByKeyId) {
        Map<String, String> previous = BOUND_OVERRIDES.get();
        BOUND_OVERRIDES.set(Map.copyOf(anOverridesByKeyId));
        return () -> {
            if (previous == null) {
                BOUND_OVERRIDES.remove();
            } else {
                BOUND_OVERRIDES.set(previous);
            }
        };
    }

    /** Undoes a {@link #bindOverrides} call; unlike {@link AutoCloseable#close()} it throws nothing. */
    @FunctionalInterface
    public interface Binding extends AutoCloseable {
        @Override
        void close();
    }
}
