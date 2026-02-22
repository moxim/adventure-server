package com.pdg.adventure.view.support;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;

import java.util.List;

import com.pdg.adventure.api.Describable;

public class GridProvider<T extends Describable> {
    Grid<T> grid;

    public GridProvider(Class<T> clazz) {
        grid = new Grid<>(clazz, false);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(ViewSupporter::formatDescription).setHeader("Short Description").setSortable(true);
//                .setAutoWidth(true);
        grid.getColumns().forEach(column -> column.setAutoWidth(true));
        grid.setSizeFull();
//        grid.setWidth("500px");
//        grid.setHeight("500px");
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    public Grid<T> getGrid() {
        return grid;
    }

    public void addColumn(ValueProvider<T, ?> valueProvider, String aName) {
        grid.addColumn(valueProvider).setHeader(aName);
    }

    public void addSelectionListener(SelectionListener<Grid<T>, T> aListener) {
        grid.addSelectionListener(aListener);

//        grid.addSelectionListener(selection -> {
//            Optional<T> optionalGridItem = selection.getFirstSelectedItem();
//            if (optionalGridItem.isPresent()) {
//                targetLocationId = optionalGridItem.get().getId();
//                edit.setEnabled(true);
//            } else {
//                edit.setEnabled(false);
//            }
//        });

//        grid.addItemDoubleClickListener(e -> {
//            targetLocationId = e.getItem().getId();
//            navigateToLocationEditor(targetLocationId);
//        });

//        createDataView(locations, grid);


//        LocationsMenuView.TContextMenu contextMenu = new LocationsMenuView.TContextMenu(grid);
    }

    public void addItemDoubleClickListener(ComponentEventListener<ItemDoubleClickEvent<T>> aListener) {
        grid.addItemDoubleClickListener(aListener);
    }

    public void setFilter(SerializablePredicate<T> aFilter, List<T> aListOfThings, TextField aSearchField) {
        final GridListDataView<T> dataView = grid.setItems(aListOfThings);
        aSearchField.addValueChangeListener(_ -> dataView.refreshAll());
        dataView.addFilter(aFilter);
    }
}
