package com.pdg.adventure.view.component;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ResetBackSaveViewTest {

    @Test
    void clickingCancel_firesBacksClickListener() {
        ResetBackSaveView view = new ResetBackSaveView();
        AtomicBoolean backClicked = new AtomicBoolean(false);
        view.getBack().addClickListener(_ -> backClicked.set(true));

        view.getCancel().click();

        assertThat(backClicked).isTrue();
    }

    @Test
    void clickingCancel_doesNotFireResetsClickListener() {
        // Regression test: Cancel used to fire Reset first, which cleared the editor's dirty
        // flag before Back's own click listener ran - silently defeating the
        // "uncommitted changes" leave-page guard that clicking Back alone still shows for the
        // exact same destination. Cancel must trigger only Back.
        ResetBackSaveView view = new ResetBackSaveView();
        AtomicBoolean resetClicked = new AtomicBoolean(false);
        view.getReset().addClickListener(_ -> resetClicked.set(true));

        view.getCancel().click();

        assertThat(resetClicked).isFalse();
    }

    @Test
    void clickingCancel_withNoBackListenerRegistered_doesNotThrow() {
        ResetBackSaveView view = new ResetBackSaveView();

        view.getCancel().click();
    }
}
