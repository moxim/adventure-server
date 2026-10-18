package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.VocabularyData;

class GameContextClickTest {

    private GameContext gameContext;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
    }

    @Test
    void isClickAt_isFalseWithoutAClick() {
        assertThat(gameContext.isClickAt(0, 0)).isFalse();
    }

    @Test
    void beginClick_makesIsClickAtTrueOnlyForThatCell() {
        gameContext.beginClick(3, 4);

        assertThat(gameContext.isClickAt(3, 4)).isTrue();
        assertThat(gameContext.isClickAt(4, 3)).isFalse();
        assertThat(gameContext.isClickAt(null, 4)).isFalse();
        assertThat(gameContext.isClickAt(3, null)).isFalse();
    }

    @Test
    void beginClick_emptiesTheTypedFieldsThatWereSet() {
        gameContext.setCurrentVerb("take");
        gameContext.setCurrentAdjective("brass");
        gameContext.setCurrentNoun("lamp");
        gameContext.setCurrentPreposition("on");
        gameContext.setCurrentAdverb("slowly");
        gameContext.setCurrentNoun2("table");
        gameContext.setCurrentAdjective2("big");

        gameContext.beginClick(1, 1);

        assertThat(gameContext.getCurrentVerb()).isEqualTo(VocabularyData.EMPTY_STRING);
        assertThat(gameContext.getCurrentAdjective()).isEqualTo(VocabularyData.EMPTY_STRING);
        assertThat(gameContext.getCurrentNoun()).isEqualTo(VocabularyData.EMPTY_STRING);
        assertThat(gameContext.getCurrentPreposition()).isEqualTo(VocabularyData.EMPTY_STRING);
        assertThat(gameContext.getCurrentAdverb()).isEqualTo(VocabularyData.EMPTY_STRING);
        assertThat(gameContext.getCurrentNoun2()).isEqualTo(VocabularyData.EMPTY_STRING);
        assertThat(gameContext.getCurrentAdjective2()).isEqualTo(VocabularyData.EMPTY_STRING);
    }

    @Test
    void endClick_clearsTheClickAndTheHandledFlag() {
        gameContext.beginClick(3, 4);
        gameContext.markClickHandled();

        gameContext.endClick();

        assertThat(gameContext.isClickAt(3, 4)).isFalse();
        assertThat(gameContext.isClickHandled()).isFalse();
    }

    @Test
    void beginClick_resetsTheHandledFlag() {
        gameContext.beginClick(3, 4);
        gameContext.markClickHandled();
        assertThat(gameContext.isClickHandled()).isTrue();

        gameContext.beginClick(5, 5);

        assertThat(gameContext.isClickHandled()).isFalse();
    }
}
