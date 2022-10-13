package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class Parser {
    private final Vocabulary vocabulary;

    public Parser(Vocabulary aVocabulary) {
        vocabulary = aVocabulary;
    }

    public CommandDescription getInput(BufferedReader aReader) throws IOException {
        Environment.tell("What now? > ");
        String actionCommand = aReader.readLine();
        return handle(actionCommand);
    }

    public CommandDescription handle(String anInput) {
        String verb = Environment.EMPTY_STRING;
        String adjective = Environment.EMPTY_STRING;
        String noun = Environment.EMPTY_STRING;
        String lowerCaseInput = anInput.toLowerCase();

        try (
        Scanner scanner = new Scanner(lowerCaseInput)) {
            while (scanner.hasNext()) {
                String token = scanner.next();
                Vocabulary.Word word = vocabulary.getSynonym(token);
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
        return new CommandDescription(verb, adjective, noun);
    }
}
