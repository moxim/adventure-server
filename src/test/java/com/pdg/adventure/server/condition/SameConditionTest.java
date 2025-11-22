package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.testhelper.TestSupporter;

class SameConditionTest {

    VariableProvider vp = new VariableProvider();

    {
        Variable v1 = new Variable("one", "one");
        Variable v2 = new Variable("two", "twp");
        Variable v3 = new Variable("oneToo", "one");

        vp.set(v1);
        vp.set(v2);
        vp.set(v3);
    }

    @Test
    void isValidFailsForSame() {
        // given
        SameCondition sut = new SameCondition("one", "oneToo", vp);

        // when
        boolean ok = TestSupporter.conditionToBoolean(sut);

        // then
        assertThat(ok).isFalse();
    }

    @Test
    void isNotValidFailsForDifferneces() {
        // given
        SameCondition sut = new SameCondition("one", "two", vp);

        // when
        boolean ok = TestSupporter.conditionToBoolean(sut);

        // then
        assertThat(ok).isFalse();
    }

}
