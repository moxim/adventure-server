package com.pdg.adventure;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.Actionable;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.server.action.*;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.condition.HereCondition;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.tangible.Item;

public class CommandFactory {
    private final MessagesHolder allMessages;
    private final GameContext gameContext;

    public CommandFactory(MessagesHolder allMessages, GameContext gameContext) {
        this.allMessages = allMessages;
        this.gameContext = gameContext;
    }

    public void setUpLookCommands(Actionable aThing) {
        aThing.addCommand(new GenericCommand(new GenericCommandDescription("describe", aThing),
                                             new DescribeAction(aThing::getLongDescription, allMessages)));
    }

    public void setUpWearCommands(Item anItem) {
        anItem.setIsWearable(true);
        GenericCommand wear = new GenericCommand(new GenericCommandDescription("wear", anItem),
                                                 new WearAction(anItem, allMessages));
        PreCondition carriedCondition = new CarriedCondition(anItem, gameContext);
        wear.addPreCondition(carriedCondition);
        anItem.addCommand(wear);
        GenericCommand remove = new GenericCommand(new GenericCommandDescription("remove", anItem),
                                                   new RemoveAction(anItem, allMessages));
        remove.addPreCondition(carriedCondition);
        anItem.addCommand(remove);
    }

    public void setUpTakeCommands(Item anItem) {
        GenericCommandDescription getCommandDescription = new GenericCommandDescription("get", anItem);
        GenericCommand takeFailCommand = new GenericCommand(getCommandDescription, new MessageAction(
                allMessages.getMessage("-13").formatted(anItem.getEnrichedBasicDescription()), allMessages));
        takeFailCommand.addPreCondition(new CarriedCondition(anItem, gameContext));
        anItem.addCommand(takeFailCommand);

        GenericCommand takeCommand = new GenericCommand(getCommandDescription, new TakeAction(anItem,
                                                                                              new ContainerSupplier(
                                                                                                      gameContext::getPocket),
                                                                                              allMessages));
        takeCommand.addPreCondition(new NotCondition(new CarriedCondition(anItem, gameContext)));
        takeCommand.addPreCondition(new HereCondition(anItem, gameContext));
        anItem.addCommand(takeCommand);

        GenericCommandDescription dropCommandDescription = new GenericCommandDescription("drop", anItem);
        GenericCommand dropAndRemoveCommand = new GenericCommand(dropCommandDescription, new DropAction(anItem,
                                                                                                        new ContainerSupplier(
                                                                                                                () -> gameContext.getCurrentLocation()
                                                                                                                                 .getItemContainer()),
                                                                                                        allMessages));
        PreCondition wornCondition = new WornCondition(anItem);
        dropAndRemoveCommand.addPreCondition(wornCondition);
        dropAndRemoveCommand.addFollowUpAction(new RemoveAction(anItem, allMessages));
        anItem.addCommand(dropAndRemoveCommand);

        GenericCommand dropCommand = new GenericCommand(dropCommandDescription, new DropAction(anItem,
                                                                                               new ContainerSupplier(
                                                                                                       () -> gameContext.getCurrentLocation()
                                                                                                                        .getItemContainer()),
                                                                                               allMessages));
        dropCommand.addPreCondition(new NotCondition(wornCondition));
        dropCommand.addPreCondition(new CarriedCondition(anItem, gameContext));
        anItem.addCommand(dropCommand);
    }

    public void setUpTakeCommands(Item anItem, MessageAction aMessageAction) {
        GenericCommandDescription getCommandDescription = new GenericCommandDescription("get", anItem);
        GenericCommand takeFailCommand = new GenericCommand(getCommandDescription, aMessageAction);
        anItem.addCommand(takeFailCommand);
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

        GenericCommandDescription anyCommandDescription = new GenericCommandDescription("}", "}", "}");
        GenericCommand anyCommand = new GenericCommand(anyCommandDescription,
                                                       new MessageAction("What now? > ", allMessages));
        aWorkflow.addPreCommand(anyCommandDescription, anyCommand);
    }
}
