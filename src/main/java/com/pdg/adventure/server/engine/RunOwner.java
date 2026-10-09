package com.pdg.adventure.server.engine;

import lombok.Getter;

import java.io.Serial;

/**
 * A handle a run's view keeps to say "I'm still here", and who is playing. The engine layer sees only this, never a
 * Vaadin type. The view marks it gone when it detaches.
 */
@Getter
public final class RunOwner implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String playerId;
    private volatile boolean gone;

    public RunOwner(String aPlayerId) {
        playerId = aPlayerId;
    }

    public void markGone() {
        gone = true;
    }
}
