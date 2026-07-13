package com.pdg.adventure.view.command;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.model.condition.LowerThanConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.model.condition.WornConditionData;

class PreconditionActionFormatterConditionsTest {

    private final AdventureData adventureData = new AdventureData();
    private final PreconditionActionFormatter formatter = new PreconditionActionFormatter(adventureData);

    private static HereConditionData here(String id) {
        HereConditionData h = new HereConditionData();
        h.setThingId(id);
        return h;
    }

    @Test
    void here() {
        assertThat(formatter.formatCondition(here("bucket"))).isEqualTo("HERE bucket");
    }

    @Test
    void carried() {
        CarriedConditionData c = new CarriedConditionData();
        c.setItemId("key");
        assertThat(formatter.formatCondition(c)).isEqualTo("CARRIED key");
    }

    @Test
    void worn() {
        WornConditionData c = new WornConditionData();
        c.setThingId("cloak");
        assertThat(formatter.formatCondition(c)).isEqualTo("WORN cloak");
    }

    @Test
    void playerAt() {
        PlayerAtConditionData c = new PlayerAtConditionData();
        c.setLocationId("cave");
        assertThat(formatter.formatCondition(c)).isEqualTo("PLAYER_AT cave");
    }

    @Test
    void itemAt() {
        ItemAtConditionData c = new ItemAtConditionData();
        c.setThingId("gem");
        c.setLocationId("vault");
        assertThat(formatter.formatCondition(c)).isEqualTo("ITEM_AT gem vault");
    }

    @Test
    void equals_() {
        EqualsConditionData c = new EqualsConditionData();
        c.setVariableName("score");
        c.setValue("5");
        assertThat(formatter.formatCondition(c)).isEqualTo("EQ score 5");
    }

    @Test
    void greaterThan() {
        GreaterThanConditionData c = new GreaterThanConditionData();
        c.setVariableName("score");
        c.setValue(3);
        assertThat(formatter.formatCondition(c)).isEqualTo("GT score 3");
    }

    @Test
    void lowerThan() {
        LowerThanConditionData c = new LowerThanConditionData();
        c.setVariableName("score");
        c.setValue(10);
        assertThat(formatter.formatCondition(c)).isEqualTo("LT score 10");
    }

    @Test
    void same() {
        SameConditionData c = new SameConditionData();
        c.setVariableNameOne("a");
        c.setVariableNameTwo("b");
        assertThat(formatter.formatCondition(c)).isEqualTo("SAME a b");
    }

    @Test
    void notWrapsLeafWithPrefix() {
        NotConditionData not = new NotConditionData();
        not.setPreCondition(here("dragon"));
        assertThat(formatter.formatCondition(not)).isEqualTo("NOT_HERE dragon");
    }

    @Test
    void nullConditionIsRenderedSafely() {
        assertThat(formatter.formatCondition(null)).isEqualTo("?");
    }

    @Test
    void blankThingIdIsRenderedSafely() {
        assertThat(formatter.formatCondition(here(null))).isEqualTo("HERE ?");
    }

    @Test
    void formatConditionsReturnsOneLinePerEntry() {
        NotConditionData n1 = new NotConditionData();
        n1.setPreCondition(here("dragon"));
        NotConditionData n2 = new NotConditionData();
        n2.setPreCondition(here("chest"));
        assertThat(formatter.formatConditions(List.of(n1, n2)))
                .containsExactly("NOT_HERE dragon", "NOT_HERE chest");
    }

    @Test
    void formatConditionsHandlesNullList() {
        assertThat(formatter.formatConditions(null)).isEmpty();
    }

    @Test
    void resolvesThingIdToItemDescription() {
        ItemData bucket = new ItemData();
        bucket.setId("item-1");
        bucket.setDescriptionData(new DescriptionData("bucket", "a metal bucket"));
        adventureData.getPlayerPocket().getItems().add(bucket);
        // Formatter indexes items at construction time, so build a fresh one after adding the item.
        PreconditionActionFormatter f = new PreconditionActionFormatter(adventureData);
        assertThat(f.formatCondition(here("item-1"))).isEqualTo("HERE bucket");
    }
}
