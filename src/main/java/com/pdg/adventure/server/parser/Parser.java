package com.pdg.adventure.server.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class Parser {
    private static final String SENTENCE_TERMINATOR = ".";

    private final Vocabulary vocabulary;
    private String lastVerb = VocabularyData.EMPTY_STRING;
    private String lastNoun = VocabularyData.EMPTY_STRING;
    private String lastAdjective = VocabularyData.EMPTY_STRING;

    public Parser(Vocabulary aVocabulary) {
        vocabulary = aVocabulary;
    }

    public CommandSequence handle(String anInput) {
        List<GenericCommandDescription> commands = new ArrayList<>();
        SimpleSentence currentSentence = new SimpleSentence();
        boolean currentHasContent = false;

        try (Scanner scanner = new Scanner(withSpacedTerminators(anInput))) {
            while (scanner.hasNext()) {
                String token = scanner.next();
                Word resolved = null;
                boolean isSeparator = SENTENCE_TERMINATOR.equals(token);

                if (!isSeparator) {
                    Optional<Word> optionalWord = vocabulary.findWord(token);
                    if (optionalWord.isEmpty()) {
                        continue; // don't know this word
                    }
                    Word word = optionalWord.get();
                    resolved = word.getSynonym() == null ? word : word.getSynonym();
                    isSeparator = resolved.getType() == Word.Type.CONJUNCTION;
                }

                if (isSeparator) {
                    if (currentHasContent) {
                        commands.add(closeSentence(currentSentence));
                        currentSentence = new SimpleSentence();
                        currentHasContent = false;
                    }
                    continue;
                }

                // resolved is always non-null here: the only way past the isSeparator checks
                // above without a `continue` is through the vocabulary lookup that assigns it.
                populate(currentSentence, resolved);
                currentHasContent = true;
            }
        }

        // A separator (period or conjunction) with nothing after it never opens a trailing
        // segment; but a turn with no separator at all - or with nothing recognisable in it -
        // must still yield exactly one (possibly empty) command, matching the pre-existing
        // single-command contract GameLoop's bare-verb check relies on.
        if (currentHasContent || commands.isEmpty()) {
            commands.add(closeSentence(currentSentence));
        }
        return new CommandSequence(commands);
    }

    // "." is not itself a vocabulary word (it can't be looked up by findWord), so it is split
    // out into its own token here, before the whitespace-based Scanner tokenizing above, rather
    // than being treated as a hardcoded command string like a real word would be.
    private static String withSpacedTerminators(String anInput) {
        return anInput.toLowerCase().replace(SENTENCE_TERMINATOR, " " + SENTENCE_TERMINATOR + " ");
    }

    private static GenericCommandDescription toDescription(SimpleSentence aSentence) {
        return new GenericCommandDescription(aSentence.getVerb(), aSentence.getAdjective(), aSentence.getNoun());
    }

    // Closes one sub-command: infers a missing verb from the last one seen, builds the
    // GenericCommandDescription, then updates the back-reference state from what was actually
    // parsed - regardless of whether GameLoop later succeeds in executing it, since Parser has
    // no visibility into execution outcomes.
    private GenericCommandDescription closeSentence(SimpleSentence aSentence) {
        if (aSentence.getVerb().isEmpty() && !aSentence.getNoun().isEmpty() && !lastVerb.isEmpty()) {
            aSentence.setVerb(lastVerb);
        }
        GenericCommandDescription description = toDescription(aSentence);
        if (!description.getVerb().isEmpty()) {
            lastVerb = description.getVerb();
        }
        if (!description.getNoun().isEmpty()) {
            lastNoun = description.getNoun();
            lastAdjective = description.getAdjective();
        }
        return description;
    }

    private void populate(SimpleSentence aSentence, Word aWord) {
        switch (aWord.getType()) {
            case NOUN -> aSentence.setNoun(aWord.getText());
            case VERB -> aSentence.setVerb(aWord.getText());
            case ADJECTIVE -> aSentence.setAdjective(aWord.getText());
            case PRONOUN -> {
                if (lastNoun.isEmpty()) {
                    throw new UnresolvedReferenceException(
                            "I don't know what '" + aWord.getText() + "' refers to.");
                }
                aSentence.setNoun(lastNoun);
                aSentence.setAdjective(lastAdjective);
            }
            default -> throw new IllegalArgumentException("Unknown word type " + aWord.getType());
        }
    }
}

@Data
class SimpleSentence {
    private String verb = VocabularyData.EMPTY_STRING;
    private String adjective = VocabularyData.EMPTY_STRING;
    private String noun = VocabularyData.EMPTY_STRING;
}
