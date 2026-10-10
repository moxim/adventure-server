package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.OptionalInt;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.SavedGameData;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.GameStateSnapshotter;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.storage.service.SavedGameService;

/**
 * Saves the running game into a slot of the current player: the slot the player typed (SAVE 3), or the lowest free
 * one (SAVE). Meant for a Response on the wildcard noun, like the Auto-item actions.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SaveGameAction extends AbstractAction {

    private final transient GameContext gameContext;
    private final transient GameStateSnapshotter snapshotter;
    private final transient SavedGameService savedGameService;

    public SaveGameAction(GameContext aGameContext, GameStateSnapshotter aSnapshotter,
                          SavedGameService aSavedGameService) {
        gameContext = aGameContext;
        snapshotter = aSnapshotter;
        savedGameService = aSavedGameService;
    }

    @Override
    public ExecutionResult execute() {
        GameContext.RunIdentity who = gameContext.getRunIdentity();
        if (who == null) {
            return failure(SystemMessageKey.SM71.defaultText());
        }
        String noun = gameContext.getCurrentNoun();
        int slot;
        if (noun.isEmpty()) {
            OptionalInt free = savedGameService.freeSlot(who.playerId(), who.adventureId());
            if (free.isEmpty()) {
                return failure(SystemMessageKey.SM69.defaultText()
                                                    .formatted(SavedGameData.SAVED_GAME_SLOTS,
                                                                    SavedGameData.SAVED_GAME_SLOTS));
            }
            slot = free.getAsInt();
        } else {
            OptionalInt requested = SavedGameService.parseSlot(noun);
            if (requested.isEmpty()) {
                return failure(SystemMessageKey.SM70.defaultText().formatted(SavedGameData.SAVED_GAME_SLOTS));
            }
            slot = requested.getAsInt();
        }
        savedGameService.save(who.playerId(), who.adventureId(), slot, who.builderVersion(), snapshotter.capture(),
                              Instant.now());
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS,
                                          SystemMessageKey.SM68.defaultText().formatted(slot));
    }

    private static ExecutionResult failure(String aMessage) {
        return new CommandExecutionResult(ExecutionResult.State.FAILURE, aMessage);
    }
}
