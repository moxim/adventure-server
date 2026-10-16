package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** The system message a SysMesAction shows (the authors see it as "SystemMsg"), by its key id, e.g. "15". */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SysMesActionData extends ActionData {
    private String messageKeyId;
}
