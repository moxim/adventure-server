package com.pdg.adventure.views.directions;

import com.pdg.adventure.views.components.AdventureAppLayout;

public class DirectionsMainLayout extends AdventureAppLayout {

    public DirectionsMainLayout() {
        String title = "Adventure Builder";
        createHeader(title);

        String appName = "Direction Commands";
        createDrawer(appName);

        setPrimarySection(Section.NAVBAR);
    }
}
