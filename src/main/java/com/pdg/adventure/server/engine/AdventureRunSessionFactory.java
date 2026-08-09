package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Service;

import com.pdg.adventure.CommandFactory;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.LoadAdventureAction;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.mapper.WorkflowMapper;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * Bootstraps a browser-playable game session for a single, already-saved adventure — the
 * plain-web-app equivalent of what MiniAdventure/AdventureClient assemble for the console.
 * Serves both the author's "Test" flow and a player's "Run Adventure" flow; access control
 * (who may load which adventure) is handled by the caller via AdventureAccessService, not here.
 * Reuses the same process-wide GameContext/AdventureConfig singletons the console engine uses
 * (no per-session engine isolation), so at most one run session is meaningfully active at a
 * time — the same constraint the console already has.
 * <p>
 * The returned session has not shown the opening room yet — call {@code session.submit("look")}
 * to render it, exactly like any other turn.
 */
@Service
public class AdventureRunSessionFactory {

    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final WorkflowMapper workflowMapper;
    private final AdventureConfig adventureConfig;
    private final GameContext gameContext;

    public AdventureRunSessionFactory(AdventureService anAdventureService, AdventureMapper anAdventureMapper,
                                      WorkflowMapper aWorkflowMapper, AdventureConfig anAdventureConfig,
                                      GameContext aGameContext) {
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        workflowMapper = aWorkflowMapper;
        adventureConfig = anAdventureConfig;
        gameContext = aGameContext;
    }

    public AdventureRunSession start(AdventureData anAdventureData) {
        loadIntoSharedEngine(anAdventureData);

        Vocabulary vocabulary = adventureConfig.allWords();
        registerBaseVerbs(vocabulary);

        Workflow workflow = gameContext.setUpWorkflows();
        CommandFactory commandFactory = new CommandFactory(adventureConfig.allMessages(), gameContext,
                                                            anAdventureData.getVocabularyData());
        commandFactory.setUpWorkflowCommands(workflow);
        workflowMapper.populate(gameContext.getWorkflowData(), workflow);

        GameLoop gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
        return new AdventureRunSession(gameLoop, gameContext);
    }

    // LoadAdventureAction signals success by throwing ReloadAdventureException and failure (bad
    // id, adventure not found, no locations) by returning normally — inverted from what you'd
    // expect. AdventureClient.run() relies on the same inversion.
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

    // Mirrors MiniAdventure.createSpecialWords(Vocabulary), minus addAdventureIdsToNouns() and
    // the cross-adventure "load X" workflow wiring — a run session is scoped to one adventure.
    private void registerBaseVerbs(Vocabulary aVocabulary) {
        aVocabulary.createNewWord("quit", Word.Type.VERB);
        aVocabulary.createSynonym("exit", "quit");
        aVocabulary.createSynonym("bye", "quit");
        aVocabulary.createNewWord("describe", Word.Type.VERB);
        aVocabulary.createSynonym("look", "describe");
        aVocabulary.createSynonym("l", "describe");
        aVocabulary.createSynonym("desc", "describe");
        aVocabulary.createSynonym("examine", "describe");
        aVocabulary.createSynonym("x", "describe");
        aVocabulary.createNewWord("help", Word.Type.VERB);
        aVocabulary.createNewWord("inventory", Word.Type.VERB);
        aVocabulary.createSynonym("i", "inventory");
        aVocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        aVocabulary.createSynonym("then", "and");
        aVocabulary.createNewWord("it", Word.Type.PRONOUN);
    }
}
