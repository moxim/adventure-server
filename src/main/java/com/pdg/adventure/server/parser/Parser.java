package com.pdg.adventure.server.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

import com.pdg.adventure.server.vocabulary.Vocabulary;
import com.pdg.adventure.server.vocabulary.Word;

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
        String verb = Vocabulary.EMPTY_STRING;
        String adjective = Vocabulary.EMPTY_STRING;
        String noun = Vocabulary.EMPTY_STRING;
        String lowerCaseInput = anInput.toLowerCase();

        try (
        Scanner scanner = new Scanner(lowerCaseInput)) {
            while (scanner.hasNext()) {
                String token = scanner.next();
                Word word = vocabulary.getSynonym(token);
                if (word == null) {
                    // don't know this word
                    continue;
                }
                switch (word.getType()) {
                    case NOUN -> noun = word.getText();
                    case VERB -> verb = word.getText();
                    case ADJECTIVE -> adjective = word.getText();
                    default -> throw new IllegalArgumentException("Unknown word type " + word.getType());
                }
            }
        }

        return new GenericCommandDescription(verb, adjective, noun);
    }
}
