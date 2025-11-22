package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;

import com.pdg.adventure.model.Word;

public class WordFilter {
    public static SerializablePredicate<DescribableWordAdapter> filterByTypeTextOrSynonym(TextField searchField) {
        return (aWord) -> {
            String searchTerm = searchField.getValue().trim();
            if (searchTerm.isEmpty()) {
                return true;
            }
            Word word = aWord.word();
            Word synonym = word.getSynonym();
            boolean matchesText = matchesTerm(word.getText(), searchTerm);
            boolean matchesType = matchesTerm(word.getType().name(), searchTerm);
            boolean matchesSynonym = synonym != null && matchesTerm(synonym.getText(), searchTerm);
//          boolean matchesId = matchesTerm(word.getId(), searchTerm);
            return matchesText || matchesType || matchesSynonym; // || matchesId;
        };
    }

    private static boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().startsWith(searchTerm.toLowerCase());
    }

    public static ComboBox.ItemFilter<Word> filterByTypeOrText() {
        return (word, filterString) -> {
            String typeName = word.getType().name();
            String text = word.getText();
            String searchTerm = filterString.trim();
            // Show all items when no filter is applied
            if (searchTerm.isEmpty()) {
                return true;
            }
            // Filter by type name (case-insensitive match)
            if (typeName.startsWith(searchTerm.toUpperCase())) {
                return true;
            }
            // Filter by word text (case-insensitive match)
            return text.startsWith(searchTerm.toLowerCase());
        };
    }
}
