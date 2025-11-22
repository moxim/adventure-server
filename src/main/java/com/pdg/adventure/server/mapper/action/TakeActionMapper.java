package com.pdg.adventure.server.mapper.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.TakeAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "TakeAction mapping with dynamic action resolution")
public class TakeActionMapper extends ActionMapper<TakeActionData, TakeAction> {

    private final AdventureConfig adventureConfig;

    @Autowired
    public TakeActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        this.adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(TakeActionData.class, TakeAction.class, this);
    }

    @Override
    public TakeAction mapToBO(final TakeActionData aTakeActionData) {
        TakeAction action = new TakeAction(
                adventureConfig.allItems().get(aTakeActionData.getThingId()),
                new ContainerSupplier(Environment::getPocket),
                adventureConfig.allMessages());
        return action;
    }

    @Override
    public TakeActionData mapToDO(final TakeAction from) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
