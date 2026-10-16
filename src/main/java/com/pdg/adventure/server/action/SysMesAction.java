package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

/**
 * This shows one of the engine's system messages, e.g. 15 "OK.", in the adventure's own wording of it
 * (see "Manage System Messages"). Like {@link MesAction} it adds no line break, so the next text of the same command
 * continues on the same line. The authors see it as "SystemMsg".
 * <p>
 * The wording is looked up when the action runs, so it follows the overrides bound to the running turn.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SysMesAction extends AbstractAction {

    private final SystemMessageKey messageKey;

    public SysMesAction(SystemMessageKey aMessageKey) {
        messageKey = aMessageKey;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(messageKey.defaultText());
        return result;
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }

    // a SYSMESS prints without a line break (NEWLINE does that); this follows the behaviour of Msg.
    @Override
    public boolean continuesLine() {
        return true;
    }
}
