package com.pdg.adventure.server.mapper.action;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
public class ActionMapper implements Mapper<ActionData, Action> {

    private final MapperSupporter mapperSupporter;

    public ActionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void registerMapper() {
        mapperSupporter.registerMapper(ActionData.class, Action.class, this);
    }

    @Override
    public Action mapToBO(ActionData from) {
        return null;
    }

    @Override
    public ActionData mapToDO(Action from) {
        return null;
    }
}
