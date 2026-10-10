package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.SavedGameData;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.GameStateSnapshotter;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.storage.service.SavedGameService;

/**
 * Without a slot number lists the current player's saved games for this adventure; with one (LOAD 3) restores that
 * game in place and describes the restored location. Meant for a Response on the wildcard noun.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LoadGameAction extends AbstractAction {

    private final transient GameContext gameContext;
    private final transient GameStateSnapshotter snapshotter;
    private final transient SavedGameService savedGameService;

    public LoadGameAction(GameContext aGameContext, GameStateSnapshotter aSnapshotter,
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
        if (noun.isEmpty()) {
            return list(who);
        }
        OptionalInt requested = SavedGameService.parseSlot(noun);
        if (requested.isEmpty()) {
            return failure(SystemMessageKey.SM70.defaultText().formatted(SavedGameData.SAVED_GAME_SLOTS));
        }
        return restore(who, requested.getAsInt());
    }

    private ExecutionResult list(GameContext.RunIdentity aWho) {
        List<SavedGameData> saved = savedGameService.list(aWho.playerId(), aWho.adventureId());
        if (saved.isEmpty()) {
            return new CommandExecutionResult(ExecutionResult.State.SUCCESS,
                                              SystemMessageKey.SM73.defaultText());
        }
        List<String> lines = new ArrayList<>();
        lines.add(SystemMessageKey.SM72.defaultText());
        for (SavedGameData game : saved) {
            lines.add("%s. %s".formatted(game.getSlot(),
                                         SavedGameService.label(aWho.adventureTitle(), game.getSavedAt())));
        }
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS, String.join("\n", lines));
    }

    private ExecutionResult restore(GameContext.RunIdentity aWho, int aSlot) {
        Optional<SavedGameData> found = savedGameService.find(aWho.playerId(), aWho.adventureId(), aSlot);
        if (found.isEmpty()) {
            return failure(SystemMessageKey.SM75.defaultText().formatted(aSlot));
        }
        SavedGameData saved = found.get();
        if (!snapshotter.restore(saved.getSnapshot())) {
            return failure(SystemMessageKey.SM76.defaultText());
        }
        List<String> lines = new ArrayList<>();
        lines.add(SystemMessageKey.SM74.defaultText().formatted(aSlot));
        if (saved.getBuilderVersion() != null && aWho.builderVersion() != null
            && !Objects.equals(saved.getBuilderVersion(), aWho.builderVersion())) {
            lines.add(SystemMessageKey.SM77.defaultText()
                                           .formatted(saved.getBuilderVersion(), aWho.builderVersion()));
        }
        lines.add(gameContext.getCurrentLocation().getLookDescription().text());
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS, String.join("\n", lines));
    }

    private static ExecutionResult failure(String aMessage) {
        return new CommandExecutionResult(ExecutionResult.State.FAILURE, aMessage);
    }
}
