package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.io.InputStream;
import java.util.Scanner;

public class Parser {
    private final Vocabulary vocabulary;

    public Parser(Vocabulary aVocabulary) {
        vocabulary = aVocabulary;
    }

    public CommandDescription getInput() {
        Environment.tell("What now? > ");
        InputStream input = System.in;
        String aciontThing = input.toString();
        return handle(aciontThing);
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
