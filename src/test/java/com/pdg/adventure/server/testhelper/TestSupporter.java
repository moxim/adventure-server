package com.pdg.adventure.server.testhelper;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.parser.GenericCommandDescription;

public class TestSupporter {
    private TestSupporter() {
        // don't instantiate me
    }

    public static boolean conditionToBoolean(PreCondition aCondition) {
        final ExecutionResult executionResult = aCondition.check();
        return executionResult.getExecutionState()== ExecutionResult.State.SUCCESS;
    }

    public static boolean addItemToBoolean(Container aContainer, Containable anItem) {
        final ExecutionResult executionResult = aContainer.add(anItem);
        return executionResult.getExecutionState()== ExecutionResult.State.SUCCESS;
    }

    public static boolean removeItemToBoolean(Container aContainer, Containable anItem) {
        final ExecutionResult executionResult = aContainer.remove(anItem);
        return executionResult.getExecutionState()== ExecutionResult.State.SUCCESS;
    }

    public static boolean applyCommandToBoolean(Containable anItem, GenericCommandDescription aCommandDescription) {
        final ExecutionResult executionResult = anItem.applyCommand(aCommandDescription);
        return executionResult.getExecutionState()== ExecutionResult.State.SUCCESS;
    }


    public static CommandData createCommand(String anId, VocabularyData aVocabularyData) {
        final CommandData commandData = new CommandData();
        commandData.setId(anId);
        commandData.setCommandDescription(TestSupporter.createCommandDescriptionData(anId, aVocabularyData));
        return commandData;
    }

    public static CommandDescriptionData createCommandDescriptionData(String anId, VocabularyData aVocabularyData) {
        CommandDescriptionData result = new CommandDescriptionData();
        result.setId(anId);
        Word verb = new Word(anId + "_verb", Word.Type.VERB);
        result.setVerb(verb);
        Word adjective = new Word(anId + "_adjective", Word.Type.ADJECTIVE);
        result.setAdjective(adjective);
        Word noun = new Word(anId + "_noun", Word.Type.NOUN);
        result.setNoun(noun);

        aVocabularyData.addWord(verb);
        aVocabularyData.addWord(adjective);
        aVocabularyData.addWord(noun);

        return result;
    }
}
