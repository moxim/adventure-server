package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.server.action.QuitAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Quit action mapper")
public class QuitActionMapper extends ActionMapper<QuitActionData, QuitAction> {

    public QuitActionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
    }

    @Override
    public QuitAction mapToBO(QuitActionData data) {
        return new QuitAction();
    }

    @Override
    public QuitActionData mapToDO(QuitAction action) {
        return new QuitActionData();
    }
}
