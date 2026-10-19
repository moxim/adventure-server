package com.pdg.adventure.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** A click on the picture in the cell x (column), y (row) of the 10 x 10 grid, both counted from 0. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ClickedConditionData extends PreConditionData {
    private Integer x;
    private Integer y;
}
