package com.pdg.adventure.view.support;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.view.support.PictureGrid.Cell;

class PictureGridTest extends BrowserlessTest {

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

    @Test
    void overlay_hasTenTimesTenClickableCells() {
        Div overlay = PictureGrid.createOverlay();
        UI.getCurrent().add(overlay);

        assertThat(cells(overlay)).hasSize(100);
    }

    @Test
    void overlay_coversTheWholeBoxItIsPlacedIn() {
        Div overlay = PictureGrid.createOverlay();

        assertThat(overlay.getStyle().get("position")).isEqualTo("absolute");
        assertThat(overlay.getStyle().get("inset")).isEqualTo("0");
        assertThat(overlay.getStyle().get("grid-template-columns")).isEqualTo("repeat(10, 1fr)");
        assertThat(overlay.getStyle().get("grid-template-rows")).isEqualTo("repeat(10, 1fr)");
    }

    @Test
    void overlay_clickingACell_reportsItsColumnAndRow() {
        Div overlay = PictureGrid.createOverlay();
        UI.getCurrent().add(overlay);
        Div cell = cells(overlay).get(3 * 10 + 7);

        ComponentUtil.fireEvent(cell, new ClickEvent<>(cell));

        assertThat(test(find(Notification.class).single()).getText()).isEqualTo("Location 7 : 3");
    }

    private List<Div> cells(Div anOverlay) {
        return find(Div.class, anOverlay).withClassName("world-map-cell").all();
    }
}
