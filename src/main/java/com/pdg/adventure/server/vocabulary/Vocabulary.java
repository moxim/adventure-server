package com.pdg.adventure.server.vocabulary;

import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basics.BasicData;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.pdg.adventure.model.VocabularyData.EMPTY_STRING;
import static com.pdg.adventure.model.VocabularyData.UNKNOWN_WORD_TEXT;

// TODO: have each word type in a separate map?
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Vocabulary extends BasicData implements Ided {

    private final VocabularyData data;
//    private Map<String, Word> allWords; // text -> synonym, eg. take -> Word(get, null, VERB)

    /*
      text   | synonym | type
      -------+---------+------
      get          GET   VERB        <- createWord(word, type)
      rabbit    RABBIT   NOUN        <- createWord(word, type)
      put          PUT   VERB        <- createWord(word, type)
      take         GET   VERB        <- createWord(word, synonym)
      small      SMALL   ADJECTIVE   <- createWord(word, type)
     */

    public Vocabulary() {
        Map<String, Word> allWords = new HashMap<>();
        data = new VocabularyData(allWords);
    }

    public Word createNewWord(String aWord, Word.Type aType) {
        return data.createWord(aWord, aType);
    }

    public Word createSynonym(String aNewSynonym, String anExistingWord) {
        return data.createSynonym(aNewSynonym, anExistingWord);
    }

//    private Word addSynonymForWord(String aText, Word aWord) {
//        return data.createSynonymForWord(aText, aWord);
//    }

    public Word findSynonym(String aWord) {
        Word word = data.findWord(aWord).orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord)));
        return word.getSynonym();
    }

    public String findSynonym(String aWord, Word.Type aType) {
        Word synonym = findSynonym(aWord); // guaranteed to be present
        if (synonym.getType() != aType) {
            return EMPTY_STRING;
        }
        return synonym.getText();
    }

    public Word.Type getType(String aWord) {
        Word word = data.findWord(aWord).orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord)));
        return word.getType();
    }

    public Collection<Word> getWords() {
        return data.getWords();
    }

    public void setWords(Collection<Word> words) {
        data.getWords().clear();
        addWords(words);
    }
    public void addWords(Collection<Word> words) {
        for (Word word : words) {
            data.createWord(word.getText(), word.getType());
        }
    }

    public Optional<Word> findWord(String aWordText) {
        return data.findWord(aWordText);
    }
}
