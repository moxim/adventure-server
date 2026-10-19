package com.pdg.adventure.view.support;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;

import java.util.Optional;

/**
 * The 10 x 10 grid laid over a picture, as on the world map: cells are numbered from 0, the column from left to
 * right and the row from top to bottom. The run view uses it to turn a click on the picture into the cell a CLICKED
 * condition names; the world map and the picture editor lay {@link #createOverlay()} over a picture so an author can
 * see the cells.
 */
public final class PictureGrid {

    public static final int SIZE = 10;

    /** A grid cell, column and row from 0 to {@code SIZE - 1}. */
    public record Cell(int column, int row) {
    }

    private PictureGrid() {
    }

    /**
     * A transparent grid of {@code SIZE x SIZE} clickable cells that covers the whole box it is added to (so that box
     * must be positioned and sized to the picture, with the picture out of the way of the cells). A cell is
     * highlighted under the mouse (see {@code world-map.css}, which the view must load) and a click on it says
     * "Location column : row".
     */
    public static Div createOverlay() {
        Div grid = new Div();
        grid.getStyle().set("position", "absolute").set("inset", "0").set("display", "grid")
            .set("grid-template-columns", "repeat(" + SIZE + ", 1fr)")
            .set("grid-template-rows", "repeat(" + SIZE + ", 1fr)");
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                grid.add(createCell(column, row));
            }
        }
        return grid;
    }

    private static Div createCell(int aColumn, int aRow) {
        Div cell = new Div();
        cell.addClassName("world-map-cell");
        cell.addClickListener(_ -> Notification.show("Location " + aColumn + " : " + aRow));
        return cell;
    }

    /**
     * The cell under a click, given where it landed in the box that shows the picture (offsets from the box's
     * top-left corner) and the sizes of that box and of the picture. The picture is drawn like CSS
     * {@code object-fit: contain}: scaled to fit the box and centred. The grid covers the drawn picture only, so a
     * click in the empty margin beside it, or any size of zero (a picture not loaded yet), gives no cell.
     */
    public static Optional<Cell> cellAt(double aOffsetX, double aOffsetY, double aBoxWidth, double aBoxHeight,
                                        double aPictureWidth, double aPictureHeight) {
        if (aBoxWidth <= 0 || aBoxHeight <= 0 || aPictureWidth <= 0 || aPictureHeight <= 0) {
            return Optional.empty();
        }
        double scale = Math.min(aBoxWidth / aPictureWidth, aBoxHeight / aPictureHeight);
        double drawnWidth = aPictureWidth * scale;
        double drawnHeight = aPictureHeight * scale;
        double x = aOffsetX - (aBoxWidth - drawnWidth) / 2;
        double y = aOffsetY - (aBoxHeight - drawnHeight) / 2;
        if (x < 0 || y < 0 || x >= drawnWidth || y >= drawnHeight) {
            return Optional.empty();
        }
        return Optional.of(new Cell(index(x, drawnWidth), index(y, drawnHeight)));
    }

    private static int index(double anOffset, double aDrawnLength) {
        return Math.min(SIZE - 1, (int) Math.floor(anOffset / aDrawnLength * SIZE));
    }
}
