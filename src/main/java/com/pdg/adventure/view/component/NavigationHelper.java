package com.pdg.adventure.view.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteParameters;

import java.util.Optional;

public class NavigationHelper {
    public static Optional<? extends Component> navigateTo(Class<? extends Component> view, RouteParameters params) {
        return UI.getCurrent().navigate(view, params);
    }
}
