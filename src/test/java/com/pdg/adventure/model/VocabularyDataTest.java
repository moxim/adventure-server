package com.pdg.adventure.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyDataTest {

    private VocabularyData vocabulary;

    @BeforeEach
    void setUp() {
        vocabulary = new VocabularyData();
    }

    @Test
    void findWordsBySynonym_returnsWordsPointingToTarget() {
        Word examine = vocabulary.createWord("examine", Word.Type.VERB);
        vocabulary.createSynonym("x", examine);
        vocabulary.createSynonym("inspect", examine);

        List<Word> result = vocabulary.findWordsBySynonym(examine);

        assertThat(result).extracting(Word::getText).containsExactlyInAnyOrder("x", "inspect");
    }

    @Test
    void findWordsBySynonym_excludesWordsNotPointingToTarget() {
        Word examine = vocabulary.createWord("examine", Word.Type.VERB);
        Word describe = vocabulary.createWord("describe", Word.Type.VERB);
        vocabulary.createSynonym("look", describe);
        vocabulary.createSynonym("x", examine);

        List<Word> result = vocabulary.findWordsBySynonym(examine);

        assertThat(result).extracting(Word::getText).containsExactly("x");
    }

    @Test
    void findWordsBySynonym_excludesTarget_itself() {
        Word examine = vocabulary.createWord("examine", Word.Type.VERB);

        List<Word> result = vocabulary.findWordsBySynonym(examine);

        assertThat(result).isEmpty();
    }

    @Test
    void findWordsBySynonym_returnsEmpty_whenNoWordsPointToTarget() {
        Word examine = vocabulary.createWord("examine", Word.Type.VERB);
        vocabulary.createWord("describe", Word.Type.VERB);

        List<Word> result = vocabulary.findWordsBySynonym(examine);

        assertThat(result).isEmpty();
    }

    /**
     * Reproduces the exact user story:
     * 1. create "describe"
     * 2. create "look" with synonym "describe"
     * 3. create "examine"
     * 4. create "x" with synonym "examine"
     * 5. assign "describe" as synonym for "examine"
     * After step 5, "x" still points to "examine" — findWordsBySynonym should surface it.
     */
    @Test
    void findWordsBySynonym_userStory_surfacesStaleReference() {
        Word describe = vocabulary.createWord("describe", Word.Type.VERB);
        vocabulary.createSynonym("look", describe);
        Word examine = vocabulary.createWord("examine", Word.Type.VERB);
        vocabulary.createSynonym("x", examine);

        // Step 5: examine becomes a synonym of describe
        examine.setSynonym(describe);
        examine.setType(describe.getType());

        // "x" still points to examine (stale), "look" points directly to describe (fine)
        List<Word> stale = vocabulary.findWordsBySynonym(examine);

        assertThat(stale).extracting(Word::getText).containsExactly("x");
        assertThat(vocabulary.findWordsBySynonym(describe))
                .extracting(Word::getText)
                .containsExactlyInAnyOrder("look", "examine");
    }
}
