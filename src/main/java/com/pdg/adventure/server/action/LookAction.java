package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;

/**
 * Describes the location the player is in, in full whatever the visit count, and shows its picture. The adventure's
 * Arrival Processes fire afterwards, so an explicit look re-triggers them like walking in does. For the description
 * of one fixed item or location use {@link DescribeAction}; for the item the player named use {@link ExamineAction}.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LookAction extends AbstractAction {

    private final transient GameContext gameContext;

    public LookAction(GameContext aGameContext) {
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        Location.LocationDescription description = gameContext.getCurrentLocation().getLookDescription();
        gameContext.setCurrentPictureId(description.pictureId());

        String arrivals = gameContext.runArrivalProcesses().getResultMessage();
        String message = VocabularyData.EMPTY_STRING.equals(arrivals)
                         ? description.text()
                         : description.text() + "\n" + arrivals;

        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(message);
        return result;
    }
}
