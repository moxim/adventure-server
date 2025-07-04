package com.pdg.adventure.views.vocabulary;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventureEditorView;
import com.pdg.adventure.views.support.GridProvider;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route(value = "adventures/:adventureId/vocabulary", layout = VocabularyMainLayout.class)
@RouteAlias(value = "adventures/vocabulary", layout = VocabularyMainLayout.class)
@PageTitle("Vocabulary")
public class VocabularyMenuView extends VerticalLayout implements SaveListener, GuiListener
//, HasDynamicTitle
{

    private transient final AdventureService adventureService;
    private AdventureData adventureData;
    private VocabularyData vocabularyData;

//    private final String pageTitle = "";
    private Button create;
    private Button edit;
    private Button back;
    private TextField searchField;
    private Div gridContainer;

    @Autowired
    public VocabularyMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        setSizeFull();
        createGUI();
    }

    private void createGUI() {
        final VerticalLayout leftSide = createLeftSide();
        final VerticalLayout rightSide = createRidhtSide();
        HorizontalLayout horizontalLayout = new HorizontalLayout(leftSide, rightSide);
        add(horizontalLayout);
    }

    private VerticalLayout createRidhtSide() {
        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find Word");
        searchField.setTooltipText("Find words by ID, text or type.");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        gridContainer = new Div();
        gridContainer.setSizeFull();
        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);
        return rightSide;
    }

    private VerticalLayout createLeftSide() {
        edit = new Button("Edit Word", e -> {createWordInfoDialog(WordEditorDialogue.EditType.EDIT, null );});
        edit.setEnabled(false);
        create = new Button("Create Word", e -> {createWordInfoDialog(WordEditorDialogue.EditType.NEW, null);});
        back = new Button("Back", event -> {
            UI.getCurrent().navigate(AdventureEditorView.class,
                    new RouteParameters(
                            new RouteParam("adventureId", adventureData.getId()))
            );
        });
        back.addClickShortcut(Key.ESCAPE);

        VerticalLayout vl = new VerticalLayout(create, edit, back);
        return vl;
    }

    private void createWordInfoDialog(WordEditorDialogue.EditType anEditType, DescribableWordAdapter aWord) {
        WordEditorDialogue dialogue = new WordEditorDialogue(vocabularyData);
        dialogue.addGuiListener(this);
        dialogue.addSaveListener(this);
        dialogue.open(anEditType, aWord);
    }


    private void fillGUI() {
        vocabularyData = adventureData.getVocabularyData();
        gridContainer.removeAll();
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);
        gridContainer.add(getVocabularyGrid(vocabularyData, searchField, filter));
    }

    private Grid<DescribableWordAdapter> getVocabularyGrid(VocabularyData aVocabularyData, TextField aSearchField, SerializablePredicate<DescribableWordAdapter> aFilter) {
        GridProvider<DescribableWordAdapter> gridProvider = new GridProvider<>(DescribableWordAdapter.class);
        gridProvider.addColumn(DescribableWordAdapter::getType, "Type");
        gridProvider.addColumn(DescribableWordAdapter::getSynonym, "Synonym");
        Grid<DescribableWordAdapter> grid = gridProvider.getGrid();
        grid.setWidth("500px");
        grid.setHeight("500px");

        List<DescribableWordAdapter> wordList = new ArrayList<>();
        for (Word word : aVocabularyData.getWords()) {
            wordList.add(new DescribableWordAdapter(word));
        }
        final GridListDataView<DescribableWordAdapter> dataView = grid.setItems(wordList);
        aSearchField.addValueChangeListener(e -> dataView.refreshAll());
        dataView.addFilter(aFilter);

        gridProvider.addItemDoubleClickListener(e -> {
            createWordInfoDialog(WordEditorDialogue.EditType.EDIT, e.getItem());
        });

        return grid;
    }

    public void setAdventureData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        updateGui();
    }

    @Override
    public void updateGui() {
        fillGUI();
    }

    @Override
    public void persistData() {
        adventureService.saveAdventureData(adventureData);
        adventureService.saveWordData(adventureData.getVocabularyData().getWords());
        adventureService.saveVocabularyData(adventureData.getVocabularyData());
    }

//    @Override
//    public String getPageTitle() {
//        return pageTitle;
//    }
}
