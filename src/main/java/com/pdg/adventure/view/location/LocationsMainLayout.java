package com.pdg.adventure.view.location;


import com.vaadin.flow.component.html.Image;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class LocationsMainLayout extends AdventureAppLayout {

    public LocationsMainLayout() {
        String appName = "Locations";
        Image appImage = new Image("icons/maps.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        setPrimarySection(Section.NAVBAR);
    }
}
