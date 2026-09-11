package com.pdg.adventure.view.workflow;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;

@Route(value = "author/adventures/:adventureId/arrival", layout = WorkflowMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class ArrivalProcessesEditorView extends CommandListEditorView {

    public ArrivalProcessesEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        super(anAdventureService, anAccessService, "Arrival Process", "Arrival Processes for ",
              "An arrival process runs automatically whenever this location's description is shown - " +
              "when the player arrives by moving here, and every time they explicitly look/describe it. " +
              "Add preconditions to control whether it actually does anything that time; an unmet " +
              "precondition simply does nothing that time, it is not an error, and it does not stop " +
              "the process from being checked again on the next redescribe.",
              "No arrival processes yet. Create one to get started.",
              CommandListType.ARRIVAL,
              WorkflowData::getArrivalProcesses);
    }
}
