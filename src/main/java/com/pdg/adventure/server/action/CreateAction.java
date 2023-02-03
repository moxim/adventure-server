package com.pdg.adventure.server.action;

import java.util.function.Supplier;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.messages.MessagesHolder;

public class CreateAction extends AbstractAction {

    private final Containable thing;
    private Supplier<Container> containerProvider;

    public CreateAction(Containable aThing, Supplier<Container> aContainerProvider, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        thing = aThing;
        containerProvider = aContainerProvider;
    }

    @Override
    public ExecutionResult execute() {
        Container container = containerProvider.get();
        ExecutionResult result = container.add(thing);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            // TODO
            //  really do this?
            result.setResultMessage(String.format(messagesHolder.getMessage("-12"), thing.getShortDescription(),
                                                  container.getShortDescription()));
        }
        return result;
    }
}
