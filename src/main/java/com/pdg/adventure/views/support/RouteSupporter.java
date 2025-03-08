package com.pdg.adventure.views.support;

public enum RouteSupporter {
    ADVENTURE_ID("adventureId"),
    LOCATION_ID("locationId");

    private final String routeParam;

    RouteSupporter(String routeParam) {
        this.routeParam = routeParam;
    }

    public String getValue() {
        return routeParam;
    }
}
