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
              "An arrival process runs automatically after a location's description is shown - " +
              "i. e. when the player arrives there by moving, and every time they explicitly look / describe it. " +
              " The verb / adjective / noun combos are not evaluated against the player's input and serve only " +
              "as a hint for you. " +
              "This list is adventure-wide, not scoped to one location: add a 'player is at' precondition " +
              "to gate an entry to a specific location. An unmet precondition simply does nothing that " +
              "time, it is not an error, and it does not stop the process from being checked again on the " +
              "next redescribe. Note: firing on an explicit look / describe can be bypassed if this adventure " +
              "has its own response for the verb 'describe', a command that matches 'describe'/'look', or a " +
              "custom describe action attached elsewhere - firing on arrival by movement is unaffected.",
              "No arrival processes yet. Create one to get started.",
              WorkflowData::getArrivalProcesses, ArrivalCommandEditorView.class);
    }
}
