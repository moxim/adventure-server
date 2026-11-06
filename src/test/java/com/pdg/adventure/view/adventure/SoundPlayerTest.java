package com.pdg.adventure.view.adventure;

import com.vaadin.flow.dom.Element;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.SoundInstruction;

class SoundPlayerTest {

    private record Call(String script, List<Serializable> params) {
    }

    private final List<Call> calls = new ArrayList<>();
    private final Map<String, Element> sounds = new HashMap<>();
    private SoundPlayer player;

    @BeforeEach
    void setUp() {
        sounds.put("a", new Element("audio"));
        sounds.put("b", new Element("audio"));
        player = new SoundPlayer((script, params) -> calls.add(new Call(script, List.of(params))), sounds::get);
    }

    @Test
    void play_callsAbSoundPlayWithTheElementAndTheCount() {
        player.apply(List.of(new SoundInstruction("a", 2)));

        assertThat(calls).singleElement().satisfies(call -> {
            assertThat(call.script()).contains("abSound.play($0, $1)");
            assertThat(call.params()).containsExactly(sounds.get("a"), 2);
        });
    }

    @Test
    void zero_callsAbSoundStopWithTheElement() {
        player.apply(List.of(new SoundInstruction("a", 0)));

        assertThat(calls).singleElement().satisfies(call -> {
            assertThat(call.script()).contains("abSound.stop($0)");
            assertThat(call.params()).containsExactly(sounds.get("a"));
        });
    }

    @Test
    void anUnknownSound_isSkippedWithoutError() {
        player.apply(List.of(new SoundInstruction("gone", 1), new SoundInstruction("gone", 0)));

        assertThat(calls).isEmpty();
    }

    @Test
    void instructionsKeepTheirOrder() {
        player.apply(List.of(new SoundInstruction("a", 1), new SoundInstruction("b", 3),
                             new SoundInstruction("a", 0)));

        assertThat(calls).extracting(call -> call.params().getFirst())
                         .containsExactly(sounds.get("a"), sounds.get("b"), sounds.get("a"));
        assertThat(calls.get(2).script()).contains("abSound.stop");
    }

    @Test
    void stopAll_callsAbSoundStopAll() {
        player.stopAll();

        assertThat(calls).singleElement().satisfies(call -> assertThat(call.script()).contains("abSound.stopAll()"));
    }

    @Test
    void armingTheGate_installsTheInteractionListenersOnlyOnce() {
        player.armFirstInteractionGate();

        assertThat(calls).singleElement().satisfies(call -> {
            assertThat(call.script()).contains("keydown", "pointerdown", "touchstart");
            assertThat(call.script()).contains("if (!window.abSound)");
        });
    }

    @Test
    void anEmptyListDoesNothing() {
        player.apply(List.of());

        assertThat(calls).isEmpty();
    }
}
