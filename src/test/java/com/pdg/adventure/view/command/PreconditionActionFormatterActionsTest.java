package com.pdg.adventure.view.command;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.model.action.DestroyActionData;
import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.model.action.InventoryActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.model.action.RemoveActionData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.model.action.WearActionData;

class PreconditionActionFormatterActionsTest {

    private final AdventureData adventureData = new AdventureData();
    private final PreconditionActionFormatter formatter = new PreconditionActionFormatter(adventureData);

    @Test
    void setVariable() {
        assertThat(formatter.formatAction(new SetVariableActionData("b_fill", "1"))).isEqualTo("SETVAR b_fill 1");
    }

    @Test
    void incrementVariable() {
        IncrementVariableActionData a = new IncrementVariableActionData();
        a.setName("score");
        a.setValue("2");
        assertThat(formatter.formatAction(a)).isEqualTo("INCVAR score 2");
    }

    @Test
    void decrementVariable() {
        DecrementVariableActionData a = new DecrementVariableActionData();
        a.setName("score");
        a.setValue("1");
        assertThat(formatter.formatAction(a)).isEqualTo("DECVAR score 1");
    }

    @Test
    void message() {
        MessageActionData a = new MessageActionData();
        a.setMessageId("cage_opened");
        assertThat(formatter.formatAction(a)).isEqualTo("MESSAGE cage_opened");
    }

    @Test
    void create() {
        CreateActionData a = new CreateActionData();
        a.setThingId("chest");
        a.setContainerProviderId("room");
        assertThat(formatter.formatAction(a)).isEqualTo("CREATE_ITEM chest");
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
    void quit() {
        assertThat(formatter.formatAction(new QuitActionData())).isEqualTo("QUIT");
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
