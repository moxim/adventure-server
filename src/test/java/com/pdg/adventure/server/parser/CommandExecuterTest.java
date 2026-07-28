package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.condition.ChanceCondition;
import com.pdg.adventure.server.condition.HereCondition;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.condition.PlayerAtCondition;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

class CommandExecutorTest {
    Container pocket = new GenericContainer(new DescriptionProvider("pocket"), 5);
    GenericCommandDescription smallTreeCommand = new GenericCommandDescription("climb", "small", "tree");
    GenericCommandDescription matchingCommand = new GenericCommandDescription("climb", "small", "tree");
    GenericCommandDescription bigTreeCommand = new GenericCommandDescription("climb", "big", "tree");
    GenericCommandDescription partialCommand = new GenericCommandDescription("climb", "tree");
    ExecutionResult successResult = new ExecutionResult() {
        @Override
        public ExecutionResult.State getExecutionState() {
            return ExecutionResult.State.SUCCESS;
        }

        @Override
        public void setExecutionState(ExecutionResult.State anExecutionState) {
        }

        @Override
        public String getResultMessage() {
            return "You did it!";
        }

        @Override
        public void setResultMessage(String aResultMessage) {
        }

        @Override
        public boolean hasCommandMatched() {
            return true;
        }

        @Override
        public void setCommandHasMatched() {
        }
    };
    Action successAction = new Action() {
        @Override
        public ExecutionResult execute() {
            return successResult;
        }

        @Override
        public String getActionName() {
            return "SuccessAction";
        }
    };
    private final Container locationPocket = new GenericContainer(new DescriptionProvider("locationPocket"), 5);
    Location location = new Location(new DescriptionProvider("location"), locationPocket);
    CommandExecutor sut = new CommandExecutor(pocket, location);

    @Test
    void empptyPocketAndEmptyLocationCannotExecuteAnything() {
        // given
        assertThat(pocket.getCommands()).isEqualTo(List.of());
        assertThat(location.getCommands()).isEqualTo(List.of());

        // when
        ExecutionResult result = sut.execute(smallTreeCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);

        // when
        GenericCommandDescription noCommand = new GenericCommandDescription("");

        result = sut.execute(noCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);

        // when
        result = sut.execute(smallTreeCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void matchingPocketCommandMustExecuteSuccessfully() {
        // given
        pocket.addCommand(new GenericCommand(matchingCommand, successAction));

        // when
        final ExecutionResult result = sut.execute(smallTreeCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void matchingPocketCommandForItemMustExecuteSuccessfully() {
        // given
        Item item = new Item(new DescriptionProvider("tree"), true);
        item.addCommand(
                new GenericCommand(partialCommand, successAction)); // TODO: also test for complete command execution
        pocket.add(item);

        // when
        final ExecutionResult result = sut.execute(partialCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void matchingLocationCommandMustExecuteSuccessfully() {
        // given
        location.addCommand(new GenericCommand(matchingCommand, successAction));

        // when
        final ExecutionResult result = sut.execute(smallTreeCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void matchingCommandForItemMustExecuteSuccessfully() {
        // given
        Item item = new Item(new DescriptionProvider("tree"), true);
        item.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(item);

        // when
        final ExecutionResult result = sut.execute(partialCommand); // TODO: also test for complete command execution

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void matchingCommandForAmbiguousItemsMustFail() {
        // given
        Item someTree = new Item(new DescriptionProvider("tree"), true);
        someTree.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(someTree);
        Item anotherTree = new Item(new DescriptionProvider("tree"), true);
        anotherTree.addCommand(new GenericCommand(partialCommand, successAction));
        pocket.add(anotherTree);

        // when
        final ExecutionResult result = sut.execute(partialCommand); // TODO: also test for complete command execution

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("Which %s do you want to %s?"
                .formatted(smallTreeCommand.getNoun(), smallTreeCommand.getVerb()));
    }

    @Test
    void commandForSpecificItemMustExecuteSuccessfully() {
        // given
        Item someTree = new Item(new DescriptionProvider("tree"), true);
        someTree.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(someTree);
        Item bigTree = new Item(new DescriptionProvider("big", "tree"), true);
        bigTree.addCommand(new GenericCommand(bigTreeCommand, successAction));
        location.getItemContainer().add(bigTree);
        Item smallTree = new Item(new DescriptionProvider("small", "tree"), true);
        smallTree.addCommand(new GenericCommand(matchingCommand, successAction));
        pocket.add(smallTree);

        // when
        final ExecutionResult result = sut.execute(smallTreeCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void commandWithAdjectiveMatchesItemWithoutAdjective() {
        // given: a generic tree whose describe command has no adjective (wildcard)
        Item genericTree = new Item(new DescriptionProvider("tree"), true);
        genericTree.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(genericTree);

        // when: the user issues a command with an adjective
        final ExecutionResult result = sut.execute(smallTreeCommand);

        // then: the wildcard chain still matches and executes
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void ambiguousCommandWithNoNounAsksWhatNotWhich() {
        // given: two different providers (the location itself, and an item in the pocket)
        // each offer the same no-noun, no-adjective command
        GenericCommandDescription jumpCommand = new GenericCommandDescription("jump");
        location.addCommand(new GenericCommand(jumpCommand, successAction));
        Item something = new Item(new DescriptionProvider("thing"), true);
        something.addCommand(new GenericCommand(jumpCommand, successAction));
        pocket.add(something);

        // when
        final ExecutionResult result = sut.execute(jumpCommand);

        // then: no noun was given, so the generic template must be used, not "Which  do you..."
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("What do you want to jump?");
    }

    @Test
    void ambiguousCommandForItemsWithDifferentNamesMustFail() {
        // given
        Item someTree = new Item(new DescriptionProvider("tree"), true);
        someTree.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(someTree);
        Item anotherTree = new Item(new DescriptionProvider("small", "tree"), true);
        anotherTree.addCommand(new GenericCommand(matchingCommand, successAction));
        pocket.add(anotherTree);

        // when
        final ExecutionResult result = sut.execute(partialCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("Which tree do you want to climb?");
    }

    @Test
    void jumpSea_noSuit_runsAllApplicableCommandsThroughExecutor() {
        // End-to-end through CommandExecutor: a single "jump sea" chain with three commands.
        // No suit worn → the WORN command is skipped; the NOT_WORN command and the
        // no-precondition command both apply → two accumulated messages.
        MessagesHolder messages = new MessagesHolder();

        GenericCommand worn = new GenericCommand(new GenericCommandDescription("jump", "sea"),
                new MessageAction("jump_sea_ok", messages));
        worn.addPreCondition(precondition(ExecutionResult.State.FAILURE));        // suit not worn → skipped

        GenericCommand notWorn = new GenericCommand(new GenericCommandDescription("jump", "sea"),
                new MessageAction("jetty_jump_sea_no_suit", messages));
        notWorn.addPreCondition(precondition(ExecutionResult.State.SUCCESS));     // not worn → applies

        GenericCommand always = new GenericCommand(new GenericCommandDescription("jump", "sea"),
                new MessageAction("jump_sea_also_here", messages));               // no precondition

        location.addCommand(worn);
        location.addCommand(notWorn);
        location.addCommand(always);

        ExecutionResult result = sut.execute(new GenericCommandDescription("jump", "sea"));

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("jetty_jump_sea_no_suit" + System.lineSeparator() + "jump_sea_also_here");
    }

    private static PreCondition precondition(ExecutionResult.State state) {
        PreCondition p = mock(PreCondition.class);
        when(p.check()).thenReturn(new CommandExecutionResult(state));
        return p;
    }

    @Test
    void dropSuit_oneCarriedOneInLocation_dropsTheCarriedOneWithoutAskingWhichSuit() {
        // given: the reported bug - two distinct "suit" items sharing the same noun, one
        // carried and one lying in the location, each with its own auto-generated drop chain
        // (not-carried message vs. real drop) exactly as ItemEditorView.createPickupCommands
        // wires them up. Only the carried suit can actually be dropped.
        GameContext gameContext = new GameContext();
        gameContext.setPocket(pocket);
        gameContext.setCurrentLocation(location);
        MessagesHolder messages = new MessagesHolder();

        Item carriedSuit = new Item(new DescriptionProvider("suit"), true);
        Item locationSuit = new Item(new DescriptionProvider("suit"), true);
        addDropChain(carriedSuit, gameContext, messages);
        addDropChain(locationSuit, gameContext, messages);

        pocket.add(carriedSuit);
        location.getItemContainer().add(locationSuit);

        // when
        ExecutionResult result = sut.execute(new GenericCommandDescription("drop", "suit"));

        // then: no ambiguity - the location suit's chain could only print "you don't have it"
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("dropped");
        assertThat(pocket.getContents()).doesNotContain(carriedSuit);
        assertThat(location.getItemContainer().getContents()).contains(carriedSuit);
    }

    @Test
    void getSuit_oneCarriedOneInLocation_picksUpTheLocationOneWithoutAskingWhichSuit() {
        // given: same scenario, mirrored for "get" - only the location suit can actually be
        // picked up, since the carried one's chain could only print "you already have it"
        GameContext gameContext = new GameContext();
        gameContext.setPocket(pocket);
        gameContext.setCurrentLocation(location);
        MessagesHolder messages = new MessagesHolder();

        Item carriedSuit = new Item(new DescriptionProvider("suit"), true);
        Item locationSuit = new Item(new DescriptionProvider("suit"), true);
        addTakeChain(carriedSuit, gameContext, messages);
        addTakeChain(locationSuit, gameContext, messages);

        pocket.add(carriedSuit);
        location.getItemContainer().add(locationSuit);

        // when
        ExecutionResult result = sut.execute(new GenericCommandDescription("take", "suit"));

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("taken");
        assertThat(pocket.getContents()).contains(locationSuit);
        assertThat(location.getItemContainer().getContents()).doesNotContain(locationSuit);
    }

    @Test
    void aChanceGatedExcuse_isNeverTreatedAsApplicable_soGenuineAmbiguityIsPreserved() {
        // given: two "suit" items whose "wave" chains are both textually ambiguous. One item's
        // only distinguishing command is a message-only excuse gated SOLELY by a ChanceCondition
        // set to always succeed (100%) - if disambiguation rolled it to decide, this candidate
        // would wrongly look "excused" and get dropped, silently resolving what is genuinely an
        // ambiguous command. It must not: ChanceCondition.isDeterministic() is false, so this
        // precondition is never evaluated for the disambiguation decision.
        GenericCommandDescription waveSpec = new GenericCommandDescription("wave");

        Item chanceGatedSuit = new Item(new DescriptionProvider("suit"), true);
        GenericCommand excuse = new GenericCommand(waveSpec, new MessageAction("maybe_not",
                new MessagesHolder()));
        excuse.addPreCondition(new ChanceCondition(100));
        chanceGatedSuit.addCommand(excuse);
        chanceGatedSuit.addCommand(new GenericCommand(waveSpec, successAction));
        location.getItemContainer().add(chanceGatedSuit);

        Item plainSuit = new Item(new DescriptionProvider("suit"), true);
        plainSuit.addCommand(new GenericCommand(waveSpec, successAction));
        pocket.add(plainSuit);

        // when
        ExecutionResult result = sut.execute(new GenericCommandDescription("wave", "suit"));

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("Which suit do you want to wave?");
    }

    @Test
    void realDemoDataShape_bothSuitsCarried_dropStaysAmbiguous() {
        // given: reproduces the EXACT command shape currently stored for the demo adventure's
        // neoprene suit (01kt1n50059gxqmq7q03rt5b0x) and swim suit (01ktc2a5f45qeym1jybcvg11dk)
        // items - predating the command-identity refactor, so their commands still carry the
        // full verb+adjective+noun triplet rather than an empty wildcarded noun/adjective.
        GameContext gameContext = new GameContext();
        gameContext.setPocket(pocket);
        gameContext.setCurrentLocation(location);
        MessagesHolder messages = new MessagesHolder();

        Item neoprene = new Item(new DescriptionProvider("neoprene", "suit"), true);
        GenericCommandDescription neopreneDropSpec = new GenericCommandDescription("drop", "neoprene", "suit");
        GenericCommand neopreneExcuse = new GenericCommand(neopreneDropSpec,
                new MessageAction("You don't have the a neoprene suit.", messages));
        neopreneExcuse.addPreCondition(new NotCondition(new CarriedCondition(neoprene, gameContext)));
        neoprene.addCommand(neopreneExcuse);
        GenericCommand neopreneRealDrop = new GenericCommand(neopreneDropSpec,
                moveAction(neoprene, "dropped_neoprene", pocket, location.getItemContainer()));
        neopreneRealDrop.addAction(new Action() {
            @Override
            public ExecutionResult execute() {
                return new CommandExecutionResult(ExecutionResult.State.SUCCESS, "");
            }

            @Override
            public String getActionName() {
                return "RemoveAction";
            }
        });
        neoprene.addCommand(neopreneRealDrop);

        Item swim = new Item(new DescriptionProvider("suit"), true);
        GenericCommandDescription swimDropSpec = new GenericCommandDescription("drop", "suit");
        GenericCommand swimExcuse = new GenericCommand(swimDropSpec,
                new MessageAction("You are not carrying the a swim suit.", messages));
        swimExcuse.addPreCondition(new NotCondition(new CarriedCondition(swim, gameContext)));
        swim.addCommand(swimExcuse);
        swim.addCommand(new GenericCommand(swimDropSpec, moveAction(swim, "dropped_swim", pocket,
                location.getItemContainer())));

        pocket.add(neoprene);
        pocket.add(swim);

        // when
        ExecutionResult result = sut.execute(new GenericCommandDescription("drop", "suit"));

        // then: genuinely ambiguous - both are carried, both have a real drop applicable
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("Which suit do you want to drop?");
    }

    @Test
    void realDemoDataShape_bothSuitsInLocation_getStaysAmbiguous() {
        GameContext gameContext = new GameContext();
        gameContext.setPocket(pocket);
        gameContext.setCurrentLocation(location);
        MessagesHolder messages = new MessagesHolder();

        Item neoprene = new Item(new DescriptionProvider("neoprene", "suit"), true);
        GenericCommandDescription neopreneTakeSpec = new GenericCommandDescription("take", "neoprene", "suit");
        GenericCommand neopreneAlreadyCarried = new GenericCommand(neopreneTakeSpec,
                new MessageAction("You already have the a neoprene suit.", messages));
        neopreneAlreadyCarried.addPreCondition(new CarriedCondition(neoprene, gameContext));
        neoprene.addCommand(neopreneAlreadyCarried);
        GenericCommand neopreneNotHere = new GenericCommand(neopreneTakeSpec,
                new MessageAction("The a neoprene suit is not here.", messages));
        neopreneNotHere.addPreCondition(new NotCondition(new HereCondition(neoprene, gameContext)));
        neoprene.addCommand(neopreneNotHere);
        GenericCommand neopreneRealTake = new GenericCommand(neopreneTakeSpec,
                moveAction(neoprene, "taken_neoprene", location.getItemContainer(), pocket));
        neopreneRealTake.addPreCondition(new HereCondition(neoprene, gameContext));
        neoprene.addCommand(neopreneRealTake);

        Item swim = new Item(new DescriptionProvider("suit"), true);
        GenericCommandDescription swimTakeSpec = new GenericCommandDescription("take", "suit");
        GenericCommand swimAlreadyCarried = new GenericCommand(swimTakeSpec,
                new MessageAction("You already carry the a swim suit.", messages));
        swimAlreadyCarried.addPreCondition(new CarriedCondition(swim, gameContext));
        swim.addCommand(swimAlreadyCarried);
        GenericCommand swimRealTake = new GenericCommand(swimTakeSpec,
                moveAction(swim, "taken_swim", location.getItemContainer(), pocket));
        swimRealTake.addPreCondition(new HereCondition(swim, gameContext));
        swim.addCommand(swimRealTake);

        location.getItemContainer().add(neoprene);
        location.getItemContainer().add(swim);

        // when
        ExecutionResult result = sut.execute(new GenericCommandDescription("take", "suit"));

        // then: genuinely ambiguous - neither carried, both here, both have a real take applicable
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("Which suit do you want to take?");
    }

    @Test
    void realDemoDataShape_bareJump_stillMatchesTheWildcardChain() {
        // given: reproduces the jetty's actual command data - a no-noun "jump" chain plus a
        // "jump sea" chain. A bare "jump" (no noun given) must still resolve to the wildcard
        // chain, since "jump sea" doesn't match a query with no noun at all.
        GameContext gameContext = new GameContext();
        gameContext.setPocket(pocket);
        gameContext.setCurrentLocation(location);
        Item neopreneSuit = new Item(new DescriptionProvider("neoprene", "suit"), true);
        neopreneSuit.setIsWearable(true);
        pocket.add(neopreneSuit);
        addJumpChains(gameContext, new MessagesHolder(), neopreneSuit, sea);

        ExecutionResult result = sut.execute(new GenericCommandDescription("jump"));

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("jetty_jump");
    }

    @Test
    void realDemoDataShape_jumpSeaWhileWearingTheSuit_movesThePlayerAndStillShowsTheAlsoHereFlavour() {
        // given: reproduces the jetty location's ACTUAL command data - two chains both match
        // "jump sea" via the noun wildcard (an empty-noun "jump" chain, and the real
        // "jump sea" chain). The more specific "jump sea" chain must always win over the
        // wildcard "jump" chain regardless of which of its own commands currently applies, and
        // within "jump sea" every currently-applicable command must fire - both the WORN move
        // (via a real MovePlayerAction, so this proves the player actually relocates, not just
        // that a message is printed) and the unconditional "also here" flavour message.
        GameContext gameContext = new GameContext();
        gameContext.setPocket(pocket);
        gameContext.setCurrentLocation(location);
        MessagesHolder messages = new MessagesHolder();

        Item neopreneSuit = new Item(new DescriptionProvider("neoprene", "suit"), true);
        neopreneSuit.setIsWearable(true);
        neopreneSuit.setIsWorn(true);
        pocket.add(neopreneSuit);

        addJumpChains(gameContext, messages, neopreneSuit, sea);

        // when
        ExecutionResult result = sut.execute(new GenericCommandDescription("jump", "sea"));

        // then: not ambiguous - the wildcard "jump" chain must lose to the more specific
        // "jump sea" chain, and both applicable commands within that chain must fire
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(gameContext.getCurrentLocation()).isEqualTo(sea);
        assertThat(result.getResultMessage()).contains("jump_sea_ok").contains("jump_sea_also_here")
                .doesNotContain("jetty_jump_sea_no_suit");
    }

    @Test
    void realDemoDataShape_jumpSeaWithoutTheSuit_explainsWhyAndStillShowsTheAlsoHereFlavour() {
        // given: same jetty shape as above, but the neoprene suit is not worn. "jump sea" must
        // still resolve to the "jump sea" chain (never falling back to the wildcard "jump"
        // chain just because its own currently-applicable command is an excuse rather than a
        // real action), and both the NOT_WORN excuse and the "also here" flavour must fire.
        GameContext gameContext = new GameContext();
        gameContext.setPocket(pocket);
        gameContext.setCurrentLocation(location);
        MessagesHolder messages = new MessagesHolder();

        Item neopreneSuit = new Item(new DescriptionProvider("neoprene", "suit"), true);
        neopreneSuit.setIsWearable(true);
        neopreneSuit.setIsWorn(false);
        pocket.add(neopreneSuit);

        addJumpChains(gameContext, messages, neopreneSuit, sea);

        // when
        ExecutionResult result = sut.execute(new GenericCommandDescription("jump", "sea"));

        // then: the player must not have moved, and the accumulated message is exactly the
        // NOT_WORN excuse plus the unconditional flavour - nothing from the wildcard "jump" chain
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(gameContext.getCurrentLocation()).isEqualTo(location);
        assertThat(result.getResultMessage())
                .isEqualTo("jetty_jump_sea_no_suit" + System.lineSeparator() + "jump_sea_also_here");
    }

    private final Location sea = new Location(new DescriptionProvider("sea"),
            new GenericContainer(new DescriptionProvider("seaPocket"), 5));

    private void addJumpChains(GameContext aGameContext, MessagesHolder aMessages, Item aNeopreneSuit,
                               Location aSea) {
        GenericCommandDescription bareJumpSpec = new GenericCommandDescription("jump");
        location.addCommand(new GenericCommand(bareJumpSpec, new MessageAction("jetty_jump", aMessages)));

        GenericCommandDescription jumpSeaSpec = new GenericCommandDescription("jump", "sea");
        GenericCommand jumpSeaOk = new GenericCommand(jumpSeaSpec, new MessageAction("jump_sea_ok", aMessages));
        jumpSeaOk.addPreCondition(new WornCondition(aNeopreneSuit));
        jumpSeaOk.addPreCondition(new PlayerAtCondition(location, aGameContext));
        jumpSeaOk.addAction(new MovePlayerAction(aSea, aMessages, aGameContext));
        location.addCommand(jumpSeaOk);

        GenericCommand jumpSeaNoSuit = new GenericCommand(jumpSeaSpec,
                new MessageAction("jetty_jump_sea_no_suit", aMessages));
        jumpSeaNoSuit.addPreCondition(new NotCondition(new WornCondition(aNeopreneSuit)));
        location.addCommand(jumpSeaNoSuit);

        location.addCommand(new GenericCommand(jumpSeaSpec, new MessageAction("jump_sea_also_here", aMessages)));
    }

    private void addDropChain(Item anItem, GameContext aGameContext, MessagesHolder aMessages) {
        GenericCommandDescription dropSpec = new GenericCommandDescription("drop");

        GenericCommand notCarried = new GenericCommand(dropSpec, new MessageAction("not_carried", aMessages));
        notCarried.addPreCondition(new NotCondition(new CarriedCondition(anItem, aGameContext)));
        anItem.addCommand(notCarried);

        anItem.addCommand(new GenericCommand(dropSpec, moveAction(anItem, "dropped", aGameContext.getPocket(),
                aGameContext.getCurrentLocation().getItemContainer())));
    }

    private void addTakeChain(Item anItem, GameContext aGameContext, MessagesHolder aMessages) {
        // mirrors ItemEditorView.createPickupCommands' three take variants exactly: already
        // carried, not here, and the real take (gated by HereCondition, unlike drop's success
        // variant which has no precondition of its own).
        GenericCommandDescription takeSpec = new GenericCommandDescription("take");

        GenericCommand alreadyCarried = new GenericCommand(takeSpec, new MessageAction("already_carried", aMessages));
        alreadyCarried.addPreCondition(new CarriedCondition(anItem, aGameContext));
        anItem.addCommand(alreadyCarried);

        GenericCommand notHere = new GenericCommand(takeSpec, new MessageAction("not_here", aMessages));
        notHere.addPreCondition(new NotCondition(new HereCondition(anItem, aGameContext)));
        anItem.addCommand(notHere);

        GenericCommand realTake = new GenericCommand(takeSpec, moveAction(anItem, "taken",
                aGameContext.getCurrentLocation().getItemContainer(), aGameContext.getPocket()));
        realTake.addPreCondition(new HereCondition(anItem, aGameContext));
        anItem.addCommand(realTake);
    }

    private Action moveAction(Item anItem, String aMessage, Container aFrom, Container aTo) {
        return new Action() {
            @Override
            public ExecutionResult execute() {
                aFrom.remove(anItem);
                aTo.add(anItem);
                return new CommandExecutionResult(ExecutionResult.State.SUCCESS, aMessage);
            }

            @Override
            public String getActionName() {
                return aMessage;
            }
        };
    }

}
