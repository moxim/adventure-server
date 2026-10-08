package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Component;

/**
 * The run currently active in this browser session, if any. Session-scoped (one per Vaadin session), so it
 * is what makes "one game per browser session" enforceable. A run is active while its owner's view is still
 * there and the game isn't over; a refreshed or crashed tab can look alive for a while (Vaadin only notices
 * dead UIs after missed heartbeats), which is why the view offers an explicit takeover.
 */
@Component
@PerBrowserSession
public class ActiveRun {

    private AdventureRunSession session;
    private RunOwner owner;

    public synchronized boolean isActive() {
        return session != null && !owner.isGone() && !session.isGameOver();
    }

    public synchronized void register(AdventureRunSession aSession, RunOwner anOwner) {
        session = aSession;
        owner = anOwner;
    }

    /** Ends the active run, if there is one, as superseded by a newer start. */
    public synchronized void supersedeActive() {
        if (isActive()) {
            session.supersede();
        }
    }

    /**
     * Clears the run, but only if {@code anOwner} still owns it - a late release from an old view must not
     * clear a newer run.
     *
     * @return true if the run was cleared
     */
    public synchronized boolean release(RunOwner anOwner) {
        if (owner != anOwner) {
            return false;
        }
        session = null;
        owner = null;
        return true;
    }
}
