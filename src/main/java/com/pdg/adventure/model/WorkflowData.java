package com.pdg.adventure.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class WorkflowData implements Serializable {
    private static final long serialVersionUID = 20260911135000L;

    private List<CommandData> commands = new ArrayList<>();
    private List<CommandData> interceptorCommands = new ArrayList<>();
    private List<CommandData> arrivalProcesses = new ArrayList<>();
}
