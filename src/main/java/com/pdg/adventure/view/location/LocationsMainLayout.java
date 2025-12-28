package com.pdg.adventure.view.location;


import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNavItem;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class LocationsMainLayout extends AdventureAppLayout {

    public LocationsMainLayout() {
        String appName = "Locations";
        Image appImage = new Image("icons/maps.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        final SideNavItem navItem = new SideNavItem("The World", LocationMapView.class, VaadinIcon.GLOBE.create());

        extendDrawer(navItem);
        setPrimarySection(Section.NAVBAR);
    }
}
