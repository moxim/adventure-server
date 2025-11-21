package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.function.Supplier;

import com.pdg.adventure.model.basic.BasicData;
import com.pdg.adventure.server.storage.mongo.CascadeDelete;
import com.pdg.adventure.server.storage.mongo.CascadeSave;

@Document(collection = "vocabularies")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class VocabularyData extends BasicData {

    public static final String YES_TEXT = "yes";
    public static final String NO_TEXT = "no";

    public static final String ID_TEXT = "Id";
    public static final String VERB_TEXT = "verb";
    public static final String ADJECTIVE_TEXT = "adjective";
    public static final String NOUN_TEXT = "noun";

    public static final String CONTAINABLE_TEXT = "containable";
    public static final String WEARABLE_TEXT = "wearable";
    public static final String WORN_TEXT = "worn";

    public static final String SHORT_TEXT = "Short Description";
    public static final String LONG_TEXT = "Long Description";

    public static final String CREATE_TEXT = "Create";
    public static final String DELETE_TEXT = "Delete";
    public static final String EDIT_TEXT = "Edit";
    public static final String BACK_TEXT = "Back";
    public static final String SAVE_TEXT = "Save";
    public static final String CANCEL_TEXT = "Cancel";

    public static final String UNKNOWN_WORD_TEXT = "Word '%s' is not present, yet!";
    public static final String PRESENT_WORD_HAS_DIFFERENT_TYPE_TEXT = "Word '%s' is already present, but has a synonym of different type!";
    public static final String DUPLICATE_WORD_TEXT = "Word '%s' is already present!";
    public static final String EMPTY_STRING = "";

    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private Map<String, Word> words;

    @DBRef(lazy = false)
    private Word takeWord;
    @DBRef(lazy = false)
    private Word dropWord;

    @DBRef(lazy = false)
    private Word inventoryWord;
    @DBRef(lazy = false)
    private Word lookWord;
    @DBRef(lazy = false)
    private Word examineWord;
    @DBRef(lazy = false)
    private Word goWord;
    @DBRef(lazy = false)
    private Word helpWord;
    @DBRef(lazy = false)
    private Word quitWord;
    @DBRef(lazy = false)
    private Word saveWord;
    @DBRef(lazy = false)
    private Word loadWord;

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
                throw new IllegalArgumentException(String.format(PRESENT_WORD_HAS_DIFFERENT_TYPE_TEXT, aWordText));
            }
        }
        return newWord;
    }

    public Optional<Word> removeWord(String aWordText) {
        Optional<Word> result = findWord(aWordText);
        result.ifPresent(word -> words.remove(aWordText));
        return result;
    }

    public void addWord(Word aWord) {
        words.put(aWord.getText(), aWord);
    }

    public Word createSynonym(String aNewSynonym, String anExistingWord) {
        String lowerExistingWord = anExistingWord.toLowerCase();
        Word word = findWord(lowerExistingWord).orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, anExistingWord)));
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

    public void setWords(Collection<Word> aBagOfWords) {
        words.clear();
        for (Word word : aBagOfWords) {
            words.put(word.getText(), word);
        }
    }
}
