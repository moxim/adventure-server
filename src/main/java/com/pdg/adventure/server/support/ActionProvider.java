package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Action;

import java.util.*;

public class ActionProvider {
    private final Map<String, Action> availableActions;

    public ActionProvider() {
        availableActions = new HashMap<>();
    }

    public void addAction(Action anAction) {
        availableActions.put(anAction.getName(), anAction);
    }

    public void removeAction(Action anAction) {
        availableActions.remove(anAction.getName());
    }

    public boolean hasAction(String anActionString) {
        return availableActions.containsKey(anActionString);
    }

    public List<Action> getActions() {
        return new ArrayList<>(availableActions.values());
    }
}
