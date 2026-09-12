package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.CommandFactory;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.condition.PlayerAtCondition;
import com.pdg.adventure.server.condition.PrepositionCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * Exercises processCommand(String) directly, i.e. the browser Test session's entry point into
 * the engine (no BufferedReader involved). Wires the same interceptor commands
 * AdventureRunSessionFactory registers in production, via the real CommandFactory.
 */
class GameLoopTest {

    private final StringBuilder told = new StringBuilder();
    private GameContext gameContext;
    private Vocabulary vocabulary;
    private Workflow workflow;
    private GameLoop gameLoop;
    private final VariableProvider variableProvider = new VariableProvider();

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        gameContext.setOutputSink(line -> told.append(line).append('\n'));

        gameContext.setPocket(new GenericContainer(new DescriptionProvider("pocket"), 10));

        DescriptionProvider roomDescription = new DescriptionProvider("throne", "room");
        roomDescription.setLongDescription("A grand throne room.");
        Location room = new Location(roomDescription, new GenericContainer(new DescriptionProvider("room items"), 10));
        gameContext.setCurrentLocation(room);

        vocabulary = new Vocabulary();
        vocabulary.createNewWord("quit", Word.Type.VERB);
        vocabulary.createNewWord("describe", Word.Type.VERB);
        vocabulary.createNewWord("take", Word.Type.VERB); // recognised, but wired to nothing
        vocabulary.createNewWord("suit", Word.Type.NOUN); // recognised noun, no verb given in some tests
        vocabulary.createNewWord("help", Word.Type.VERB);
        vocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        vocabulary.createSynonym("then", "and");
        vocabulary.createNewWord("it", Word.Type.PRONOUN);

        workflow = gameContext.setUpWorkflows();
        new CommandFactory(new MessagesHolder(), gameContext, new VocabularyData()).setUpWorkflowCommands(workflow);

        gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
    }

    @Test
    void describe_tellsTheCurrentLocationsLongDescription() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("A grand throne room.");
    }

    @Test
    void describe_onARevisitedLocation_stillTellsTheLongDescription() {
        // The player has been here before (timesVisited >= 1). Walking in again shows the
        // short description, but an explicit "describe" must always show the long one.
        DescriptionProvider cellarDescription = new DescriptionProvider("dark", "cellar");
        cellarDescription.setShortDescription("The dark cellar.");
        cellarDescription.setLongDescription("A dank cellar reeking of old wine and mould.");
        Location cellar = new Location(cellarDescription,
                                      new GenericContainer(new DescriptionProvider("cellar items"), 10));
        cellar.setTimesVisited(1);
        gameContext.setCurrentLocation(cellar);

        // Mirror production wiring: LoadAdventureAction registers an examine fallback on every location.
        VocabularyData vocabularyData = new VocabularyData();
        Word describeWord = vocabularyData.createWord("describe", Word.Type.VERB);
        vocabularyData.setExamineWord(describeWord);
        new CommandFactory(new MessagesHolder(), gameContext, vocabularyData)
                .applyExamineFallback(List.of(cellar));

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString())
                .contains("A dank cellar reeking of old wine and mould.")
                .doesNotContain("The dark cellar.");
    }

    @Test
    void quit_tellsGoodbye_andReturnsQuit() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("quit");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.QUIT);
        assertThat(told.toString()).contains(SystemMessageKey.SM14.defaultText());
    }

    @Test
    void unrecognisedInput_tellsPleaseRephrase_andContinues() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("mumble grumble");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM6.defaultText());
    }

    @Test
    void recognisedVerbWithNoMatchingCommand_tellsItDoesNotKnowHow() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("take");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM8.defaultText());
    }

    @Test
    void bareNounWithNoVerb_tellsPleaseRephrase_notIDontKnowHow() {
        // "suit" is a recognised noun but no verb was given, so this must be treated the
        // same as unparseable input, not as a command that merely fails to find a match.
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("suit");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM6.defaultText());
        assertThat(told.toString()).doesNotContain(SystemMessageKey.SM8.defaultText());
    }

    @Test
    void and_runsBothSubCommandsInOrder() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe and help");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        String output = told.toString();
        assertThat(output).contains("A grand throne room.").contains("Look around, examine items");
        assertThat(output.indexOf("A grand throne room."))
                .isLessThan(output.indexOf("Look around, examine items"));
    }

    @Test
    void period_behavesTheSameAsAnd() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe. help");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("A grand throne room.").contains("Look around, examine items");
    }

    @Test
    void and_stopsAtFirstFailure_secondSubCommandNeverRuns() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("take and describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM8.defaultText());
        assertThat(told.toString()).doesNotContain("A grand throne room.");
    }

    @Test
    void and_bareNounFirstSubCommand_stopsSequence() {
        // "suit" alone is a recognised noun with no verb - unparseable on its own (see
        // bareNounWithNoVerb_tellsPleaseRephrase_notIDontKnowHow above) - and must stop the
        // sequence the same way a real command failure would, not let "describe" run anyway.
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("suit and describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM6.defaultText());
        assertThat(told.toString()).doesNotContain("A grand throne room.");
    }

    @Test
    void and_quitMidSequence_returnsQuit_laterSubCommandsNeverRun() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe and quit and help");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.QUIT);
        assertThat(told.toString()).contains("A grand throne room.").contains(SystemMessageKey.SM14.defaultText());
        assertThat(told.toString()).doesNotContain("Look around, examine items");
    }

    @Test
    void and_afterMovingSubCommand_secondSubCommandSeesTheNewLocation() {
        // A command wired only on the destination location ("examine key") must be reachable
        // by the second sub-command of a sequence whose first sub-command moved the player
        // there - proves GameLoop re-reads the current location per sub-command rather than
        // capturing it once for the whole turn.
        MessagesHolder messages = new MessagesHolder();
        DescriptionProvider cellarDescription = new DescriptionProvider("cellar", "cellar");
        cellarDescription.setLongDescription("A dark, damp cellar.");
        Location cellar = new Location(cellarDescription,
                                       new GenericContainer(new DescriptionProvider("cellar items"), 10));
        cellar.addCommand(new GenericCommand(new GenericCommandDescription("examine", "key"),
                                             new MessageAction("A rusty key.")));

        GenericCommandDescription descendDescription = new GenericCommandDescription("descend");
        workflow.addResponse(descendDescription,
                                       new GenericCommand(descendDescription,
                                                          new MovePlayerAction(cellar, gameContext, variableProvider)));

        vocabulary.createNewWord("descend", Word.Type.VERB);
        vocabulary.createNewWord("examine", Word.Type.VERB);
        vocabulary.createNewWord("key", Word.Type.NOUN);

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("descend and examine key");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("A dark, damp cellar.").contains("A rusty key.");
        assertThat(told.toString()).doesNotContain(SystemMessageKey.SM8.defaultText());
    }

    @Test
    void currentPreposition_isSetPerSubCommand_andResetsWhenTheNextSubCommandHasNone() {
        // Same class of bug as the location-staleness trap documented on
        // GameLoop.runOneCommandSucceeded: a preposition left over from an earlier sub-command
        // (or turn) must not leak into a later one that had none.
        vocabulary.createNewWord("switch", Word.Type.VERB);
        vocabulary.createNewWord("lamp", Word.Type.NOUN);
        vocabulary.createNewWord("on", Word.Type.PREPOSITION);

        GenericCommandDescription switchLamp = new GenericCommandDescription("switch", "lamp");
        workflow.addResponse(switchLamp, new GenericCommand(switchLamp, new MessageAction("Click.")));

        gameLoop.processCommand("switch lamp on");
        assertThat(gameContext.getCurrentPreposition()).isEqualTo("on");

        gameLoop.processCommand("describe");
        assertThat(gameContext.getCurrentPreposition()).isEmpty();
    }

    @Test
    void and_prepositionGatedResponses_bothReachable_pickingTheOneMatchingTheTypedPreposition() {
        // "SWITCH LAMP ON" vs "SWITCH LAMP OFF": two Response rows sharing the same verb+noun,
        // each gated by a PrepositionCondition for a different preposition, must both be
        // reachable - proving Workflow's chain-per-description fix actually works end to end.
        vocabulary.createNewWord("switch", Word.Type.VERB);
        vocabulary.createNewWord("lamp", Word.Type.NOUN);
        vocabulary.createNewWord("on", Word.Type.PREPOSITION);
        vocabulary.createNewWord("off", Word.Type.PREPOSITION);

        GenericCommandDescription switchLamp = new GenericCommandDescription("switch", "lamp");
        GenericCommand switchOn = new GenericCommand(switchLamp, new MessageAction("The lamp is now on."));
        switchOn.addPreCondition(new PrepositionCondition("on", gameContext));
        GenericCommand switchOff = new GenericCommand(switchLamp, new MessageAction("The lamp is now off."));
        switchOff.addPreCondition(new PrepositionCondition("off", gameContext));
        workflow.addResponse(switchLamp, switchOn);
        workflow.addResponse(switchLamp, switchOff);

        gameLoop.processCommand("switch lamp on");
        assertThat(told.toString()).contains("The lamp is now on.").doesNotContain("The lamp is now off.");

        told.setLength(0);
        gameLoop.processCommand("switch lamp off");
        assertThat(told.toString()).contains("The lamp is now off.").doesNotContain("The lamp is now on.");
    }

    @Test
    void it_withNoAntecedentInTheSession_tellsASpecificMessage() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("take it");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM63.defaultText());
    }

    @Test
    void and_verbInference_bareNounSecondSubCommandUsesFirstSubCommandsVerb() {
        vocabulary.createNewWord("examine", Word.Type.VERB);
        vocabulary.createNewWord("sword", Word.Type.NOUN);
        vocabulary.createNewWord("shield", Word.Type.NOUN);
        gameContext.getCurrentLocation().addCommand(new GenericCommand(
                new GenericCommandDescription("examine", "sword"),
                new MessageAction("A sharp sword.")));
        gameContext.getCurrentLocation().addCommand(new GenericCommand(
                new GenericCommandDescription("examine", "shield"),
                new MessageAction("A sturdy shield.")));

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("examine sword and shield");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("A sharp sword.").contains("A sturdy shield.");
    }

    @Test
    void it_resolvesAcrossSeparateTurns_evenIfThePrecedingSubCommandFailed() {
        vocabulary.createNewWord("wear", Word.Type.VERB);
        gameContext.getCurrentLocation().addCommand(new GenericCommand(
                new GenericCommandDescription("wear", "suit"),
                new MessageAction(SystemMessageKey.SM37.defaultText().formatted("suit"))));

        GameLoop.CommandOutcome firstOutcome = gameLoop.processCommand("take suit");
        GameLoop.CommandOutcome secondOutcome = gameLoop.processCommand("wear it");

        assertThat(firstOutcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM8.defaultText());
        assertThat(secondOutcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM37.defaultText().formatted("suit"));
    }

    @Test
    void movingAway_doesNotFireAProcessGatedOnTheLocationJustLeft() {
        // Before this fix, GameLoop evaluated Processes BEFORE the sub-command (the move) executed,
        // so a Process gated on the room the player is leaving would still see them "there" and
        // wrongly fire on the very turn they left.
        MessagesHolder messages = new MessagesHolder();
        Location room = gameContext.getCurrentLocation();
        DescriptionProvider cellarDescription = new DescriptionProvider("cellar", "cellar");
        cellarDescription.setLongDescription("A dark, damp cellar.");
        Location cellar = new Location(cellarDescription,
                                       new GenericContainer(new DescriptionProvider("cellar items"), 10));

        GenericCommandDescription roomOnlyDescription = new GenericCommandDescription("throne-room-only");
        GenericCommand roomOnlyProcess = new GenericCommand(roomOnlyDescription,
                new MessageAction("Welcome to the throne room."));
        roomOnlyProcess.addPreCondition(new PlayerAtCondition(room, gameContext));
        workflow.addProcess(roomOnlyDescription, roomOnlyProcess);

        GenericCommandDescription descendDescription = new GenericCommandDescription("descend");
        workflow.addResponse(descendDescription,
                new GenericCommand(descendDescription, new MovePlayerAction(cellar, gameContext, variableProvider)));
        vocabulary.createNewWord("descend", Word.Type.VERB);

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("descend");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString())
                .contains("A dark, damp cellar.")
                .doesNotContain("Welcome to the throne room.");
    }

    @Test
    void movingInto_firesAProcessGatedOnTheDestination_onTheSameTurnAsTheMove() {
        // Mirror image of the above: a Process gated on the destination must fire on the very turn
        // that arrives, not one turn later.
        MessagesHolder messages = new MessagesHolder();
        DescriptionProvider cellarDescription = new DescriptionProvider("cellar", "cellar");
        cellarDescription.setLongDescription("A dark, damp cellar.");
        Location cellar = new Location(cellarDescription,
                                       new GenericContainer(new DescriptionProvider("cellar items"), 10));

        GenericCommandDescription cellarOnlyDescription = new GenericCommandDescription("cellar-only");
        GenericCommand cellarOnlyProcess = new GenericCommand(cellarOnlyDescription,
                new MessageAction("You shiver in the cold."));
        cellarOnlyProcess.addPreCondition(new PlayerAtCondition(cellar, gameContext));
        workflow.addProcess(cellarOnlyDescription, cellarOnlyProcess);

        GenericCommandDescription descendDescription = new GenericCommandDescription("descend");
        workflow.addResponse(descendDescription,
                new GenericCommand(descendDescription, new MovePlayerAction(cellar, gameContext, variableProvider)));
        vocabulary.createNewWord("descend", Word.Type.VERB);

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("descend");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("You shiver in the cold.");
    }

    @Test
    void movingInto_arrivalProcessMessage_nowPrintsAfterTheArrivalDescription() {
        // Documents current, intentional behavior: MovePlayerAction.execute() now appends the
        // arrival process's message onto the destination's own arrival description (via
        // gameContext.runArrivalProcesses().getResultMessage()) rather than having
        // Workflow.runArrivalProcesses() tell it immediately mid-execution - so the description
        // prints first and the arrival process's message follows it. This is the fix for the
        // ordering inconsistency the final review flagged; pinned here so a future change to this
        // ordering is a deliberate decision, not an untested accident.
        MessagesHolder messages = new MessagesHolder();
        DescriptionProvider cellarDescription = new DescriptionProvider("cellar", "cellar");
        cellarDescription.setLongDescription("A dark, damp cellar.");
        Location cellar = new Location(cellarDescription,
                                       new GenericContainer(new DescriptionProvider("cellar items"), 10));

        GenericCommandDescription cellarChillDescription = new GenericCommandDescription("chill");
        GenericCommand cellarChill = new GenericCommand(cellarChillDescription,
                new MessageAction("You shiver in the cold."));
        cellarChill.addPreCondition(new PlayerAtCondition(cellar, gameContext));
        workflow.addArrivalProcess(cellarChillDescription, cellarChill);

        GenericCommandDescription descendDescription = new GenericCommandDescription("descend");
        workflow.addResponse(descendDescription,
                new GenericCommand(descendDescription, new MovePlayerAction(cellar, gameContext, variableProvider)));
        vocabulary.createNewWord("descend", Word.Type.VERB);

        gameLoop.processCommand("descend");

        String output = told.toString();
        assertThat(output).contains("You shiver in the cold.").contains("A dark, damp cellar.");
        assertThat(output.indexOf("A dark, damp cellar."))
                .isLessThan(output.indexOf("You shiver in the cold."));
    }

    @Test
    void aFailingCommand_stillRunsProcessesThatTurn() {
        // Processes must fire once per sub-command attempted regardless of whether it succeeds or
        // fails - the arrival-timing fix only changes WHEN within the turn Processes evaluate state,
        // not whether they run at all. "take" is a recognised verb wired to nothing (see setUp()),
        // so it fails with SM8.
        GenericCommandDescription alwaysDescription = new GenericCommandDescription("always-fires");
        GenericCommand alwaysProcess = new GenericCommand(alwaysDescription,
                new MessageAction("The wind stirs."));
        workflow.addProcess(alwaysDescription, alwaysProcess);

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("take");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains(SystemMessageKey.SM8.defaultText());
        assertThat(told.toString()).contains("The wind stirs.");
    }

    @Test
    void describingCurrentLocation_firesAnArrivalProcessGatedOnIt() {
        GenericCommandDescription hereDescription = new GenericCommandDescription("hush");
        GenericCommand hereProcess = new GenericCommand(hereDescription,
                new MessageAction("The room is silent."));
        hereProcess.addPreCondition(new PlayerAtCondition(gameContext.getCurrentLocation(), gameContext));
        workflow.addArrivalProcess(hereDescription, hereProcess);

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("The room is silent.");
    }

    @Test
    void describingAgain_reFiresTheArrivalProcess_matchingTheOriginalPawDesign() {
        // Documented, intended behaviour (docs/superpowers/specs/2026-09-11-process-arrival-timing-design.md,
        // resolved question 4): an arrival Process re-fires on every redescribe of its location,
        // including an explicit "describe"/"look" - not just the initial move. An author who wants
        // "only once" adds their own guard; the engine does not de-duplicate.
        GenericCommandDescription hereDescription = new GenericCommandDescription("hush");
        GenericCommand hereProcess = new GenericCommand(hereDescription,
                new MessageAction("The room is silent."));
        hereProcess.addPreCondition(new PlayerAtCondition(gameContext.getCurrentLocation(), gameContext));
        workflow.addArrivalProcess(hereDescription, hereProcess);

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe and describe");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        long occurrences = told.toString().lines().filter(line -> line.equals("The room is silent.")).count();
        assertThat(occurrences).isEqualTo(2);
    }
}
