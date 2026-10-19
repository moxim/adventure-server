package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.PictureMode;

/** The picture mode a PictureModeAction sets: ON, OFF or DEFAULT. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PictureModeActionData extends ActionData {
    private PictureMode mode;
}
