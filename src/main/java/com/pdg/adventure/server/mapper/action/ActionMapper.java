package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Base action mapper")
public class ActionMapper implements Mapper<ActionData, Action> {

    private final MapperSupporter mapperSupporter;

    public ActionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
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
