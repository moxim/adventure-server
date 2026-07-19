package com.pdg.adventure.view.workflow;

import com.vaadin.flow.component.html.Image;

import com.pdg.adventure.view.component.AdventureAppLayout;

/**
 * Main layout for workflow management views.
 * Provides consistent navigation and header for workflow-related pages.
 */
public class WorkflowMainLayout extends AdventureAppLayout {

    public WorkflowMainLayout() {
        String appName = "Workflow";
        Image appImage = new Image("icons/to-do-list.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        setPrimarySection(Section.NAVBAR);
    }
}
