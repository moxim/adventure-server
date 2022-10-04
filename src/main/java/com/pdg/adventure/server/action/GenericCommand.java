package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Action;
import com.pdg.adventure.server.api.Command;

import java.util.UUID;

public class GenericCommand implements Command {
    private final CommandDescription description;
    private final Action action;
    private final UUID id;

    public GenericCommand(CommandDescription aDescription, Action anAction) {
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
        if (!(aO instanceof GenericCommand aCommand)) return false;
        return id.equals(aCommand.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
