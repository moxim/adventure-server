package com.pdg.adventure.view.vocabulary;


import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

import com.pdg.adventure.view.component.AdventureAppLayout;
import com.pdg.adventure.view.location.LocationMapView;

public class VocabularyMainLayout extends AdventureAppLayout {

    public VocabularyMainLayout() {
        String title = "Adventure Builder";
        createHeader(title);

        String appName = "Vocabulary";
        createDrawer(appName);

        RouterLink worldLink = new RouterLink("The World", LocationMapView.class);
        worldLink.setHighlightCondition(HighlightConditions.sameLocation());

        extendDrawer(worldLink);
        setPrimarySection(Section.NAVBAR);
    }
}
