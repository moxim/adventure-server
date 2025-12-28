package com.pdg.adventure.view.vocabulary;


import com.vaadin.flow.component.html.Image;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class VocabularyMainLayout extends AdventureAppLayout {

    public VocabularyMainLayout() {
        String appName = "Vocabulary";
        Image appImage = new Image("icons/grammar.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        setPrimarySection(Section.NAVBAR);
    }
}
