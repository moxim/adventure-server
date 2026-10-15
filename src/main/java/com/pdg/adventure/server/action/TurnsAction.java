package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * PAW's TURNS: shows the player how many turns they have taken, "You have taken 1 turn." / "You have taken 5
 * turns.". The number is the {@code TURNS} variable; the wording is SM17 (before the number), SM18 ("turn"), SM19
 * (the "s" added unless the number is exactly 1) and SM20 (after it), so an adventure can reword all of it.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class TurnsAction extends AbstractAction {

    private final transient VariableProvider variableProvider;

    public TurnsAction(VariableProvider aVariableProvider) {
        variableProvider = aVariableProvider;
    }

    @Override
    public ExecutionResult execute() {
        int turns = variableProvider.get(VariableProvider.TURNS_VARIABLE_NAME).map(Variable::value).orElse(0);
        String plural = turns == 1 ? "" : SystemMessageKey.SM19.defaultText();
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS,
                SystemMessageKey.SM17.defaultText() + turns + SystemMessageKey.SM18.defaultText()
                + plural + SystemMessageKey.SM20.defaultText());
    }
}
