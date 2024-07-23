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
      get          GET   VERB        <- addWord(word, type)
      rabbit    RABBIT   NOUN        <- addWord(word, type)
      put          PUT   VERB        <- addWord(word, type)
      take         GET   VERB        <- addWord(word, synonym)
      small      SMALL   ADJECTIVE   <- addWord(word, type)
     */

    public Vocabulary() {
        Map<String, Word> allWords = new HashMap<>();
        data = new VocabularyData(allWords);
    }

    public void addNewWord(String aWord, Word.Type aType) {
        data.addNewWord(aWord, aType);
    }

    public void addSynonym(String aNewSynonym, String anExistingWord) {
        data.addSynonym(aNewSynonym, anExistingWord);
    }

    private void addSynonymForWord(String aText, Word aWord) {
        data.addSynonymForWord(aText, aWord);
    }

    public Word getSynonym(String aWord) {
        Word word = data.findWord(aWord).orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord)));
        return word.getSynonym();
    }

    public String getSynonym(String aWord, Word.Type aType) {
        Word synonym = getSynonym(aWord); // guaranteed to be present
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
            data.addNewWord(word.getText(), word.getType());
        }
    }

    public Optional<Word> findWord(String aWordText) {
        return data.findWord(aWordText);
    }
}
