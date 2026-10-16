package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

/**
 * LISTAT: prints the items in one container, a location's or the player's pocket, in the list form used for a
 * location's items. Unlike INVENTORY it adds no heading such as "I have with me:"; the author puts one in front with
 * a Message or SystemMsg action if wanted. A container without items gives system message 53 ("nothing.").
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ListAtAction extends AbstractAction {

    private final Container container;

    public ListAtAction(Container aContainer) {
        container = aContainer;
    }

    @Override
    public ExecutionResult execute() {
        String text = container.isEmpty() ? SystemMessageKey.SM53.defaultText() : container.listContents();
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS, text);
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }
}
