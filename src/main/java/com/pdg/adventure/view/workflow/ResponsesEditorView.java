package com.pdg.adventure.view.workflow;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;

@Route(value = "author/adventures/:adventureId/responses", layout = WorkflowMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class ResponsesEditorView extends CommandListEditorView {

    public ResponsesEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        super(anAdventureService, anAccessService, "Response Process", "Responses for ",
              "Entries in this list are evaluated only if nothing in the current location and nothing in the " +
              "player's pocket handled that verb / adjective / noun combo already. The verb is required. " +
              "Matching a built-in verb (help, inventory, quit, look / describe) still overrides that built-in " +
              "for this adventure.",
              "No responses yet. Create one to get started.",
              WorkflowData::getInterceptorCommands, ResponseCommandEditorView.class);
    }
}
