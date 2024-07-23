package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.action.ActionData;

public class ActionMapper implements Mapper<ActionData, Action> {

    @Override
    public Action mapToBO(ActionData from) {
        return null;
    }

    @Override
    public ActionData mapToDO(Action from) {
        return null;
    }
}
