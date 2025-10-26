package com.pdg.adventure.view.message;

import com.pdg.adventure.view.component.AdventureAppLayout;

/**
 * Main layout for message management views.
 * Provides consistent navigation and header for message-related pages.
 */
public class MessagesMainLayout extends AdventureAppLayout {

    public MessagesMainLayout() {
        String title = "Adventure Builder";
        createHeader(title);

        String appName = "Messages";
        createDrawer(appName);

        setPrimarySection(Section.NAVBAR);
    }
}
