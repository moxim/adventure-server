package com.pdg.adventure.view.item;

import com.vaadin.flow.component.html.Image;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class ItemsMainLayout extends AdventureAppLayout {

    public ItemsMainLayout() {
        String appName = "Items";
        Image appImage = new Image("icons/treasure.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        setPrimarySection(Section.NAVBAR);
    }
}
