package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.InventoryActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.InventoryAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Inventory action mapper")
public class InventoryActionMapper extends ActionMapper<InventoryActionData, InventoryAction> {

    private final AdventureConfig adventureConfig;
    private final GameContext gameContext;

    public InventoryActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig,
                                  GameContext aGameContext) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        gameContext = aGameContext;
    }

    @Override
    public InventoryAction mapToBO(InventoryActionData actionData) {
        return new InventoryAction(
                gameContext::tell,
                new ContainerSupplier(gameContext::getPocket),
                adventureConfig.allMessages());
    }

    @Override
    public InventoryActionData mapToDO(InventoryAction action) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
