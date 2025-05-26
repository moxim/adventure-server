package com.pdg.adventure.views.support;

public enum RouteIds {
    ADVENTURE_ID("adventureId"),
    LOCATION_ID("locationId"),
    COMMAND_ID("commandId");

    private final String routeParam;

    RouteIds(String routeParam) {
        this.routeParam = routeParam;
    }

    public String getValue() {
        return routeParam;
    }
}
