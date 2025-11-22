package com.pdg.adventure.server.vocabulary;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.pdg.adventure.model.VocabularyData.EMPTY_STRING;
import static com.pdg.adventure.model.VocabularyData.UNKNOWN_WORD_TEXT;

import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.BasicData;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Vocabulary extends BasicData implements Ided {

    private final VocabularyData data;

    /*
      text   | type      | synonym
      -------+-----------+---------
      get      VERB           null   <- createWord(word, type)
      rabbit   NOUN           null   <- createWord(word, type)
      put      VERB           null   <- createWord(word, type)
      take     VERB            get   <- createWord(word, synonym)
      small    ADJECTIVE     small   <- createWord(word, type)
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

    public Word findSynonym(String aWord) {
        Word word = data.findWord(aWord)
                        .orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord)));
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
        Word word = data.findWord(aWord)
                        .orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_WORD_TEXT, aWord)));
        return word.getType();
    }

    public Collection<Word> getWords() {
        return data.getWords();
    }

    public void setWords(Collection<Word> words) {
        data.getWords().clear();
        words.forEach(data::addWord);
    }

    public Optional<Word> findWord(String aWordText) {
        return data.findWord(aWordText);
    }
}
