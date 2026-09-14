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
        super(anAdventureService, anAccessService, "Workflow Process", "Workflow for ",
              "Workflow commands run automatically every turn. The verb / adjective / noun combos are not " +
              "evaluated against the player's input and serve only as a hint for you. " +
              "Therefore, you must add preconditions to control when they fire.",
              "No workflow commands yet. Create one to get started.",
              WorkflowData::getCommands, WorkflowCommandEditorView.class);
    }
}
