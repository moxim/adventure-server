package com.pdg.adventure.view.workflow;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;

@Route(value = "author/adventures/:adventureId/workflow", layout = WorkflowMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class WorkflowEditorView extends CommandListEditorView {

    public WorkflowEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        super(anAdventureService, anAccessService, "Command", "Workflow for ",
              "Workflow commands run automatically every turn. " +
              "Add preconditions to control when it fires; " +
              "an unmet precondition still shows its message every turn," +
              " it does not silently skip.",
              "No workflow commands yet. Create one to get started.",
              CommandListType.PROCESS,
              WorkflowData::getCommands);
    }
}
