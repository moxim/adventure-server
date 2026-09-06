package com.pdg.adventure;

import java.util.Collection;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.action.DescribeAction;
import com.pdg.adventure.server.action.InventoryAction;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.QuitAction;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Thing;

public class CommandFactory {
    private final MessagesHolder allMessages;
    private final GameContext gameContext;
    private final VocabularyData vocabulary;

    public CommandFactory(MessagesHolder aMessagesHolder, GameContext aGameContext, VocabularyData aVocabulary) {
        allMessages = aMessagesHolder;
        gameContext = aGameContext;
        vocabulary = aVocabulary;
    }

    public void applyExamineFallback(Collection<? extends Thing> things) {
        if (vocabulary.getExamineWord() == null) return;
        String verb = vocabulary.getExamineWord().getText();
        if (verb.isBlank()) return;
        for (Thing thing : things) {
            thing.setExamineFallback(verb, thing::getLongDescription);
        }
    }

    public void setUpWorkflowCommands(final Workflow aWorkflow) {
        GenericCommandDescription helpCommandDescription = new GenericCommandDescription("help");
        GenericCommand helpCommand = new GenericCommand(helpCommandDescription, new MessageAction("""
                Look around, examine items, take or drop items, maybe wear items, enter or leave locations.
                Or quit.""",
            allMessages));
        aWorkflow.addInterceptorCommand(helpCommandDescription, helpCommand);

        GenericCommandDescription inventoryCommandDescription = new GenericCommandDescription("inventory");
        GenericCommand inventoryCommand = new GenericCommand(inventoryCommandDescription,
                                                             new InventoryAction(gameContext::tell,
                                                                                 new ContainerSupplier(
                                                                                         gameContext::getPocket),
                                                                                 allMessages));
        aWorkflow.addInterceptorCommand(inventoryCommandDescription, inventoryCommand);

        GenericCommandDescription quitCommandDescription = new GenericCommandDescription("quit");
        GenericCommand quitCommand = new GenericCommand(quitCommandDescription, new QuitAction(allMessages));
        aWorkflow.addInterceptorCommand(quitCommandDescription, quitCommand);

        Action lookLocationAction = new DescribeAction(() -> {
            long timesVisited = 0;
            gameContext.getCurrentLocation().setTimesVisited(timesVisited);
            String result = gameContext.getCurrentLocation().getLongDescription();
            gameContext.getCurrentLocation().setTimesVisited(timesVisited++);
            return result;
        }, allMessages);
        GenericCommandDescription lookCommandDescription = new GenericCommandDescription("describe");
        GenericCommand lookCommand = new GenericCommand(lookCommandDescription, lookLocationAction);
        aWorkflow.addInterceptorCommand(lookCommandDescription, lookCommand);

        GenericCommandDescription lookCommandDescription2 = new GenericCommandDescription("describe", "here");
        GenericCommand lookCommand2 = new GenericCommand(lookCommandDescription2, lookLocationAction);
        aWorkflow.addInterceptorCommand(lookCommandDescription2, lookCommand2);

        GenericCommandDescription anyCommandDescription = new GenericCommandDescription("~", "~", "~");
        GenericCommand anyCommand = new GenericCommand(anyCommandDescription,
                                                       new MessageAction(SystemMessageKey.SM2.defaultText(), allMessages));
        aWorkflow.addPreCommand(anyCommandDescription, anyCommand);
    }
}
