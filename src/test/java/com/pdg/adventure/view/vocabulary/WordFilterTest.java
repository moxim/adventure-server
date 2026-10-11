package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.Word;

class WordFilterTest {

    // --- filterByTypeTextOrSynonym(TextField) ---

    @ParameterizedTest(name = "filterByTypeTextOrSynonym with search term \"{0}\" matches everything")
    @ValueSource(strings = {"", "   ", "SWO", "  swo  ", "no", "noun"})
    void filterByTypeTextOrSynonym_withEmptySearchTerm_matchesEverything(String searchTerm) {
        TextField searchField = new TextField();
        searchField.setValue(searchTerm);
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);

        assertThat(filter.test(adapter("sword", Word.Type.NOUN))).isTrue();
    }

    @Test
    void filterByTypeTextOrSynonym_matchesSynonymText() {
        TextField searchField = new TextField();
        searchField.setValue("wea");
        Word weapon = new Word("weapon", Word.Type.NOUN);
        Word sword = new Word("sword", weapon);
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);

        assertThat(filter.test(new DescribableWordAdapter(sword))).isTrue();
    }

    @Test
    void filterByTypeTextOrSynonym_withNullSynonym_doesNotMatchSynonymBranchOrThrow() {
        TextField searchField = new TextField();
        searchField.setValue("wea");
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);

        assertThat(filter.test(adapter("sword", Word.Type.NOUN))).isFalse();
    }

    @Test
    void filterByTypeTextOrSynonym_withNoMatch_returnsFalse() {
        TextField searchField = new TextField();
        searchField.setValue("xyz");
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);

        assertThat(filter.test(adapter("sword", Word.Type.NOUN))).isFalse();
    }

    // --- filterByTypeOrText() ---

    @ParameterizedTest(name = "filterByTypeOrText with filter string \"{0}\" matches everything")
    @ValueSource(strings = {"", "   ", "SWO", "  swo  ", "no", "noun"})
    void filterByTypeOrText_withEmptyFilterString_matchesEverything(String filterString) {
        ComboBox.ItemFilter<Word> filter = WordFilter.filterByTypeOrText();

        assertThat(filter.test(new Word("sword", Word.Type.NOUN), filterString)).isTrue();
    }


    @Test
    void filterByTypeOrText_withNoMatch_returnsFalse() {
        ComboBox.ItemFilter<Word> filter = WordFilter.filterByTypeOrText();

        assertThat(filter.test(new Word("sword", Word.Type.NOUN), "xyz")).isFalse();
    }

    private static DescribableWordAdapter adapter(String text, Word.Type type) {
        return new DescribableWordAdapter(new Word(text, type));
    }
}
