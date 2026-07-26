package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "CarriedCondition mapping with dynamic action resolution")
public class CarriedConditionMapper extends PreConditionMapper<CarriedConditionData, CarriedCondition> {

    private final GameContext gameContext;

    public CarriedConditionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public CarriedCondition mapToBO(CarriedConditionData aCarriedConditionData) {
        final Item item = getMapperSupporter().requireMappedItem(aCarriedConditionData.getItemId(),
                                                                 aCarriedConditionData);
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
