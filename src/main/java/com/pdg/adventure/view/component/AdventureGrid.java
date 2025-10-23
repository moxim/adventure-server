package com.pdg.adventure.view.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;

import java.util.List;

public class AdventureGrid<T> extends Grid<T> {
    public AdventureGrid(Class<T> beanType, List<T> items) {
         super(beanType, false);
         setSizeFull();
         setItems(items);
         setEmptyStateText("No items available.");
     }

     public AdventureGrid<T> withColumn(String property, String header) {
         addColumn(item -> getProperty(item, property)).setHeader(header).setAutoWidth(true);
         return this;
     }

     public AdventureGrid<T> withDoubleClickNavigation(Class<? extends Component> targetView, String anAdventureId, String aLocationId) {
         addItemDoubleClickListener(e ->
             NavigationHelper.navigateTo(targetView, new RouteParameters(
                 new RouteParam("adventureId", anAdventureId),
                 new RouteParam("locationId", aLocationId))));
         return this;
     }

     private Object getProperty(T item, String property) {
         // Simplified property access; replace with reflection or specific getters
         return property;
     }
}
