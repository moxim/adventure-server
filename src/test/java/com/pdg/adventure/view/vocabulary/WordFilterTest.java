package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.Word;

class WordFilterTest {

    // --- filterByTypeTextOrSynonym(TextField) ---

    @Test
    void filterByTypeTextOrSynonym_withEmptySearchTerm_matchesEverything() {
        TextField searchField = new TextField();
        searchField.setValue("");
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);

        assertThat(filter.test(adapter("sword", Word.Type.NOUN))).isTrue();
    }

    @Test
    void filterByTypeTextOrSynonym_withBlankSearchTerm_matchesEverything() {
        TextField searchField = new TextField();
        searchField.setValue("   ");
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);

        assertThat(filter.test(adapter("sword", Word.Type.NOUN))).isTrue();
    }

    @Test
    void filterByTypeTextOrSynonym_matchesWordTextCaseInsensitively() {
        TextField searchField = new TextField();
        searchField.setValue("SWO");
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);

        assertThat(filter.test(adapter("sword", Word.Type.NOUN))).isTrue();
    }

    @Test
    void filterByTypeTextOrSynonym_matchesWordTypeCaseInsensitively() {
        TextField searchField = new TextField();
        searchField.setValue("no");
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

    @Test
    void filterByTypeTextOrSynonym_trimsSurroundingWhitespaceFromSearchTerm() {
        TextField searchField = new TextField();
        searchField.setValue("  swo  ");
        SerializablePredicate<DescribableWordAdapter> filter = WordFilter.filterByTypeTextOrSynonym(searchField);

        assertThat(filter.test(adapter("sword", Word.Type.NOUN))).isTrue();
    }

    // --- filterByTypeOrText() ---

    @Test
    void filterByTypeOrText_withEmptyFilterString_matchesEverything() {
        ComboBox.ItemFilter<Word> filter = WordFilter.filterByTypeOrText();

        assertThat(filter.test(new Word("sword", Word.Type.NOUN), "")).isTrue();
    }

    @Test
    void filterByTypeOrText_matchesTypeNamePrefixCaseInsensitively() {
        ComboBox.ItemFilter<Word> filter = WordFilter.filterByTypeOrText();

        assertThat(filter.test(new Word("sword", Word.Type.NOUN), "no")).isTrue();
    }

    @Test
    void filterByTypeOrText_matchesWordTextPrefixCaseInsensitively() {
        ComboBox.ItemFilter<Word> filter = WordFilter.filterByTypeOrText();

        assertThat(filter.test(new Word("sword", Word.Type.NOUN), "SWO")).isTrue();
    }

    @Test
    void filterByTypeOrText_withNoMatch_returnsFalse() {
        ComboBox.ItemFilter<Word> filter = WordFilter.filterByTypeOrText();

        assertThat(filter.test(new Word("sword", Word.Type.NOUN), "xyz")).isFalse();
    }

    @Test
    void filterByTypeOrText_trimsSurroundingWhitespaceFromFilterString() {
        ComboBox.ItemFilter<Word> filter = WordFilter.filterByTypeOrText();

        assertThat(filter.test(new Word("sword", Word.Type.NOUN), "  swo  ")).isTrue();
    }

    private static DescribableWordAdapter adapter(String text, Word.Type type) {
        return new DescribableWordAdapter(new Word(text, type));
    }
}
