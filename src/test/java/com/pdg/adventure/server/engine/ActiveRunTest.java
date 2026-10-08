package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class ActiveRunTest {

    private final ActiveRun activeRun = new ActiveRun();
    private final GameContext gameContext = new GameContext();

    private AdventureRunSession newSession() {
        return new AdventureRunSession(new GameLoop(new Parser(new Vocabulary()), gameContext), gameContext);
    }

    @Test
    void nothingRegistered_isNotActive() {
        assertThat(activeRun.isActive()).isFalse();
    }

    @Test
    void aRegisteredRunWithALivingOwner_isActive() {
        activeRun.register(newSession(), new RunOwner("player-1"));

        assertThat(activeRun.isActive()).isTrue();
    }

    @Test
    void whenTheOwnerIsGone_theRunIsNoLongerActive() {
        RunOwner owner = new RunOwner("player-1");
        activeRun.register(newSession(), owner);

        owner.markGone();

        assertThat(activeRun.isActive()).isFalse();
    }

    @Test
    void whenTheGameIsOver_theRunIsNoLongerActive() {
        AdventureRunSession session = newSession();
        activeRun.register(session, new RunOwner("player-1"));

        session.supersede();

        assertThat(activeRun.isActive()).isFalse();
    }

    @Test
    void release_byTheOwner_clearsTheRun_andReportsTrue() {
        RunOwner owner = new RunOwner("player-1");
        activeRun.register(newSession(), owner);

        assertThat(activeRun.release(owner)).isTrue();
        assertThat(activeRun.isActive()).isFalse();
    }

    @Test
    void release_byAnyoneElse_keepsTheRun_andReportsFalse() {
        activeRun.register(newSession(), new RunOwner("player-1"));

        assertThat(activeRun.release(new RunOwner("player-1"))).isFalse();
        assertThat(activeRun.isActive()).isTrue();
    }

    @Test
    void supersedeActive_endsTheActiveSession() {
        AdventureRunSession session = newSession();
        activeRun.register(session, new RunOwner("player-1"));

        activeRun.supersedeActive();

        assertThat(session.isGameOver()).isTrue();
    }
}
