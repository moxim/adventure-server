package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** The picture an OverlayAction lays over the cell x (column), y (row) of the 20 x 20 grid, both counted from 0. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class OverlayActionData extends ActionData {
    private Integer x;
    private Integer y;
    private String pictureId;
}
