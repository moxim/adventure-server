package com.pdg.adventure.views.directions;

import com.pdg.adventure.views.components.AdventureAppLayout;
import com.vaadin.flow.component.html.H2;

public class DirectionsMainLayout extends AdventureAppLayout {

    private H2 viewTitle;

    public DirectionsMainLayout() {
        String title = "Adventure Builder";
        createHeader(title);

        String appName = "Location Commands";
        createDrawer(appName);

        setPrimarySection(Section.NAVBAR);
    }
}
