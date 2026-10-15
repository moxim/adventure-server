package com.pdg.adventure.model.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** The names of the two variables of a SubtractVariableAction: the first is applied to the second. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SubtractVariableActionData extends ActionData {
    private String firstName;
    private String secondName;
}
