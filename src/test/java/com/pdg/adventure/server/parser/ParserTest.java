package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class ParserTest {

    @Test
    void handle_unrecognisedWordsAreSkipped_returnsOneCommand() {
        // given
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("PaRsEr", Word.Type.NOUN);
        Parser parser = new Parser(vocabulary);

        // when
        CommandSequence sequence = parser.handle("hello, parser I am your trusty parser");

        // then
        assertThat(sequence.commands()).hasSize(1);
        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getVerb()).isEmpty();
        assertThat(command.getAdjective()).isEmpty();
        assertThat(command.getNoun()).isEqualTo("parser");
    }

    @Test
    void handle_and_splitsIntoTwoCommands() {
        // given
        Parser parser = new Parser(vocabularyWithTakeSwordAndKillOgre());

        // when
        CommandSequence sequence = parser.handle("take sword and kill ogre");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(0).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(0).getNoun()).isEqualTo("sword");
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("kill");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("ogre");
    }

    @Test
    void handle_then_isRegisteredAsSynonymOfAnd_andAlsoSplits() {
        // given
        Vocabulary vocabulary = vocabularyWithTakeSwordAndKillOgre();
        Parser parser = new Parser(vocabulary);

        // when
        CommandSequence sequence = parser.handle("take sword then kill ogre");

        // then
        assertThat(vocabulary.getType("then")).isEqualTo(Word.Type.CONJUNCTION);
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(0).getNoun()).isEqualTo("sword");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("ogre");
    }

    @Test
    void handle_period_splitsIntoTwoCommands_equivalentToAnd() {
        // given
        Parser parser = new Parser(vocabularyWithTakeSwordAndKillOgre());

        // when
        CommandSequence sequence = parser.handle("take sword. kill ogre");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(0).getNoun()).isEqualTo("sword");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("ogre");
    }

    @Test
    void handle_trailingPeriod_doesNotProduceAnEmptyThirdCommand() {
        // given
        Parser parser = new Parser(vocabularyWithTakeSwordAndKillOgre());

        // when
        CommandSequence sequence = parser.handle("take sword.");

        // then
        assertThat(sequence.commands()).hasSize(1);
        assertThat(sequence.commands().getFirst().getNoun()).isEqualTo("sword");
    }

    @Test
    void handle_leadingConjunction_isForgiven_doesNotProduceAnEmptyFirstCommand() {
        // given
        Parser parser = new Parser(vocabularyWithTakeSwordAndKillOgre());

        // when
        CommandSequence sequence = parser.handle("and kill ogre");

        // then
        assertThat(sequence.commands()).hasSize(1);
        assertThat(sequence.commands().getFirst().getVerb()).isEqualTo("kill");
    }

    @Test
    void handle_doubledConjunction_doesNotProduceAPhantomEmptyCommand() {
        // given
        Parser parser = new Parser(vocabularyWithTakeSwordAndKillOgre());

        // when
        CommandSequence sequence = parser.handle("take sword and and kill ogre");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(0).getNoun()).isEqualTo("sword");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("ogre");
    }

    @Test
    void handle_fullyUnparseableInput_stillReturnsOneEmptyCommand() {
        // given
        Parser parser = new Parser(vocabularyWithTakeSwordAndKillOgre());

        // when
        CommandSequence sequence = parser.handle("mumble grumble");

        // then
        assertThat(sequence.commands()).hasSize(1);
        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getVerb()).isEmpty();
        assertThat(command.getAdjective()).isEmpty();
        assertThat(command.getNoun()).isEmpty();
    }

    @Test
    void handle_bareNounSecondSubCommand_infersVerbFromFirstSubCommand() {
        // given
        Parser parser = new Parser(vocabularyWithTakeDropSwordAndShield());

        // when
        CommandSequence sequence = parser.handle("take sword and shield");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(0).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(0).getNoun()).isEqualTo("sword");
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("shield");
    }

    @Test
    void handle_explicitVerbInSecondSubCommand_isNotOverriddenByInference() {
        // given
        Parser parser = new Parser(vocabularyWithTakeDropSwordAndShield());

        // when
        CommandSequence sequence = parser.handle("take sword and drop shield");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(0).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("drop");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("shield");
    }

    @Test
    void handle_it_withNoAntecedent_throwsUnresolvedReferenceException() {
        // given
        Parser parser = new Parser(vocabularyWithBackReferenceWords());

        // when / then
        assertThatThrownBy(() -> parser.handle("wear it"))
                .isInstanceOf(UnresolvedReferenceException.class)
                .hasMessage("I don't know what 'it' refers to.");
    }

    @Test
    void handle_it_resolvesToTheLastMentionedNoun_matchingTheWorkedExample() {
        // given
        Parser parser = new Parser(vocabularyWithBackReferenceWords());

        // when
        CommandSequence sequence = parser.handle("take sword and shield and wear it");

        // then
        assertThat(sequence.commands()).hasSize(3);
        assertThat(sequence.commands().get(0).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(0).getNoun()).isEqualTo("sword");
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("shield");
        assertThat(sequence.commands().get(2).getVerb()).isEqualTo("wear");
        assertThat(sequence.commands().get(2).getNoun()).isEqualTo("shield");
    }

    @Test
    void handle_it_carriesTheAdjectiveOfTheLastMentionedNoun() {
        // given
        Parser parser = new Parser(vocabularyWithBackReferenceWords());

        // when
        CommandSequence sequence = parser.handle("take golden sword and wear it");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("wear");
        assertThat(sequence.commands().get(1).getAdjective()).isEqualTo("golden");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("sword");
    }

    @Test
    void handle_it_resolvesAcrossSeparateHandleCalls() {
        // given
        Parser parser = new Parser(vocabularyWithBackReferenceWords());
        parser.handle("take sword");

        // when
        CommandSequence sequence = parser.handle("wear it");

        // then
        assertThat(sequence.commands()).hasSize(1);
        assertThat(sequence.commands().getFirst().getVerb()).isEqualTo("wear");
        assertThat(sequence.commands().getFirst().getNoun()).isEqualTo("sword");
    }

    private static Vocabulary vocabularyWithTakeSwordAndKillOgre() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("take", Word.Type.VERB);
        vocabulary.createNewWord("sword", Word.Type.NOUN);
        vocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        vocabulary.createSynonym("then", "and");
        vocabulary.createNewWord("kill", Word.Type.VERB);
        vocabulary.createNewWord("ogre", Word.Type.NOUN);
        return vocabulary;
    }

    private static Vocabulary vocabularyWithTakeDropSwordAndShield() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("take", Word.Type.VERB);
        vocabulary.createNewWord("drop", Word.Type.VERB);
        vocabulary.createNewWord("sword", Word.Type.NOUN);
        vocabulary.createNewWord("shield", Word.Type.NOUN);
        vocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        return vocabulary;
    }

    private static Vocabulary vocabularyWithBackReferenceWords() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("take", Word.Type.VERB);
        vocabulary.createNewWord("wear", Word.Type.VERB);
        vocabulary.createNewWord("golden", Word.Type.ADJECTIVE);
        vocabulary.createNewWord("sword", Word.Type.NOUN);
        vocabulary.createNewWord("shield", Word.Type.NOUN);
        vocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        vocabulary.createNewWord("it", Word.Type.PRONOUN);
        return vocabulary;
    }
}
