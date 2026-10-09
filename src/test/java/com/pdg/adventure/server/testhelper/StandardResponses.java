package com.pdg.adventure.server.testhelper;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.ExamineAction;
import com.pdg.adventure.server.action.InventoryAction;
import com.pdg.adventure.server.action.LookAction;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.QuitAction;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * The Responses an author would put in the response table to get a playable adventure: help, inventory, quit and
 * the look/describe family. The engine plants none of them itself.
 */
public final class StandardResponses {

    public static final String HELP_TEXT = "Look around, examine items, take or drop items, maybe wear items, "
                                           + "enter or leave locations.\nOr quit.";

    private StandardResponses() {
    }

    /** The words the starter vocabulary an author builds on has for those Responses; the engine seeds none. */
    public static void registerWords(Vocabulary aVocabulary) {
        aVocabulary.createNewWord("quit", Word.Type.VERB);
        aVocabulary.createSynonym("exit", "quit");
        aVocabulary.createNewWord("describe", Word.Type.VERB);
        aVocabulary.createSynonym("look", "describe");
        aVocabulary.createSynonym("l", "describe");
        aVocabulary.createSynonym("examine", "describe");
        aVocabulary.createSynonym("x", "describe");
        aVocabulary.createNewWord("help", Word.Type.VERB);
        aVocabulary.createNewWord("inventory", Word.Type.VERB);
        aVocabulary.createSynonym("i", "inventory");
        aVocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        aVocabulary.createSynonym("then", "and");
        aVocabulary.createNewWord("it", Word.Type.PRONOUN);
    }

    public static void register(Workflow aWorkflow, GameContext aGameContext) {
        add(aWorkflow, new GenericCommandDescription("help"), new MessageAction(HELP_TEXT));
        add(aWorkflow, new GenericCommandDescription("inventory"),
            new InventoryAction(aGameContext::tell, new ContainerSupplier(aGameContext::getPocket)));
        add(aWorkflow, new GenericCommandDescription("quit"), new QuitAction());
        add(aWorkflow, new GenericCommandDescription("describe"), new LookAction(aGameContext));
        add(aWorkflow, new GenericCommandDescription("describe", "here"), new LookAction(aGameContext));
        add(aWorkflow, new GenericCommandDescription("describe", VocabularyData.EMPTY_STRING,
                                                     VocabularyData.WILDCARD_NOUN),
            new ExamineAction(aGameContext));
    }

    private static void add(Workflow aWorkflow, GenericCommandDescription aDescription,
                            com.pdg.adventure.api.Action anAction) {
        aWorkflow.addResponse(aDescription, new GenericCommand(aDescription, anAction));
    }
}
