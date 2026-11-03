package com.pdg.adventure.view.location;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Collection;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.PictureGrid;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Puts a location into an empty cell of the world map: either one of the locations that are not on the map yet, or a
 * new one made right here. A new location needs a noun from the adventure's vocabulary, may have an adjective and a
 * short description; everything else about it is edited in the location editor.
 */
public class PlaceLocationDialog extends Dialog {

    private final transient AdventureData adventure;
    private final transient AdventureService service;
    private final transient PictureGrid.Cell cell;
    private final transient Runnable onDone;
    private final Grid<LocationData> unplacedGrid = new Grid<>();
    private final VocabularyPickerField noun = new VocabularyPickerField("Noun", "The main theme of this location.");
    private final VocabularyPickerField adjective = new VocabularyPickerField("Adjective",
                                                                              "The qualifier for this location.");
    private final TextField shortDescription = new TextField("Short description");
    private final Tab existingTab = new Tab("Unplaced locations");
    private final Tab newTab = new Tab("New location");
    private final Tabs tabs = new Tabs();
    private final VerticalLayout existingContent = new VerticalLayout();
    private final VerticalLayout newContent = new VerticalLayout();
    private final Button place = new Button("Place", _ -> onPlace());

    public PlaceLocationDialog(AdventureData anAdventure, AdventureService aService, PictureGrid.Cell aCell,
                               Runnable anOnDone) {
        adventure = anAdventure;
        service = aService;
        cell = aCell;
        onDone = anOnDone;
        setHeaderTitle("Place a location at " + cell.column() + " : " + cell.row());

        List<LocationData> unplaced = LocationMapPlacement.unplaced(adventure);
        unplacedGrid.addColumn(ViewSupporter::formatDescription).setHeader("Short Description").setAutoWidth(true);
        unplacedGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        unplacedGrid.setItems(unplaced);
        unplacedGrid.addSelectionListener(_ -> updatePlace());
        existingContent.add(unplacedGrid);

        Collection<Word> nouns = adventure.getVocabularyData().getWords(Word.Type.NOUN);
        noun.populate(nouns);
        noun.addValueChangeListener(_ -> updatePlace());
        adjective.populate(adventure.getVocabularyData().getWords(Word.Type.ADJECTIVE));
        shortDescription.setWidthFull();
        if (nouns.isEmpty()) {
            newContent.add(new Paragraph("The vocabulary has no noun yet. Add a noun to the adventure's vocabulary "
                                         + "first."));
        }
        newContent.add(noun, adjective, shortDescription);
        newContent.setPadding(false);
        existingContent.setPadding(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        if (unplaced.isEmpty()) {
            // nothing to choose from: only a new location can go here
            content.add(newContent);
        } else {
            tabs.add(existingTab, newTab);
            tabs.addSelectedChangeListener(_ -> showSelectedTab());
            newContent.setVisible(false);
            content.add(tabs, existingContent, newContent);
        }
        add(content);

        place.setEnabled(false);
        getFooter().add(new Button("Cancel", _ -> close()), place);
    }

    private boolean isNewTabShown() {
        return tabs.getParent().isEmpty() || tabs.getSelectedTab() == newTab;
    }

    private void showSelectedTab() {
        existingContent.setVisible(!isNewTabShown());
        newContent.setVisible(isNewTabShown());
        updatePlace();
    }

    private void updatePlace() {
        place.setEnabled(isNewTabShown() ? noun.getValue() != null : !unplacedGrid.getSelectedItems().isEmpty());
    }

    private void onPlace() {
        if (isNewTabShown()) {
            if (noun.getValue() == null) {
                return;
            }
            LocationData created = LocationCreator.create(adventure, noun.getValue(), adjective.getValue(),
                                                          shortDescription.getValue());
            LocationMapPlacement.place(created, cell);
            // the adventure stores the new location along with its cell
            LocationMapSaver.saveAdventure(service, adventure);
        } else {
            unplacedGrid.getSelectedItems().stream().findFirst().ifPresent(location -> {
                LocationMapPlacement.place(location, cell);
                LocationMapSaver.saveAll(service, List.of(location));
            });
        }
        close();
        onDone.run();
    }
}
