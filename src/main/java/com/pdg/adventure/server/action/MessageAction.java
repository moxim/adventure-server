package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.server.engine.FontMarkup;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Shows a message's text, in the message's own font if it has one (see {@link FontMarkup}).
 * <p>
 * TODO: Review needed — only authored messages get a font. Location and item descriptions, the built-in system
 *  messages and other engine text are not wrapped and use the adventure's Run Font.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MessageAction extends AbstractAction {

    private final String message;
    private final AdventureFont font;

    public MessageAction(String aMessage) {
        this(aMessage, AdventureFont.DEFAULT);
    }

    /**
     * @param aFont the message's own font; {@code DEFAULT} (or null) means no override, the text is passed on
     *              exactly as it is
     */
    public MessageAction(String aMessage, AdventureFont aFont) {
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
}
