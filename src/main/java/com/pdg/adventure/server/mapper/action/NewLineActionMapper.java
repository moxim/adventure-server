package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.NewLineActionData;
import com.pdg.adventure.server.action.NewLineAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "NewLine action mapper")
public class NewLineActionMapper extends ActionMapper<NewLineActionData, NewLineAction> {

    public NewLineActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public NewLineAction mapToBO(NewLineActionData from) {
        NewLineAction action = new NewLineAction();
        action.setId(from.getId());
        return action;
    }

    @Override
    public NewLineActionData mapToDO(NewLineAction from) {
        NewLineActionData data = new NewLineActionData();
        data.setId(from.getId());
        return data;
    }
}
