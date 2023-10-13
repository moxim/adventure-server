package com.pdg.adventure.server.vocabulary;

import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basics.BasicData;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Vocabulary extends BasicData implements Ided {
    public static final String UNKNOWN_WORD_TEXT = "Word '%s' is not present, yet!";
    public static final String EMPTY_STRING = "";

    private Map<String, Word> allWords; // text -> synonym, eg. take -> Word(get, null, VERB)

    /*
      text   | synonym | type
      -------+---------+------
      get          GET   VERB        <- addWord(word, type)
      rabbit    RABBIT   NOUN        <- addWord(word, type)
      put          PUT   VERB        <- addWord(word, type)
      take         GET   VERB        <- addWord(word, synonym)
      small      SMALL   ADJECTIVE   <- addWord(word, type)
     */

    public Vocabulary() {
        allWords = new HashMap<>();
    }

    public void addNewWord(String aWord, Word.Type aType) {
        String lowerText = aWord.toLowerCase();
        Word newWord = new Word(lowerText, aType);
        allWords.put(lowerText, newWord);
    }

    public void addSynonym(String aNewWord, String aSynonym) {
        String lowerSynonym = aSynonym.toLowerCase();
        Word synonym = allWords.get(lowerSynonym);
        if (synonym == null) {
            throw new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aSynonym));
        }
        addSynonymForWord(aNewWord, synonym);
    }

    public void addSynonymForWord(String aText, Word aWord) {
        String lowerText = aText.toLowerCase();
        Word newWord = new Word(lowerText, aWord);
        allWords.put(lowerText, newWord);
    }

    public Word getSynonym(String aWord) {
        Word direct = allWords.get(aWord);
        if (direct == null) {
            return null;
        }
        final Word optionalSynonym = direct.getSynonym();
        if (optionalSynonym == null) {
            return null;
        }
        return optionalSynonym;
    }

    public String getSynonym(String aWord, Word.Type aType) {
        Word synonym = getSynonym(aWord);
        if (synonym == null || synonym.getType() != aType) {
            return EMPTY_STRING;
        }
        return synonym.getText();
    }

    public Word.Type getType(String aWord) {
        Word word = allWords.get(aWord);
        if (word == null) {
            throw new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord));
        }
        return word.getType();
    }

    public Collection<Word> getWords() {
        return allWords.values();
    }
}
