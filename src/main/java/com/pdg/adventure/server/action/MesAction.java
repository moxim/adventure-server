package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.server.engine.FontMarkup;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * PAW's MES: shows a message's text like {@link MessageAction} (MESSAGE), but without a line break after it, so the
 * text of the next action in the same command continues on the same line. The authors see it as "Msg".
 * <p>
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MesAction extends AbstractAction {

    private final String message;
    private final AdventureFont font;

    public MesAction(String aMessage) {
        this(aMessage, AdventureFont.DEFAULT);
    }

    /**
     * @param aFont the message's own font; {@code DEFAULT} (or null) means no override, the text is passed on
     *              exactly as it is
     */
    public MesAction(String aMessage, AdventureFont aFont) {
        message = aMessage;
        font = aFont == null ? AdventureFont.DEFAULT : aFont;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(FontMarkup.wrap(message, font));
        return result;
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
