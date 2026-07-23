package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.parser.GenericCommandDescription;

@Service
public class WorkflowMapper {

    private final CommandMapper commandMapper;

    public WorkflowMapper(CommandMapper aCommandMapper) {
        commandMapper = aCommandMapper;
    }

    // Workflow can only be constructed with its owning GameContext (see Workflow(GameContext)),
    // so this isn't a symmetric Mapper<WorkflowData, Workflow>. It instead layers the author's
    // persisted commands onto an already-constructed runtime Workflow, e.g. right after
    // GameContext.setUpWorkflows().
    public void populate(WorkflowData aWorkflowData, Workflow aWorkflow) {
        for (CommandData commandData : aWorkflowData.getCommands()) {
            Command command = commandMapper.mapToBO(commandData);
            CommandDescription description = command.getDescription();
            aWorkflow.addPreCommand((GenericCommandDescription) description, command);
        }
    }
}
