package com.pdg.adventure.view.sound;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class SoundsMainLayout extends AdventureAppLayout {

    public SoundsMainLayout() {
        createDrawer("Sounds");
        setPrimarySection(Section.NAVBAR);
    }
}
