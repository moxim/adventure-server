package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LightAction extends AbstractAction {

    private final Item item;
    private final Integer lumen;

    public LightAction(Item anItem, Integer aLumen) {
        item = anItem;
        lumen = aLumen;
    }

    @Override
    public ExecutionResult execute() {
        // TODO: Review needed — "alter the lumen attribute" is ambiguous between setting it
        // absolutely and incrementing it by the given amount. Implemented as an absolute set,
        // matching the "LIGHT torch 50" example (reads as "set lumen to 50", not "by 50").
        item.setLight(lumen);
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(SystemMessageKey.SM66.defaultText()
                                                     .formatted(item.getStrippedBasicDescription(), lumen));
        return result;
    }
}
