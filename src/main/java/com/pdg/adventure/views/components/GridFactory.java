package com.pdg.adventure.views.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.function.ValueProvider;

import java.util.List;

public class GridFactory {
    public static <T> Grid<T> createGrid(Class<T> type, List<ColumnConfig<T>> columns) {
        Grid<T> grid = new Grid<>(type, false);
        grid.setWidth("500px");
        grid.setEmptyStateText("No data available.");
        columns.forEach(c -> grid.addColumn(c.renderer).setHeader(c.header).setAutoWidth(true).setSortable(c.sortable));
        return grid;
    }

    public static class ColumnConfig<T> {
        ValueProvider<T, ?> renderer;
        String header;
        boolean sortable;

        public ColumnConfig(ValueProvider<T, ?> renderer, String header, boolean sortable) {
            this.renderer = renderer;
            this.header = header;
            this.sortable = sortable;
        }
    }
}
