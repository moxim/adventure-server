package com.pdg.adventure.server.mapper.condition;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "CarriedCondition mapping with dynamic action resolution")
public class CarriedConditionMapper extends PreConditionMapper<CarriedConditionData, CarriedCondition> {

    private final AdventureConfig adventureConfig;
    private final GameContext gameContext;

    public CarriedConditionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig,
                                  GameContext aGameContext) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        gameContext = aGameContext;
        aMapperSupporter.registerMapper(CarriedConditionData.class, CarriedCondition.class, this);
    }

    @Override
    public CarriedCondition mapToBO(CarriedConditionData aCarriedConditionData) {
        final Item item = adventureConfig.allItems().get(aCarriedConditionData.getItemId());
        CarriedCondition result = new CarriedCondition(item, gameContext);
        result.setId(aCarriedConditionData.getId());
        return result;
    }

    @Override
    public CarriedConditionData mapToDO(CarriedCondition aCarriedCondition) {
        CarriedConditionData result = new CarriedConditionData();
        result.setItemId(aCarriedCondition.getItem().getId());
        result.setId(aCarriedCondition.getId());
        return result;
    }
}
