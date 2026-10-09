package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Optional;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * Shows the long description of the item the player named with the noun of the current sub-command, looking first in
 * the pocket and then at the current location. Like the AutoTake/AutoDrop actions it is not bound to one item.
 * <p>
 * Outcomes: the item's long description; or, when no noun was given or no item of that name is carried or here
 * (the parser drops words it doesn't know), {@link SystemMessageKey#SM26}.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ExamineAction extends AbstractNounItemAction {

    public ExamineAction(GameContext aGameContext) {
        super(aGameContext, Map.of());
    }

    @Override
    public ExecutionResult execute() {
        if (!hasNoun()) {
            return failure(SystemMessageKey.SM26.defaultText());
        }
        Optional<Item> item = findIn(pocket()).or(() -> findIn(here()));
        if (item.isEmpty()) {
            return failure(SystemMessageKey.SM26.defaultText());
        }
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(item.get().getLongDescription());
        return result;
    }
}
