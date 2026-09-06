package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.function.Supplier;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CreateAction extends AbstractAction {

    private final Containable thing;
    private final transient Supplier<Container> containerProvider;

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
            // TODO: really deliver this message?
            result.setResultMessage(SystemMessageKey.SM58.defaultText().formatted(thing.getStrippedBasicDescription(),
                                                                                  container.getStrippedBasicDescription()));
        }
        return result;
    }
}
