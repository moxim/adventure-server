package com.pdg.adventure.view.direction;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class DirectionsMainLayout extends AdventureAppLayout {

    public DirectionsMainLayout() {
        String title = "Adventure Builder";
        createHeader(title);

        String appName = "Direction Commands";
        createDrawer(appName);

        setPrimarySection(Section.NAVBAR);
    }
}
