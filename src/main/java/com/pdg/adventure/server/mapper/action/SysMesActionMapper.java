package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.SysMesActionData;
import com.pdg.adventure.server.action.SysMesAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "SysMes action mapper")
public class SysMesActionMapper extends ActionMapper<SysMesActionData, SysMesAction> {

    public SysMesActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public SysMesAction mapToBO(SysMesActionData from) {
        SystemMessageKey key = SystemMessageKey.fromId(from.getMessageKeyId())
                .orElseThrow(() -> new ConfigurationException(
                        "SystemMsg action refers to an unknown system message: " + from.getMessageKeyId()));
        SysMesAction action = new SysMesAction(key);
        action.setId(from.getId());
        return action;
    }

    @Override
    public SysMesActionData mapToDO(SysMesAction from) {
        SysMesActionData data = new SysMesActionData();
        data.setMessageKeyId(from.getMessageKey().id());
        data.setId(from.getId());
        return data;
    }
}
