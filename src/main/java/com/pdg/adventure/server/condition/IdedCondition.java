package com.pdg.adventure.server.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.Ided;

@Data
@EqualsAndHashCode
public class IdedCondition implements Ided {
    private String id;
}
