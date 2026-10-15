package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** The message a MesAction shows (the authors see it as "Msg"); same reference as MessageActionData. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MesActionData extends ActionData {
    private String messageId;
}
