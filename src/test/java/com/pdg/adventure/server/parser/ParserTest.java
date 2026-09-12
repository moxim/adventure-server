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
    void handle_verbInference_persistsAcrossSeparateHandleCalls() {
        // given
        Parser parser = new Parser(vocabularyWithTakeDropSwordAndShield());
        parser.handle("take sword");

        // when
        CommandSequence sequence = parser.handle("shield");

        // then
        assertThat(sequence.commands()).hasSize(1);
        assertThat(sequence.commands().getFirst().getVerb()).isEqualTo("take");
        assertThat(sequence.commands().getFirst().getNoun()).isEqualTo("shield");
    }

    @Test
    void handle_adverbBeforeVerb_isCapturedWithoutDisturbingVerbAndNoun() {
        // "SLOWLY OPEN CHEST"
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("slowly", Word.Type.ADVERB);
        vocabulary.createNewWord("open", Word.Type.VERB);
        vocabulary.createNewWord("chest", Word.Type.NOUN);
        Parser parser = new Parser(vocabulary);

        CommandSequence sequence = parser.handle("slowly open chest");

        assertThat(sequence.commands()).hasSize(1);
        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getVerb()).isEqualTo("open");
        assertThat(command.getNoun()).isEqualTo("chest");
        assertThat(command.getAdverb()).isEqualTo("slowly");
    }

    @Test
    void handle_prepositionAfterNoun_isCapturedWithoutDisturbingVerbAndNoun() {
        // "SWITCH LAMP ON"
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("switch", Word.Type.VERB);
        vocabulary.createNewWord("lamp", Word.Type.NOUN);
        vocabulary.createNewWord("on", Word.Type.PREPOSITION);
        Parser parser = new Parser(vocabulary);

        CommandSequence sequence = parser.handle("switch lamp on");

        assertThat(sequence.commands()).hasSize(1);
        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getVerb()).isEqualTo("switch");
        assertThat(command.getNoun()).isEqualTo("lamp");
        assertThat(command.getPreposition()).isEqualTo("on");
    }

    @Test
    void handle_noAdverbOrPreposition_leavesBothFieldsEmpty() {
        Parser parser = new Parser(vocabularyWithTakeSwordAndKillOgre());

        CommandSequence sequence = parser.handle("take sword");

        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getPreposition()).isEmpty();
        assertThat(command.getAdverb()).isEmpty();
    }

    @Test
    void handle_secondNounAndAdjective_fallIntoNoun2AndAdjective2() {
        // "USE SPANNER ON ANCIENT MACHINE"
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("use", Word.Type.VERB);
        vocabulary.createNewWord("spanner", Word.Type.NOUN);
        vocabulary.createNewWord("on", Word.Type.PREPOSITION);
        vocabulary.createNewWord("ancient", Word.Type.ADJECTIVE);
        vocabulary.createNewWord("machine", Word.Type.NOUN);
        Parser parser = new Parser(vocabulary);

        CommandSequence sequence = parser.handle("use spanner on ancient machine");

        assertThat(sequence.commands()).hasSize(1);
        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getVerb()).isEqualTo("use");
        assertThat(command.getNoun()).isEqualTo("spanner");
        assertThat(command.getAdjective()).isEmpty();
        assertThat(command.getPreposition()).isEqualTo("on");
        assertThat(command.getAdjective2()).isEqualTo("ancient");
        assertThat(command.getNoun2()).isEqualTo("machine");
    }

    @Test
    void handle_adjectivesOnBothNouns_eachPairsWithTheNounItPrecedes() {
        // "USE OLD SPANNER ON ANCIENT MACHINE"
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("use", Word.Type.VERB);
        vocabulary.createNewWord("old", Word.Type.ADJECTIVE);
        vocabulary.createNewWord("spanner", Word.Type.NOUN);
        vocabulary.createNewWord("on", Word.Type.PREPOSITION);
        vocabulary.createNewWord("ancient", Word.Type.ADJECTIVE);
        vocabulary.createNewWord("machine", Word.Type.NOUN);
        Parser parser = new Parser(vocabulary);

        CommandSequence sequence = parser.handle("use old spanner on ancient machine");

        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getAdjective()).isEqualTo("old");
        assertThat(command.getNoun()).isEqualTo("spanner");
        assertThat(command.getAdjective2()).isEqualTo("ancient");
        assertThat(command.getNoun2()).isEqualTo("machine");
    }

    @Test
    void handle_singleNounSentence_leavesNoun2AndAdjective2Empty_matchingPriorBehaviour() {
        // A lone adjective with no noun following still fills the PRIMARY adjective slot exactly
        // as before this feature - it must not silently disappear into an unused adjective2.
        Parser parser = new Parser(vocabularyWithBackReferenceWords());

        CommandSequence sequence = parser.handle("take golden sword");

        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getAdjective()).isEqualTo("golden");
        assertThat(command.getNoun()).isEqualTo("sword");
        assertThat(command.getAdjective2()).isEmpty();
        assertThat(command.getNoun2()).isEmpty();
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

    @Test
    void handle_pronounAfterAPrimaryNounIsAlreadyFilled_fallsIntoNoun2_ratherThanOverwritingNoun() {
        // "USE SPANNER ON IT" - the pronoun refers to the second noun slot, not the primary one.
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("take", Word.Type.VERB);
        vocabulary.createNewWord("use", Word.Type.VERB);
        vocabulary.createNewWord("spanner", Word.Type.NOUN);
        vocabulary.createNewWord("machine", Word.Type.NOUN);
        vocabulary.createNewWord("on", Word.Type.PREPOSITION);
        vocabulary.createNewWord("it", Word.Type.PRONOUN);
        Parser parser = new Parser(vocabulary);
        parser.handle("take machine");

        CommandSequence sequence = parser.handle("use spanner on it");

        GenericCommandDescription command = sequence.commands().getFirst();
        assertThat(command.getVerb()).isEqualTo("use");
        assertThat(command.getNoun()).isEqualTo("spanner");
        assertThat(command.getNoun2()).isEqualTo("machine");
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
