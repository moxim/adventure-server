package com.pdg.adventure.view.adventure;

import com.vaadin.flow.dom.Element;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import com.pdg.adventure.model.SoundInstruction;

/**
 * Carries the sound instructions of PLAY actions to the browser. The game only queues instructions
 * ({@link com.pdg.adventure.server.engine.GameContext#playSound}); this turns them into calls of a small script in the
 * page, which plays each sound as many times as asked from audio copies it keeps track of per sound, so a PLAY with 0
 * can stop all copies of that sound.
 * <p>
 * Browsers refuse sound before the player has interacted with the page. The script therefore drops a play that
 * arrives before the first key press, click or touch (a stop is always honoured): the opening room's PLAY is skipped.
 * <p>
 * TODO: Review needed — checked in Chromium (Brave) only. Safari may allow audio per element only when it was first
 *  played inside a user gesture; if PLAY stays silent there, unlock in the gesture and play through a Web Audio
 *  AudioContext instead.
 */
final class SoundPlayer {

    /** Runs a script in the browser; the parameters are referred to as $0, $1, ... */
    @FunctionalInterface
    interface Browser {
        void execute(String aScript, Serializable... someParameters);
    }

    // Installed once per page. $0 of play/stop is the hidden audio element that serves the sound.
    private static final String BOOTSTRAP_SCRIPT = """
            if (!window.abSound) {
              const state = { unlocked: false, copies: new Map() };
              const unlock = () => { state.unlocked = true; };
              ['keydown', 'pointerdown', 'touchstart'].forEach(
                  type => document.addEventListener(type, unlock, { capture: true, once: true }));
              const release = (element, audio) => {
                const set = state.copies.get(element);
                if (set) {
                  set.delete(audio);
                  if (set.size === 0) {
                    state.copies.delete(element);
                  }
                }
              };
              window.abSound = {
                play(element, times) {
                  if (!state.unlocked || !element || !element.src) {
                    return;
                  }
                  let remaining = times;
                  const audio = new Audio(element.src);
                  let set = state.copies.get(element);
                  if (!set) {
                    set = new Set();
                    state.copies.set(element, set);
                  }
                  set.add(audio);
                  const next = () => {
                    if (remaining-- > 0) {
                      audio.currentTime = 0;
                      audio.play().catch(() => release(element, audio));
                    } else {
                      release(element, audio);
                    }
                  };
                  audio.addEventListener('ended', next);
                  audio.addEventListener('error', () => release(element, audio));
                  next();
                },
                stop(element) {
                  const set = state.copies.get(element);
                  if (set) {
                    set.forEach(audio => audio.pause());
                    state.copies.delete(element);
                  }
                },
                stopAll() {
                  state.copies.forEach(set => set.forEach(audio => audio.pause()));
                  state.copies.clear();
                }
              };
            }
            """;

    private static final String PLAY_SCRIPT = "window.abSound && window.abSound.play($0, $1)";
    private static final String STOP_SCRIPT = "window.abSound && window.abSound.stop($0)";
    private static final String STOP_ALL_SCRIPT = "window.abSound && window.abSound.stopAll()";

    private final Browser browser;
    private final Function<String, Element> soundElements;

    /**
     * @param aBrowser       where scripts run
     * @param aSoundElements the hidden audio element serving a sound id, or {@code null} for a sound that does not
     *                       exist (a deleted one), which is skipped
     */
    SoundPlayer(Browser aBrowser, Function<String, Element> aSoundElements) {
        browser = aBrowser;
        soundElements = aSoundElements;
    }

    /** Installs the script and the first-interaction gate in the page; safe to call again. */
    void armFirstInteractionGate() {
        browser.execute(BOOTSTRAP_SCRIPT);
    }

    void apply(List<SoundInstruction> someInstructions) {
        for (SoundInstruction instruction : someInstructions) {
            Element element = soundElements.apply(instruction.soundId());
            if (element == null) {
                continue;
            }
            if (instruction.times() > 0) {
                browser.execute(PLAY_SCRIPT, element, instruction.times());
            } else {
                browser.execute(STOP_SCRIPT, element);
            }
        }
    }

    void stopAll() {
        browser.execute(STOP_ALL_SCRIPT);
    }
}
