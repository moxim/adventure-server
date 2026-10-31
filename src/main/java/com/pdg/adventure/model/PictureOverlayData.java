package com.pdg.adventure.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A picture laid over one cell (column, row; each 0 to 9) of the picture on show, as the Overlay action does. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureOverlayData {
    private int column;
    private int row;
    private String pictureId;
}
