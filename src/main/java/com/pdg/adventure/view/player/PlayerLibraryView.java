package com.pdg.adventure.view.player;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import com.pdg.adventure.view.adventure.AdventuresMainLayout;

@Route(value = "player/library", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_PLAYER")
public class PlayerLibraryView extends VerticalLayout {
    public PlayerLibraryView() {
        add(new H1("Game Library - Play your purchased games"));
    }
}
