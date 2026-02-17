package com.pdg.adventure;

import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.*;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.GameLoop;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.vocabulary.Vocabulary;

// TODO
//  get rid of ugly casts
//  find them with \(\b[A-Z][A-Za-z0-9]*?\b\)

public class MiniAdventure {
    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final GameContext gameContext;
    private AdventureConfig adventureConfig;
    // these hold everything
    private Vocabulary allWords;
    private MessagesHolder allMessages;
    private Map<String, Location> allLocations;
    private CommandFactory commandFactory;
    private MiniAdventureContent content;

    public MiniAdventure(AdventureConfig anAdventureConfig, final AdventureMapper anAdventureMapper,
                         AdventureService anAdventureService, GameContext aGameContext) {
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        gameContext = aGameContext;
        useAdventureConfiguration(anAdventureConfig);
    }

    private void useAdventureConfiguration(final AdventureConfig anAdventureConfig) {
        adventureConfig = anAdventureConfig;
        allWords = adventureConfig.allWords();
        allMessages = adventureConfig.allMessages();
        allLocations = adventureConfig.allLocations();
        commandFactory = new CommandFactory(allMessages, gameContext);
        content = new MiniAdventureContent(adventureConfig, gameContext, commandFactory);
    }

    static void main(String[] args) {
        final GameContext gameContext = new GameContext();
        final MiniAdventure game = new MiniAdventure(new AdventureConfig(gameContext), null, null, gameContext);
        game.setup();
        game.run();
    }

    private void setup() {
        content.setUp();

        gameContext.setPocket(content.getPocket());
        gameContext.setCurrentLocation(content.getLocation());

        Workflow wf = gameContext.setUpWorkflows();
        commandFactory.setUpWorkflowCommands(wf);

        // start on location "1"
        ExecutionResult result = new MovePlayerAction(content.getLocation(), allMessages, gameContext).execute();
        gameContext.tell(result.getResultMessage());
    }

    void run() {

        boolean keepRunning = true;
        while (keepRunning) {
            try {
                content.setUpMessages();
                System.out.println(new MessageAction(allMessages.getMessage("4"), allMessages).execute());

                Workflow wf = gameContext.setUpWorkflows();
                createSpecialWords(allWords);
                commandFactory.setUpWorkflowCommands(wf);

                final GameLoop gameLoop = initializeGameLoop();

                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                gameLoop.run(reader);
                keepRunning = false; // Normal exit

            } catch (ReloadAdventureException e) {
                // we've just reloaded the adventure, so restart the game loop
                gameContext.tell(e.getMessage());
            }
        }
    }

    private @NonNull GameLoop initializeGameLoop() {
        final var look = new DescribeAction(() -> {
            long timesVisited = 0;
            gameContext.getCurrentLocation().setTimesVisited(timesVisited);
            String result = gameContext.getCurrentLocation().getLongDescription();
            gameContext.getCurrentLocation().setTimesVisited(++timesVisited);
            return result;
        }, allMessages).execute();
        gameContext.tell(look.getResultMessage());

        GameLoop gameLoop = new GameLoop(new Parser(allWords), gameContext);
        return gameLoop;
    }

    private void createSpecialWords(final Vocabulary aVocabulary) {
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
        aVocabulary.createNewWord("default", Word.Type.NOUN);

        addAdventureIdsToNouns();
    }

    private void addAdventureIdsToNouns() {
        final List<AdventureData> anAdventureDataList = adventureService.getAdventures();
        for (AdventureData adventureData : anAdventureDataList) {
            String adventureId = adventureData.getId();
            allWords.createNewWord(adventureId, Word.Type.NOUN);
            addLoadAdventureToWorkflowCommands(adventureId);
        }
        addLoadAdventureToWorkflowCommands("");
    }

    private void addLoadAdventureToWorkflowCommands(final String aAdventureId) {
        var loadAdventureCommandDescription = new GenericCommandDescription("load", aAdventureId);
        final var loadAdventureAction = new LoadAdventureAction(adventureService, adventureMapper, adventureConfig,
                                                                allMessages, gameContext);
        loadAdventureAction.setAdventureId(aAdventureId);
        var loadAdventureCommand = new GenericCommand(loadAdventureCommandDescription, loadAdventureAction);
        gameContext.getWorkflow().addInterceptorCommand(loadAdventureCommandDescription, loadAdventureCommand);
    }

    public void setLocations(List<Location> locations) {
        for (Location location : locations) {
            allLocations.put(location.getId(), location);
        }
    }
}
