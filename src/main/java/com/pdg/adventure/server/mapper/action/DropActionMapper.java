package com.pdg.adventure.server.mapper.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.DropAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "DropAction mapping with dynamic action resolution")
public class DropActionMapper extends ActionMapper<DropActionData, DropAction> {

    private final AdventureConfig adventureConfig;

    @Autowired
    public DropActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        this.adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(DropActionData.class, DropAction.class, this);
    }


    @Override
    public DropAction mapToBO(final DropActionData aDropActionData) {
        // Use lazy evaluation - defer getting current location until action execution
        DropAction action = new DropAction(
                adventureConfig.allItems().get(aDropActionData.getThingId()),
                new ContainerSupplier(() -> Environment.getCurrentLocation().getItemContainer()),
                adventureConfig.allMessages());
        return action;
    }

    @Override
    public DropActionData mapToDO(final DropAction from) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
