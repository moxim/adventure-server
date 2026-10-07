package com.pdg.adventure.server.engine;

/**
 * A handle a run's view keeps to say "I'm still here", and who is playing. The engine layer sees only this, never a
 * Vaadin type. The view marks it gone when it detaches.
 */
public final class RunOwner {

    private final String playerId;
    private volatile boolean gone;

    public RunOwner(String aPlayerId) {
        playerId = aPlayerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public boolean isGone() {
        return gone;
    }

    public void markGone() {
        gone = true;
    }
}
