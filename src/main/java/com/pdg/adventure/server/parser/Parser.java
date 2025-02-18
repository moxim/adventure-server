package com.pdg.adventure.server.parser;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

public class Parser {
    private final Vocabulary vocabulary;

    public Parser(Vocabulary aVocabulary) {
        vocabulary = aVocabulary;
    }

    public GenericCommandDescription getInput(BufferedReader aReader) throws IOException {
        String actionCommand = aReader.readLine();
        return handle(actionCommand);
    }

    public GenericCommandDescription handle(String anInput) {
        SimpleSentence simpleSentence = new SimpleSentence();
        String lowerCaseInput = anInput.toLowerCase();

        try (
        Scanner scanner = new Scanner(lowerCaseInput)) {
            while (scanner.hasNext()) {
                String token = scanner.next();
                if (false) {
                    Word optionalWord = vocabulary.findSynonym(token);
                    if (optionalWord == null) {
                        // don't know this word
                        continue;
                    }
                    populate(simpleSentence, optionalWord);
                } else {
                    Optional<Word> optionalWord = vocabulary.findWord(token);
                    if (optionalWord.isEmpty()) {
                        // don't know this word
                        continue;
                    }
                    Word word = optionalWord.get();
                    populate(simpleSentence, word.getSynonym() == null ? word : word.getSynonym());
                }
            }
        }
        return new GenericCommandDescription(simpleSentence.getVerb(), simpleSentence.getAdjective(), simpleSentence.getNoun());
    }

    private void populate(SimpleSentence aSentence, Word aWord) {
        switch (aWord.getType()) {
            case NOUN -> aSentence.setNoun(aWord.getText());
            case VERB -> aSentence.setVerb(aWord.getText());
            case ADJECTIVE -> aSentence.setAdjective(aWord.getText());
            default -> throw new IllegalArgumentException("Unknown word type " + aWord.getType());
        }
    }
}

class SimpleSentence {
    private String verb = VocabularyData.EMPTY_STRING;
    private String adjective = VocabularyData.EMPTY_STRING;
    private String noun = VocabularyData.EMPTY_STRING;

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public void setAdjective(String adjective) {
        this.adjective = adjective;
    }

    public void setNoun(String noun) {
        this.noun = noun;
    }

    public String getVerb() {
        return verb;
    }

    public String getAdjective() {
        return adjective;
    }

    public String getNoun() {
        return noun;
    }
}
