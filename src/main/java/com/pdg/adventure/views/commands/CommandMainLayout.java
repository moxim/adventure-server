package com.pdg.adventure.views.commands;

import com.pdg.adventure.views.components.AdventureAppLayout;
import com.vaadin.flow.component.html.H2;

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
