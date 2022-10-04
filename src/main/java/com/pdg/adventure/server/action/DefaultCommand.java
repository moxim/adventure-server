package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Action;
import com.pdg.adventure.server.api.Command;

import java.util.UUID;

public class DefaultCommand implements Command {
    private final CommandDescription description;
    private final Action action;
    private final UUID id;

    public DefaultCommand(CommandDescription aDescription, Action anAction) {
        description = aDescription;
        action = anAction;
        id = UUID.randomUUID();
    }

    public String getDescription() {
        return description.getDescription();
    }

    public void execute() {
        action.execute();
    }

    @Override
    public boolean equals(Object aO) {
        if (this == aO) return true;
        if (!(aO instanceof DefaultCommand aDefaultCommand)) return false;
        return id.equals(aDefaultCommand.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
