package com.pdg.adventure.server.vocabulary;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: clean up comments from ZVocabulary
 */
public class Vocabulary {
    public static final String UNKNOWN_WORD_TEXT = "Word '%s' is not present, yet!";
    public static final String EMPTY_STRING = "";

    private final Map<String, Word> words; // text -> synonym, eg. take -> Word(get, null, VERB)
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
        words = new HashMap<>();
    }

    public void addSynonym(String aNewWord, String aSynonym) {
        String lowerSynonym = aSynonym.toLowerCase();
        Word synonym = words.get(lowerSynonym);
        if (synonym == null) {
            throw new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aSynonym));
        }
        addSynonymForWord(aNewWord, synonym);
    }

    public Word getSynonym(String aWord) {
        return words.get(aWord);
    }

    public String getSynonym(String aWord, Word.Type aType) {
        Word word = getSynonym(aWord);
        if (word.getType() != aType) {
            return EMPTY_STRING;
        }
        return word.getText();
    }

    public void addWord(String aWord, Word.Type aType) {
        Word word = new Word(aWord.toLowerCase(), aType);
        addWord(word);
    }

    public void addWord(Word aWord) {
        addSynonymForWord(aWord.getText(), aWord);
    }

    private void addSynonymForWord(String aText, Word aWord) {
        String lowerText = aText.toLowerCase();
        words.put(lowerText, aWord);
    }

    public Word.Type getType(String aWord) {
        Word synonym = words.get(aWord);
        if (synonym == null) {
            throw new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord));
        }
        return synonym.getType();
    }

}
