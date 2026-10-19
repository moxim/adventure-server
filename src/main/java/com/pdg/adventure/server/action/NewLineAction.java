package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * NEWLINE: ends the line the command's texts have reached, so the text after it starts on a new line. After a
 * line-continuing action (Msg, SystemMsg, Print) it supplies the line break they left out; after an ordinary
 * action, which has ended its line already, it leaves an empty line, as in PAW.
 * <p>
 * It is a line-continuing action whose text is a line break: {@code GenericCommand} keeps such a text and adds no
 * further separator after it. Since the text is blank, a NewLine on its own, with no other text in the command, prints
 * nothing (the run session drops blank lines).
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class NewLineAction extends AbstractAction {

    @Override
    public ExecutionResult execute() {
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS, System.lineSeparator());
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }

    @Override
    public boolean continuesLine() {
        return true;
    }
}
