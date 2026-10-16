package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.ListAtActionData;
import com.pdg.adventure.server.action.ListAtAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "ListAt action mapper")
public class ListAtActionMapper extends ActionMapper<ListAtActionData, ListAtAction> {

    public ListAtActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public ListAtAction mapToBO(ListAtActionData from) {
        ListAtAction action = new ListAtAction(
                getMapperSupporter().requireMappedContainer(from.getContainerId(), from));
        action.setId(from.getId());
        return action;
    }

    @Override
    public ListAtActionData mapToDO(ListAtAction from) {
        ListAtActionData data = new ListAtActionData();
        data.setContainerId(from.getContainer().getId());
        data.setId(from.getId());
        return data;
    }
}
