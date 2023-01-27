package com.pdg.adventure.model.basics;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BasicDescriptionData extends BasicData {
    private String adjective;
    private String noun;
}
