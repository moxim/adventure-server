package com.pdg.adventure.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WorkflowData {
    private List<CommandData> commands = new ArrayList<>();
}
