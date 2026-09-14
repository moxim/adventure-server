package com.pdg.adventure.view.vocabulary;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.Word;

class DescribableWordAdapterTest {

    @Test
    void getters_exposeTheUnderlyingWordsTextTypeAndId() {
        Word word = new Word("sword", Word.Type.NOUN);
        DescribableWordAdapter adapter = new DescribableWordAdapter(word);

        assertThat(adapter.getType()).isEqualTo("NOUN");
        assertThat(adapter.getShortDescription()).isEqualTo("sword");
        assertThat(adapter.getId()).isEqualTo(word.getId());
        assertThat(adapter.getAdjective()).isEqualTo("a");
        assertThat(adapter.getNoun()).isEqualTo("n");
        assertThat(adapter.getBasicDescription()).isNull();
        assertThat(adapter.getEnrichedBasicDescription()).isNull();
        assertThat(adapter.getLongDescription()).isNull();
        assertThat(adapter.getEnrichedShortDescription()).isNull();
    }

    @Test
    void setId_delegatesToTheUnderlyingWord() {
        Word word = new Word("sword", Word.Type.NOUN);
        DescribableWordAdapter adapter = new DescribableWordAdapter(word);

        adapter.setId("new-id");

        assertThat(word.getId()).isEqualTo("new-id");
        assertThat(adapter.getId()).isEqualTo("new-id");
    }

    @Test
    void getSynonym_withNoSynonym_returnsEmptyString() {
        Word word = new Word("sword", Word.Type.NOUN);
        DescribableWordAdapter adapter = new DescribableWordAdapter(word);

        assertThat(adapter.getSynonym()).isEmpty();
    }

    @Test
    void getSynonym_withASynonym_returnsItsText() {
        Word weapon = new Word("weapon", Word.Type.NOUN);
        Word sword = new Word("sword", weapon);
        DescribableWordAdapter adapter = new DescribableWordAdapter(sword);

        assertThat(adapter.getSynonym()).isEqualTo("weapon");
    }
}
