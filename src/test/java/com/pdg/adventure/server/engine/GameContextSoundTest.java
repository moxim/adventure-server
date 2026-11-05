package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.SoundInstruction;

class GameContextSoundTest {

    private GameContext gameContext;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
    }

    @Test
    void playSound_queuesInstructionsOldestFirst() {
        gameContext.playSound("a", 2);
        gameContext.playSound("b", 1);
        gameContext.playSound("a", 0);

        assertThat(gameContext.drainSoundInstructions())
                .containsExactly(new SoundInstruction("a", 2), new SoundInstruction("b", 1),
                                 new SoundInstruction("a", 0));
    }

    @Test
    void drain_returnsAndEmptiesTheQueue() {
        gameContext.playSound("a", 1);

        assertThat(gameContext.drainSoundInstructions()).hasSize(1);
        assertThat(gameContext.drainSoundInstructions()).isEmpty();
    }

    @Test
    void playSound_ignoresBlankIdsAndOutOfRangeCounts() {
        gameContext.playSound(null, 1);
        gameContext.playSound("", 1);
        gameContext.playSound("  ", 1);
        gameContext.playSound("a", -1);
        gameContext.playSound("a", 100);

        assertThat(gameContext.drainSoundInstructions()).isEmpty();
    }

    @Test
    void playSound_acceptsTheBoundaries0And99() {
        gameContext.playSound("a", 0);
        gameContext.playSound("a", 99);

        assertThat(gameContext.drainSoundInstructions())
                .containsExactly(new SoundInstruction("a", 0), new SoundInstruction("a", 99));
    }

    @Test
    void queue_survivesChangingThePicture() {
        gameContext.playSound("a", 1);

        gameContext.setCurrentPictureId("room-1");
        gameContext.setCurrentPictureId("room-2");

        assertThat(gameContext.drainSoundInstructions()).containsExactly(new SoundInstruction("a", 1));
    }

    @Test
    void clearSoundInstructions_emptiesTheQueue() {
        gameContext.playSound("a", 1);

        gameContext.clearSoundInstructions();

        assertThat(gameContext.drainSoundInstructions()).isEmpty();
    }
}
