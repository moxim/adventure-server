package com.pdg.adventure.view.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.action.*;

class PreconditionActionFormatterActionsTest {

    private final AdventureData adventureData = new AdventureData();
    private final PreconditionActionFormatter formatter = new PreconditionActionFormatter(adventureData);

    @Test
    void saveGame() {
        assertThat(formatter.formatAction(new SaveGameActionData())).isEqualTo("SAVE");
    }

    @Test
    void loadGame() {
        assertThat(formatter.formatAction(new LoadGameActionData())).isEqualTo("LOAD");
    }

    @Test
    void setVariable() {
        assertThat(formatter.formatAction(new SetVariableActionData("b_fill", 1))).isEqualTo("SETVAR b_fill 1");
    }

    @Test
    void incrementVariable() {
        IncrementVariableActionData a = new IncrementVariableActionData();
        a.setName("score");
        a.setValue(2);
        assertThat(formatter.formatAction(a)).isEqualTo("INCVAR score 2");
    }

    @Test
    void decrementVariable() {
        DecrementVariableActionData a = new DecrementVariableActionData();
        a.setName("score");
        a.setValue(1);
        assertThat(formatter.formatAction(a)).isEqualTo("DECVAR score 1");
    }

    @Test
    void mes_isShownToTheAuthorAsMsg() {
        MesActionData a = new MesActionData();
        a.setMessageId("no-such-message");
        assertThat(formatter.formatAction(a)).startsWith("MSG ");
    }

    @Test
    void print() {
        PrintActionData a = new PrintActionData();
        a.setName("score");
        assertThat(formatter.formatAction(a)).isEqualTo("PRINT score");
    }

    @Test
    void random() {
        RandomActionData a = new RandomActionData();
        a.setName("dice");
        assertThat(formatter.formatAction(a)).isEqualTo("RANDOM dice");
    }

    @Test
    void listAt_namesThePocket() {
        ItemContainerData pocket = new ItemContainerData("player-pocket");
        pocket.setId("pocket-1");
        adventureData.setPlayerPocket(pocket);
        ListAtActionData a = new ListAtActionData();
        a.setContainerId("pocket-1");

        assertThat(new PreconditionActionFormatter(adventureData).formatAction(a)).isEqualTo("LISTAT pocket");
    }

    @Test
    void listAt_withoutAContainer_showsAQuestionMark() {
        assertThat(formatter.formatAction(new ListAtActionData())).isEqualTo("LISTAT ?");
    }

    @Test
    void newLine() {
        assertThat(formatter.formatAction(new NewLineActionData())).isEqualTo("NEWLINE");
    }

    @Test
    void sysMes() {
        SysMesActionData a = new SysMesActionData();
        a.setMessageKeyId("15");
        assertThat(formatter.formatAction(a)).isEqualTo("SYSMESS 15");
    }

    @Test
    void addVariable() {
        assertThat(formatter.formatAction(new AddVariableActionData("bonus", "score"))).isEqualTo("ADD bonus score");
    }

    @Test
    void subtractVariable() {
        assertThat(formatter.formatAction(new SubtractVariableActionData("cost", "score"))).isEqualTo("MINUS cost score");
    }

    @Test
    void message() {
        MessageActionData a = new MessageActionData();
        a.setMessageId("cage_opened");
        assertThat(formatter.formatAction(a)).isEqualTo("MESSAGE cage_opened");
    }

    @Test
    void message_showsTheSummaryOfTheReferencedMessage() {
        MessageData message = new MessageData("Cage opens", "The cage swings open.");
        adventureData.getMessages().put(message.getId(), message);
        MessageActionData a = new MessageActionData();
        a.setMessageId(message.getId());
        assertThat(formatter.formatAction(a)).isEqualTo("MESSAGE Cage opens");
    }

    @Test
    void message_withBlankSummary_showsTheMessageId() {
        MessageData message = new MessageData(" ", "The cage swings open.");
        adventureData.getMessages().put(message.getId(), message);
        MessageActionData a = new MessageActionData();
        a.setMessageId(message.getId());
        assertThat(new PreconditionActionFormatter(adventureData).formatAction(a))
                .isEqualTo("MESSAGE " + message.getId());
    }

    @Test
    void create() {
        CreateActionData a = new CreateActionData();
        a.setThingId("chest");
        a.setContainerProviderId("room");
        assertThat(formatter.formatAction(a)).isEqualTo("CREATE chest");
    }

    @Test
    void destroy() {
        DestroyActionData a = new DestroyActionData();
        a.setThingId("vase");
        assertThat(formatter.formatAction(a)).isEqualTo("DESTROY vase");
    }

    @Test
    void drop() {
        DropActionData a = new DropActionData();
        a.setThingId("key");
        assertThat(formatter.formatAction(a)).isEqualTo("DROP key");
    }

    @Test
    void take() {
        TakeActionData a = new TakeActionData();
        a.setThingId("coin");
        assertThat(formatter.formatAction(a)).isEqualTo("TAKE coin");
    }

    @Test
    void wear() {
        WearActionData a = new WearActionData();
        a.setThingId("cloak");
        assertThat(formatter.formatAction(a)).isEqualTo("WEAR cloak");
    }

    @Test
    void light() {
        LightActionData a = new LightActionData();
        a.setThingId("torch");
        a.setLumen(50);
        assertThat(formatter.formatAction(a)).isEqualTo("LIGHT torch 50");
    }

    @Test
    void remove() {
        RemoveActionData a = new RemoveActionData();
        a.setThingId("ring");
        assertThat(formatter.formatAction(a)).isEqualTo("REMOVE ring");
    }

    @Test
    void moveItem() {
        MoveItemActionData a = new MoveItemActionData();
        a.setThingId("apple");
        a.setDestinationId("basket");
        assertThat(formatter.formatAction(a)).isEqualTo("MOVE_ITEM apple basket");
    }

    @Test
    void movePlayer() {
        MovePlayerActionData a = new MovePlayerActionData();
        a.setLocationId("cave");
        assertThat(formatter.formatAction(a)).isEqualTo("MOVE_PLAYER cave");
    }

    @Test
    void describe() {
        DescribeActionData a = new DescribeActionData();
        a.setTargetId("sign");
        assertThat(formatter.formatAction(a)).isEqualTo("DESCRIBE sign");
    }

    @Test
    void inventory() {
        assertThat(formatter.formatAction(new InventoryActionData())).isEqualTo("INVENTORY");
    }

    @Test
    void diagnoseVariable() {
        DiagnoseVariableActionData pv = new DiagnoseVariableActionData();
        pv.setName("VISITED");
        assertThat(formatter.formatAction(pv)).isEqualTo("DIAGNOSE_VAR VISITED");
    }

    @Test
    void diagnoseVariables() {
        assertThat(formatter.formatAction(new DiagnoseVariablesActionData())).isEqualTo("DIAGNOSE_VARS");
    }

    @Test
    void turns() {
        assertThat(formatter.formatAction(new TurnsActionData())).isEqualTo("TURNS");
    }

    @Test
    void look() {
        assertThat(formatter.formatAction(new LookActionData())).isEqualTo("LOOK");
    }

    @Test
    void examine() {
        assertThat(formatter.formatAction(new ExamineActionData())).isEqualTo("EXAMINE");
    }

    @Test
    void quit() {
        assertThat(formatter.formatAction(new QuitActionData())).isEqualTo("QUIT");
    }

    @Test
    void breakAction() {
        assertThat(formatter.formatAction(new BreakActionData())).isEqualTo("BREAK");
    }

    @Test
    void nullActionIsRenderedSafely() {
        assertThat(formatter.formatAction(null)).isEqualTo("?");
    }

    @Test
    void formatActionsReturnsOneLinePerEntry() {
        MessageActionData m = new MessageActionData();
        m.setMessageId("dragon_fight");
        assertThat(formatter.formatActions(List.of(m, new QuitActionData())))
                .containsExactly("MESSAGE dragon_fight", "QUIT");
    }

    @Test
    void formatActionsHandlesNullList() {
        assertThat(formatter.formatActions(null)).isEmpty();
    }
}
