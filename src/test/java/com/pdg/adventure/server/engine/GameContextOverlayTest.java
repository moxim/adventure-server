package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.PictureOverlayData;

class GameContextOverlayTest {

    private GameContext gameContext;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        gameContext.setCurrentPictureId("room");
    }

    @Test
    void overlaysOnDifferentCells_stackOldestFirst() {
        gameContext.showOverlay(1, 1, "a");
        gameContext.showOverlay(2, 1, "b");

        assertThat(gameContext.getOverlays())
                .containsExactly(new PictureOverlayData(1, 1, "a"), new PictureOverlayData(2, 1, "b"));
    }

    @Test
    void anOverlayOnTheSameCell_replacesTheOldOneAndGoesOnTop() {
        gameContext.showOverlay(1, 1, "a");
        gameContext.showOverlay(2, 1, "b");
        gameContext.showOverlay(1, 1, "c");

        assertThat(gameContext.getOverlays())
                .containsExactly(new PictureOverlayData(2, 1, "b"), new PictureOverlayData(1, 1, "c"));
    }

    @Test
    void showingAnotherPicture_clearsTheOverlays() {
        gameContext.showOverlay(1, 1, "a");

        gameContext.setCurrentPictureId("cellar");

        assertThat(gameContext.getOverlays()).isEmpty();
    }

    @Test
    void showingThePictureAlreadyOnShow_keepsTheOverlays() {
        gameContext.showOverlay(1, 1, "a");

        gameContext.setCurrentPictureId("room");

        assertThat(gameContext.getOverlays()).hasSize(1);
    }

    @Test
    void clearingThePicture_clearsTheOverlays() {
        gameContext.showOverlay(1, 1, "a");

        gameContext.setCurrentPictureId(null);

        assertThat(gameContext.getOverlays()).isEmpty();
    }

    @Test
    void clearOverlays_removesThemAll() {
        gameContext.showOverlay(1, 1, "a");
        gameContext.showOverlay(2, 1, "b");

        gameContext.clearOverlays();

        assertThat(gameContext.getOverlays()).isEmpty();
    }

    @Test
    void getOverlays_returnsACopy() {
        gameContext.showOverlay(1, 1, "a");

        gameContext.getOverlays().clear();

        assertThat(gameContext.getOverlays()).hasSize(1);
    }
}
