package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.MesActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.MesAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Mes action mapper")
public class MesActionMapper extends ActionMapper<MesActionData, MesAction> {
    private final AdventureConfig adventureConfig;

    public MesActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        this.adventureConfig = anAdventureConfig;
    }

    @Override
    public MesActionData mapToDO(MesAction action) {
        MesActionData data = new MesActionData();
        // TODO: Review needed — as in MessageActionMapper, a MesAction only holds the resolved text, so this
        //  stores the text instead of the message id; no production code maps BO -> DO.
        data.setMessageId(action.getMessage());
        return data;
    }

    @Override
    public MesAction mapToBO(MesActionData actionData) {
        String message = adventureConfig.allMessages().getMessage(actionData.getMessageId());
        if (message == null) {
            // TODO: Review needed — same literal-text fallback as MessageActionMapper.
            message = actionData.getMessageId();
        }
        return new MesAction(message, adventureConfig.allMessages().getFont(actionData.getMessageId()));
    }
}
