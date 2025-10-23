package com.pdg.adventure.view.command;

import com.vaadin.flow.component.html.H2;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class CommandMainLayout extends AdventureAppLayout {

    private H2 viewTitle;

    public CommandMainLayout() {
        String title = "Adventure Builder";
        createHeader(title);

        String appName = "Location Commands";
        createDrawer(appName);

        setPrimarySection(Section.NAVBAR);
    }
}
