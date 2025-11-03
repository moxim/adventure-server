package com.pdg.adventure.view.command;

import com.vaadin.flow.component.html.Image;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class CommandMainLayout extends AdventureAppLayout {

    public CommandMainLayout() {
        String appName = "Location Commands";
        Image appImage = new Image("icons/to-do-list.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        setPrimarySection(Section.NAVBAR);
    }
}
