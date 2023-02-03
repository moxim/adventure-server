package com.pdg.adventure.server.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.Ided;

@Data
@EqualsAndHashCode
public class IdedAction implements Ided {
    private String id;
}
