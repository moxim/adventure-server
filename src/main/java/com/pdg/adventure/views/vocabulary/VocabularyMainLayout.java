package com.pdg.adventure.views.vocabulary;


import com.pdg.adventure.views.components.AdventureAppLayout;
import com.pdg.adventure.views.locations.LocationMapView;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

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
