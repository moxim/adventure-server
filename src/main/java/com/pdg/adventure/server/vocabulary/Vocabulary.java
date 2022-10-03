package com.pdg.adventure.server.vocabulary;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: clean up comments from ZVocabulary
 */
public class Vocabulary {
    public static final String UNKNOWN_WORD_TEXT = "Word '%s' is not present, yet!";
//    private static final String WORD_ALREADY_PRESENT_TEXT = "Word '%s' is already present!";
//    private static final String WORD_OF_DIFFERENT_TYPE_ALREADY_EXISTS = "Word '%s' is already present, but is of " +
//            "different type (%s)!";

    private Map<String, Word> words; // text -> synonym, eg. take -> Word(get, null, VERB)
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

    public void addWord(String aWord, Word.WordType aType) {
        Word word = new Word(aWord, aType);
        addWord(word);
    }

    public void addWord(Word aWord) {
        words.put(aWord.getText(), aWord);
    }

    public void addSynonym(String aNewWord, Word aSynonym) {
        Word synonym = words.get(aSynonym.getText());
        if (synonym == null) {
            throw new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aSynonym));
        }
        words.put(aNewWord, synonym);
    }

    public Word getSynonym(String aWord) {
        return words.get(aWord);
    }

    public Word.WordType getType(String aWord) {
        Word synonym = words.get(aWord);
        if (synonym == null) {
            throw new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord));
        }
        return synonym.getType();
    }

//
//    public String getRoot(String aWord) {
//        return getRootAsWord(aWord).getWord();
//    }
//
//    public Word getRootAsWord(String aWord) {
//        final Word result = vocabulary.get(aWord);
//        if (result == null) {
//            throw new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord));
//        }
//        return result;
//    }
//
//    public void addSynonym(String aSynonym, String aRoot) {
//        Word root = getRootAsWord(aRoot);
//        if (vocabulary.containsKey(aSynonym)) {
//            throw new IllegalArgumentException(String.format(WORD_ALREADY_PRESENT_TEXT, aSynonym));
//        }
//        vocabulary.put(aSynonym, root);
//    }
//
//    public void addWord(Word aWord) {
//        if (vocabulary.containsKey(aWord.getWord())) { // word is already present
//            if (!hasWord(aWord.getWord(), aWord.getType())) {
//                throw new IllegalArgumentException(String.format(WORD_OF_DIFFERENT_TYPE_ALREADY_EXISTS, aWord, vocabulary.get(aWord.getWord())));
//            }
//            return; // don't add the word again
//        }
//        vocabulary.put(aWord.getWord(), aWord);
//    }
//
//    public boolean hasWord(String aWord, Word.WordType aType) {
//        Word word = vocabulary.get(aWord);
//        return word != null && word.getType() == aType;
//    }
//
//    public boolean hasWord(String aWord) {
//        return vocabulary.containsKey(aWord);
//    }
}
