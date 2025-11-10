package com.pdg.adventure.view.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteParameters;

import java.util.Optional;

public class NavigationHelper {
    public static <T extends Component> Optional<T>  navigateTo(Class<T> view, RouteParameters params) {
        return UI.getCurrent().navigate(view, params);
    }
}
