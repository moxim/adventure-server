package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.basic.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommandChainData extends BasicData {
    private List<CommandData> commands = new ArrayList<>();
}
