package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.pdg.adventure.model.basic.DatedData;
import com.pdg.adventure.server.storage.mongo.CascadeDelete;
import com.pdg.adventure.server.storage.mongo.CascadeSave;

@Document(collection = "vocabularies")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class VocabularyData extends DatedData {

    public static final String YES_TEXT = "yes";
    public static final String NO_TEXT = "no";

    public static final String ID_TEXT = "Id";
    public static final String VERB_TEXT = "Verb";
    public static final String ADJECTIVE_TEXT = "Adjective";
    public static final String NOUN_TEXT = "Noun";

    public static final String CONTAINABLE_TEXT = "Containable";
    public static final String WEARABLE_TEXT = "Wearable";
    public static final String WORN_TEXT = "Worn";

    public static final String SHORT_TEXT = "Short Description";
    public static final String LONG_TEXT = "Long Description";

    public static final String CREATE_TEXT = "Create";
    public static final String DELETE_TEXT = "Delete";
    public static final String EDIT_TEXT = "Edit";
    public static final String BACK_TEXT = "Back";
    public static final String SAVE_TEXT = "Save";
    public static final String CANCEL_TEXT = "Cancel";

    public static final String UNKNOWN_WORD_TEXT = "Word '%s' is not present, yet!";
    public static final String PRESENT_WORD_HAS_DIFFERENT_TYPE_TEXT
            = "Word '%s' is already present, but has a synonym of different type!";
    public static final String DUPLICATE_WORD_TEXT = "Word '%s' is already present!";
    public static final String EMPTY_STRING = "";

    /**
     * The wildcard noun an author can pick in the response table: a response keyed on it matches
     * any noun the player typed (or none), like the "_" of the original PAW's "GET _ AUTOG".
     */
    public static final String WILDCARD_NOUN = "~";

    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private Map<String, Word> words;

    public VocabularyData() {
        this(new HashMap<>());
    }

    public VocabularyData(Map<String, Word> words) {
        this.words = words;
    }

    public Word createWord(String aWordText, Word.Type aType) {
        String lowerText = aWordText.toLowerCase();
        Word newWord = words.get(lowerText);
        if (newWord == null) {
            newWord = new Word(lowerText, aType);
            words.put(lowerText, newWord);
        } else {
            if (newWord.getSynonym() == null) {
                newWord.setType(aType);
            } else if (newWord.getType() != aType) {
                // if the word is already present, it must have the same type, or it won't match its synonym
                throw new IllegalArgumentException(PRESENT_WORD_HAS_DIFFERENT_TYPE_TEXT.formatted(aWordText));
            }
        }
        return newWord;
    }

    /**
     * Makes sure the wildcard noun exists as a real, persistable word so the response editor can
     * offer it and a command's {@code @DBRef} noun can point at it. Own guard instead of a
     * "seeded once" flag, so adventures created before the wildcard existed pick it up too, and an
     * existing word of the same text is never retyped or repointed.
     */
    public Word ensureWildcardNoun() {
        return findWord(WILDCARD_NOUN).orElseGet(() -> createWord(WILDCARD_NOUN, Word.Type.NOUN));
    }

    public Optional<Word> removeWord(String aWordText) {
        Optional<Word> result = findWord(aWordText);
        result.ifPresent(_ -> words.remove(aWordText));
        return result;
    }

    public void addWord(Word aWord) {
        words.put(aWord.getText(), aWord);
    }

    public Word createSynonym(String aNewSynonym, String anExistingWord) {
        String lowerExistingWord = anExistingWord.toLowerCase();
        Word word = findWord(lowerExistingWord).orElseThrow(
                () -> new IllegalArgumentException(UNKNOWN_WORD_TEXT.formatted(anExistingWord)));
        return createSynonym(aNewSynonym, word);
    }

    public Word createSynonym(String aNewSynonym, Word anExistingWord) {
        String lowerSynonym = aNewSynonym.toLowerCase();
        Word newSynonym = words.get(lowerSynonym);
        if (newSynonym == null) {
            newSynonym = new Word(lowerSynonym, anExistingWord);
            words.put(lowerSynonym, newSynonym);
        } else {
            newSynonym.setType(anExistingWord.getType());
            newSynonym.setSynonym(anExistingWord);
        }
        return newSynonym;
    }

    public Collection<Word> getWords() {
        return words.values();
    }

    public Collection<Word> getWords(Word.Type aType) {
        return words.values().stream().filter(word -> word.getType() == aType).toList();
    }

    public Word createWord(String aText, Supplier<Word> aWordSupplier) {
        String lowerText = aText.toLowerCase();
        Word word = words.get(lowerText);
        if (word == null) {
            word = new Word(aText, aWordSupplier.get());
            words.put(lowerText, word);
        } else {
            word.setSynonym(aWordSupplier.get());
        }
        return word;
    }

    public Optional<Word> findWord(String aWordText) {
        String lowerText = aWordText.toLowerCase();
        Word word = words.get(lowerText);
        return Optional.ofNullable(word);
    }

    public List<Word> findWordsBySynonym(Word aTarget) {
        return words.values().stream()
                    .filter(w -> !w.equals(aTarget) && aTarget.equals(w.getSynonym()))
                    .toList();
    }

    public void setWords(Collection<Word> aBagOfWords) {
        words.clear();
        for (Word word : aBagOfWords) {
            words.put(word.getText(), word);
        }
    }
}
