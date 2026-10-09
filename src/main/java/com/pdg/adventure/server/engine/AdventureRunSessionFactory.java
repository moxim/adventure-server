package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SavedGameData;
import com.pdg.adventure.model.SystemMessageData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.LoadAdventureAction;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.mapper.WorkflowMapper;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * Bootstraps a browser-playable game session for a single, already-saved adventure.
 * Serves both the author's "Test" flow and a player's "Run Adventure" flow; access control
 * (who may load which adventure) is handled by the caller via AdventureAccessService, not here.
 * <p>
 * The GameContext and the AdventureConfig registries injected here are scoped-proxy beans bound to the
 * calling Vaadin session, so every browser session has its own engine state and several players can play
 * at once. Within one browser session only one run is active at a time ({@link ActiveRun}); a second
 * {@link #start} is refused with {@link RunAlreadyActiveException} before anything is touched, and
 * {@link #startReplacingActive} is the explicit takeover.
 * <p>
 * The returned session has not shown the opening room yet - the caller renders it, running the rendering
 * through {@link AdventureRunSession#runBound} so the adventure's system-message overrides apply.
 */
@Service
public class AdventureRunSessionFactory {

    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final WorkflowMapper workflowMapper;
    private final AdventureConfig adventureConfig;
    private final GameContext gameContext;
    private final ActiveRun activeRun;

    public AdventureRunSessionFactory(AdventureService anAdventureService, AdventureMapper anAdventureMapper,
                                      WorkflowMapper aWorkflowMapper, AdventureConfig anAdventureConfig,
                                      GameContext aGameContext, ActiveRun anActiveRun) {
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        workflowMapper = aWorkflowMapper;
        adventureConfig = anAdventureConfig;
        gameContext = aGameContext;
        activeRun = anActiveRun;
    }

    /**
     * Starts a run for the given owner.
     *
     * @throws RunAlreadyActiveException if this browser session already has an active run; nothing has been
     *                                   changed in that case
     */
    public AdventureRunSession start(AdventureData anAdventureData, RunOwner anOwner) {
        if (activeRun.isActive()) {
            throw new RunAlreadyActiveException();
        }
        return startRun(anAdventureData, anOwner);
    }

    /** The explicit takeover: ends the active run (its next input reports "ended in another tab") and starts anew. */
    public AdventureRunSession startReplacingActive(AdventureData anAdventureData, RunOwner anOwner) {
        activeRun.supersedeActive();
        return startRun(anAdventureData, anOwner);
    }

    /**
     * Releases the run if {@code anOwner} still owns it and clears this browser session's registries so a
     * finished game doesn't pin its adventure in memory. A no-op for an owner that no longer owns the run, so a
     * late release from an old view can't wipe a newer game.
     */
    public void release(RunOwner anOwner) {
        if (activeRun.release(anOwner)) {
            clearRegistries();
        }
    }

    private AdventureRunSession startRun(AdventureData anAdventureData, RunOwner anOwner) {
        Map<String, String> overrides = systemMessageOverrides(anAdventureData);
        // Loading, mapping and workflow set-up run with the adventure's overrides bound, exactly as when
        // LoadAdventureAction used to install them process-wide before mapping: anything constructed on the way
        // that reads SystemMessageKey text keeps seeing the adventure's own wording.
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(overrides)) {
            loadIntoSharedEngine(anAdventureData);
            gameContext.setRunIdentity(new GameContext.RunIdentity(anOwner.getPlayerId(), anAdventureData.getId(),
                                                                   anAdventureData.getTitle(),
                                                                   anAdventureData.getBuilderVersion()));

            Vocabulary vocabulary = adventureConfig.allWords();
            registerBaseWords(vocabulary);

            Workflow workflow = gameContext.setUpWorkflows();
            workflowMapper.populate(gameContext.getWorkflowData(), workflow);

            GameLoop gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
            AdventureRunSession session = new AdventureRunSession(gameLoop, gameContext, overrides);
            activeRun.register(session, anOwner);
            return session;
        }
    }

    private void clearRegistries() {
        adventureConfig.allLocations().clear();
        adventureConfig.allItems().clear();
        adventureConfig.allContainers().clear();
        adventureConfig.allMessages().clear();
        adventureConfig.allVariables().clear();
        adventureConfig.allWords().setWords(List.of());
        // Also drop what the finished game left on the shared-per-session context, so it neither pins the old
        // adventure nor shows the old picture at the start of the next run in this browser session.
        gameContext.setCurrentLocation(null);
        gameContext.setPocket(null);
        gameContext.setCurrentPictureId(null);
        gameContext.setRunIdentity(null);
    }

    private static Map<String, String> systemMessageOverrides(AdventureData anAdventureData) {
        Map<String, String> overrides = new HashMap<>();
        for (SystemMessageData override : anAdventureData.getSystemMessages().values()) {
            overrides.put(override.getKey(), override.getText());
        }
        return overrides;
    }

    // LoadAdventureAction signals success by throwing ReloadAdventureException and failure (bad
    // id, adventure not found, no locations) by returning normally — inverted from what you'd
    // expect.
    private void loadIntoSharedEngine(AdventureData anAdventureData) {
        LoadAdventureAction loadAdventureAction = new LoadAdventureAction(adventureService, adventureMapper,
                                                                          adventureConfig, gameContext);
        try {
            loadAdventureAction.loadAdventure(anAdventureData.getId());
        } catch (ReloadAdventureException expectedOnSuccess) {
            return;
        }
        throw new IllegalStateException(
                "Adventure '%s' could not be loaded to run — check it has at least one location."
                        .formatted(anAdventureData.getId()));
    }

    // A run session is scoped to one adventure.
    private void registerBaseWords(Vocabulary aVocabulary) {
        // The parser drops unknown words, so the slot numbers of SAVE/LOAD must be words.
        for (int slot = 1; slot <= SavedGameData.SAVED_GAME_SLOTS; slot++) {
            String digit = Integer.toString(slot);
            if (aVocabulary.findWord(digit).isEmpty()) {
                aVocabulary.createNewWord(digit, Word.Type.NOUN);
            }
        }
    }
}
