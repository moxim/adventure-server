package com.pdg.adventure.model;

import com.pdg.adventure.model.basics.BasicData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Document
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class VocabularyData extends BasicData {
    public static final String UNKNOWN_WORD_TEXT = "Word '%s' is not present, yet!";
    public static final String EMPTY_STRING = "";

    @DBRef
    private Map<String, Word> words = new HashMap<>();

    public void addNewWord(String aWord, Word.Type aType) {
        String lowerText = aWord.toLowerCase();
//        Word newWord = createWord(lowerText, () -> new Word(lowerText, aType));
        Word newWord = words.get(lowerText);
        if (newWord == null) {
            newWord = new Word(lowerText, aType);
            words.put(lowerText, newWord);
        } else {
            // TODO: can this ever happen? if so, then check if the type is the same
            newWord.setType(aType);
        }
    }

    public void addSynonym(String aNewWord, String aSynonym) {
        String lowerSynonym = aSynonym.toLowerCase();
        Word synonym = words.get(lowerSynonym);
        if (synonym == null) {
            throw new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aSynonym));
        }
        addSynonymForWord(aNewWord, synonym);
    }

    public void addSynonymForWord(String aText, Word aWord) {
        String lowerText = aText.toLowerCase();
//        Word newWord = createWord(lowerText, () -> aWord);
        Word newWord = words.get(lowerText);
        if (newWord == null) {
            newWord = new Word(lowerText, aWord);
            words.put(lowerText, newWord);
        } else {
            newWord.setType(aWord.getType());
            newWord.setSynonym(aWord);
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
        if (word == null) {
            return Optional.empty();
        }
        return Optional.of(word); // new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWordText)
    }
}
