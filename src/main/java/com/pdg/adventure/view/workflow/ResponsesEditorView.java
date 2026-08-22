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
        super(anAdventureService, anAccessService, "Response", "Responses for ",
              "A response fires only when the player's verb (and adjective/noun, if set) matches exactly, " +
              "short-circuiting the normal location/item lookup. If its preconditions aren't met, it falls " +
              "through silently to normal handling - no message is shown. Matching a built-in verb " +
              "(help, inventory, quit, look/describe) overrides that built-in for this adventure.",
              "No responses yet. Create one to get started.",
              CommandListType.RESPONSE,
              WorkflowData::getInterceptorCommands);
    }
}
