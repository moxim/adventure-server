package com.pdg.adventure.view.direction;

import com.vaadin.flow.component.html.Image;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class DirectionsMainLayout extends AdventureAppLayout {

    public DirectionsMainLayout() {
        String appName = "Direction Commands";
        Image appImage = new Image("icons/path.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        setPrimarySection(Section.NAVBAR);
    }
}
