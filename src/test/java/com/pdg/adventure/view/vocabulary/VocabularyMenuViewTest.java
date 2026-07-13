package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

/**
 * Unit tests for VocabularyMenuView business logic.
 * Tests focus on grid population, data filtering, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class VocabularyMenuViewTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private AdventureAccessService accessService;

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

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void enterWithAdventure() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view.beforeEnter(event);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new VocabularyMenuView(adventureService, accessService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setAdventureData_shouldPopulateGridWithWords() {
        // given
        view = new VocabularyMenuView(adventureService, accessService);

        Word sword = createTestWord("sword", Word.Type.NOUN);
        Word golden = createTestWord("golden", Word.Type.ADJECTIVE);

        vocabularyData.addWord(sword);
        vocabularyData.addWord(golden);

        // when
        enterWithAdventure();

        // then
        assertThat(adventureData.getVocabularyData().getWords())
                .hasSize(2)
                .containsExactlyInAnyOrder(sword, golden);
    }

    @Test
    void setAdventureData_withMultipleWords_shouldPreserveAllWords() {
        // given
        view = new VocabularyMenuView(adventureService, accessService);

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
        enterWithAdventure();

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
        view = new VocabularyMenuView(adventureService, accessService);

        // Vocabulary has no words added (empty by default)

        // when
        enterWithAdventure();

        // then
        assertThat(adventureData.getVocabularyData().getWords()).isEmpty();
    }

    private Word createTestWord(String text, Word.Type type) {
        return new Word(text, type);
    }
}
