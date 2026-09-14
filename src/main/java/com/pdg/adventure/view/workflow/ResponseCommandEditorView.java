package com.pdg.adventure.view.workflow;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;

import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;

@Route(value = "author/adventures/:adventureId/responses/:commandId/edit", layout = WorkflowMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/responses/new", layout = WorkflowMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class ResponseCommandEditorView extends SingleCommandEditorView {

    public ResponseCommandEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        super(anAdventureService, anAccessService, "Response", CommandListType.RESPONSE,
              WorkflowData::getInterceptorCommands, ResponsesEditorView.class);
    }
}
