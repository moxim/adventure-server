package com.pdg.adventure.view.message;

import com.vaadin.flow.component.html.Image;

import com.pdg.adventure.view.component.AdventureAppLayout;

/**
 * Main layout for message management views.
 * Provides consistent navigation and header for message-related pages.
 */
public class MessagesMainLayout extends AdventureAppLayout {

    public MessagesMainLayout() {
        String appName = "Messages";
        Image appImage = new Image("icons/scroll-with-quill.gif", appName);
        appImage.setMaxWidth("100px");
        createDrawer(appName, appImage);

        setPrimarySection(Section.NAVBAR);
    }
}
