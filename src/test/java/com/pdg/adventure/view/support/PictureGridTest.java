package com.pdg.adventure.view.support;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.view.support.PictureGrid.Cell;

class PictureGridTest {

    @Test
    void size_isTen() {
        assertThat(PictureGrid.SIZE).isEqualTo(10);
    }

    @Test
    void pictureFillingTheBox_cornersAndCentre() {
        // 2000 x 1000 picture in a 1000 x 500 box: same shape, no margins
        assertThat(PictureGrid.cellAt(0, 0, 1000, 500, 2000, 1000)).contains(new Cell(0, 0));
        assertThat(PictureGrid.cellAt(999, 499, 1000, 500, 2000, 1000)).contains(new Cell(9, 9));
        assertThat(PictureGrid.cellAt(500, 250, 1000, 500, 2000, 1000)).contains(new Cell(5, 5));
    }

    @Test
    void clickAtTheBoxEdge_isOutside() {
        assertThat(PictureGrid.cellAt(1000, 250, 1000, 500, 2000, 1000)).isEmpty();
        assertThat(PictureGrid.cellAt(500, 500, 1000, 500, 2000, 1000)).isEmpty();
    }

    @Test
    void negativeOffsets_areOutside() {
        assertThat(PictureGrid.cellAt(-1, 10, 1000, 500, 2000, 1000)).isEmpty();
        assertThat(PictureGrid.cellAt(10, -1, 1000, 500, 2000, 1000)).isEmpty();
    }

    @Test
    void squarePictureInAWideBox_sideMarginsAreIgnored() {
        // drawn 500 wide, centred: the picture spans x = 250 .. 750
        assertThat(PictureGrid.cellAt(100, 250, 1000, 500, 500, 500)).isEmpty();
        assertThat(PictureGrid.cellAt(250, 0, 1000, 500, 500, 500)).contains(new Cell(0, 0));
        assertThat(PictureGrid.cellAt(749, 499, 1000, 500, 500, 500)).contains(new Cell(9, 9));
        assertThat(PictureGrid.cellAt(750, 250, 1000, 500, 500, 500)).isEmpty();
    }

    @Test
    void widePictureInATallBox_topAndBottomMarginsAreIgnored() {
        // drawn 500 x 250, centred: the picture spans y = 375 .. 625
        assertThat(PictureGrid.cellAt(250, 100, 500, 1000, 500, 250)).isEmpty();
        assertThat(PictureGrid.cellAt(0, 375, 500, 1000, 500, 250)).contains(new Cell(0, 0));
        assertThat(PictureGrid.cellAt(499, 624, 500, 1000, 500, 250)).contains(new Cell(9, 9));
        assertThat(PictureGrid.cellAt(250, 625, 500, 1000, 500, 250)).isEmpty();
    }

    @Test
    void theCellDoesNotDependOnHowBigThePictureIsShown() {
        Optional<Cell> full = PictureGrid.cellAt(640, 360, 1280, 720, 1280, 720);
        Optional<Cell> half = PictureGrid.cellAt(320, 180, 640, 360, 1280, 720);

        assertThat(full).contains(new Cell(5, 5));
        assertThat(half).isEqualTo(full);
    }

    @Test
    void aPictureNotLoadedYetOrAnEmptyBox_isOutside() {
        assertThat(PictureGrid.cellAt(10, 10, 1000, 500, 0, 0)).isEmpty();
        assertThat(PictureGrid.cellAt(10, 10, 0, 0, 2000, 1000)).isEmpty();
    }
}
