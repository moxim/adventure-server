package com.pdg.adventure.views.locations;


import com.pdg.adventure.views.components.AdventureAppLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

public class LocationsMainLayout extends AdventureAppLayout {

    public LocationsMainLayout() {
        String title = "Adventure Builder";
        createHeader(title);

        String appName = "Locations";
        Image appImage = new Image("icons/maps.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        RouterLink worldLink = new RouterLink("The World", LocationMapView.class);
        worldLink.setHighlightCondition(HighlightConditions.sameLocation());

        extendDrawer(worldLink);
        setPrimarySection(Section.NAVBAR);
    }
}
