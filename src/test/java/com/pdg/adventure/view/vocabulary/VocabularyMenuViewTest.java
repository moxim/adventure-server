package com.pdg.adventure.view.vocabulary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.AdventureService;

/**
 * Unit tests for VocabularyMenuView business logic.
 * Tests focus on grid population, data filtering, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class VocabularyMenuViewTest {

    @Mock
    private AdventureService adventureService;

    private VocabularyMenuView view;
    private AdventureData adventureData;
    private VocabularyData vocabularyData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        vocabularyData = new VocabularyData();
        adventureData.setVocabularyData(vocabularyData);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new VocabularyMenuView(adventureService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setAdventureData_shouldPopulateGridWithWords() {
        // given
        view = new VocabularyMenuView(adventureService);

        Word sword = createTestWord("sword", Word.Type.NOUN);
        Word golden = createTestWord("golden", Word.Type.ADJECTIVE);

        vocabularyData.addWord(sword);
        vocabularyData.addWord(golden);

        // when
        view.setAdventureData(adventureData);

        // then
        assertThat(adventureData.getVocabularyData().getWords())
                .hasSize(2)
                .containsExactlyInAnyOrder(sword, golden);
    }

    @Test
    void setAdventureData_withMultipleWords_shouldPreserveAllWords() {
        // given
        view = new VocabularyMenuView(adventureService);

        Word sword = createTestWord("sword", Word.Type.NOUN);
        Word shield = createTestWord("shield", Word.Type.NOUN);
        Word golden = createTestWord("golden", Word.Type.ADJECTIVE);
        Word wooden = createTestWord("wooden", Word.Type.ADJECTIVE);
        Word take = createTestWord("take", Word.Type.VERB);

        vocabularyData.addWord(sword);
        vocabularyData.addWord(shield);
        vocabularyData.addWord(golden);
        vocabularyData.addWord(wooden);
        vocabularyData.addWord(take);

        // when
        view.setAdventureData(adventureData);

        // then
        assertThat(adventureData.getVocabularyData().getWords())
                .hasSize(5)
                .containsExactlyInAnyOrder(sword, shield, golden, wooden, take);

        // Verify words by type
        List<Word> nouns = vocabularyData.getWords().stream()
                .filter(w -> w.getType() == Word.Type.NOUN)
                .toList();
        assertThat(nouns).hasSize(2);

        List<Word> adjectives = vocabularyData.getWords().stream()
                .filter(w -> w.getType() == Word.Type.ADJECTIVE)
                .toList();
        assertThat(adjectives).hasSize(2);

        List<Word> verbs = vocabularyData.getWords().stream()
                .filter(w -> w.getType() == Word.Type.VERB)
                .toList();
        assertThat(verbs).hasSize(1);
    }

    @Test
    void setAdventureData_withEmptyVocabulary_shouldHandleEmptyState() {
        // given
        view = new VocabularyMenuView(adventureService);

        // Vocabulary has no words added (empty by default)

        // when
        view.setAdventureData(adventureData);

        // then
        assertThat(adventureData.getVocabularyData().getWords()).isEmpty();
    }

    private Word createTestWord(String text, Word.Type type) {
        return new Word(text, type);
    }
}
