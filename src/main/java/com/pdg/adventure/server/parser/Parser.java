package com.pdg.adventure.server.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class Parser {

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
                boolean isSeparator = SystemMessageKey.SM51.defaultText().equals(token);

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
        return anInput.toLowerCase().replace(SystemMessageKey.SM51.defaultText(), " " + SystemMessageKey.SM51.defaultText() + " ");
    }

    private static GenericCommandDescription toDescription(SimpleSentence aSentence) {
        return new GenericCommandDescription(aSentence.getVerb(), aSentence.getAdjective(), aSentence.getNoun(),
                aSentence.getPreposition(), aSentence.getAdverb(),
                aSentence.getAdjective2(), aSentence.getNoun2());
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
            // The first NOUN/ADJECTIVE seen in a sentence fills the primary slot (the one that
            // is part of a command's match key); a second one - e.g. "ancient" and "machine" in
            // "use spanner on ancient machine" - falls through to noun2/adjective2, which are
            // ambient only (checked by Noun2Condition/Adjective2Condition). Whether an ADJECTIVE
            // is "first" is decided by the primary noun slot, not a separate adjective slot, so
            // that "use OLD spanner on ANCIENT machine" still pairs each adjective with the noun
            // it precedes: "old" arrives before "spanner" has filled the primary noun slot, so it
            // becomes the primary adjective; "ancient" arrives after, so it becomes adjective2.
            case NOUN -> {
                if (aSentence.getNoun().isEmpty()) {
                    aSentence.setNoun(aWord.getText());
                } else {
                    aSentence.setNoun2(aWord.getText());
                }
            }
            case VERB -> aSentence.setVerb(aWord.getText());
            case ADJECTIVE -> {
                if (aSentence.getNoun().isEmpty()) {
                    aSentence.setAdjective(aWord.getText());
                } else {
                    aSentence.setAdjective2(aWord.getText());
                }
            }
            case PREPOSITION -> aSentence.setPreposition(aWord.getText());
            case ADVERB -> aSentence.setAdverb(aWord.getText());
            case PRONOUN -> {
                if (lastNoun.isEmpty()) {
                    throw new UnresolvedReferenceException(
                        "I don't know what '" + aWord.getText() + "' refers to.");
                }
                if (aSentence.getNoun().isEmpty()) {
                    aSentence.setNoun(lastNoun);
                    aSentence.setAdjective(lastAdjective);
                } else {
                    aSentence.setNoun2(lastNoun);
                    aSentence.setAdjective2(lastAdjective);
                }
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
    private String preposition = VocabularyData.EMPTY_STRING;
    private String adverb = VocabularyData.EMPTY_STRING;
    private String adjective2 = VocabularyData.EMPTY_STRING;
    private String noun2 = VocabularyData.EMPTY_STRING;
}
