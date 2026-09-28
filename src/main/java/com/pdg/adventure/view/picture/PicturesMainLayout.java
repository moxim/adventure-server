package com.pdg.adventure.view.picture;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class PicturesMainLayout extends AdventureAppLayout {

    public PicturesMainLayout() {
        createDrawer("Pictures");
        setPrimarySection(Section.NAVBAR);
    }
}
