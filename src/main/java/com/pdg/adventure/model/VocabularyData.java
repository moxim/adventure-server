package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.pdg.adventure.model.basics.BasicData;

@Document
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class VocabularyData extends BasicData {
    public static final String UNKNOWN_WORD_TEXT = "Word '%s' is not present, yet!";
    public static final String PRESENT_WORD_HAS_DIFFERENT_TYPE_TEXT = "Word '%s' is already present, but has a synonym of different type!";
    public static final String DUPLICATE_WORD_TEXT = "Word '%s' is already present!";
    public static final String EMPTY_STRING = "";

    @DBRef
    private Map<String, Word> words;

    boolean simple = false; // TODO: delete this

//    @PersistenceInstantiator
    public VocabularyData() {
        this(new HashMap<>());
    }

    public VocabularyData(Map<String, Word> words) {
        this.words = words;
    }

    public Word createWord(String aWordText, Word.Type aType) {
        String lowerText = aWordText.toLowerCase();
        if (simple) {
            return createWord(lowerText, () -> new Word(lowerText, aType));
        } else {
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
        if (simple) {
            return createWord(lowerSynonym, () -> anExistingWord);
        } else {
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
