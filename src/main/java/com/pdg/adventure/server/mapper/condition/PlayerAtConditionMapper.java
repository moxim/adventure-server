package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.PlayerAtCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "PlayerAtCondition mapper")
public class PlayerAtConditionMapper extends PreConditionMapper<PlayerAtConditionData, PlayerAtCondition> {

    private final GameContext gameContext;

    public PlayerAtConditionMapper(MapperSupporter aMapperSupporter, GameContext aGameContext) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public PlayerAtCondition mapToBO(PlayerAtConditionData data) {
        final Location location = getMapperSupporter().requireMappedLocation(data.getLocationId(), data);
        PlayerAtCondition result = new PlayerAtCondition(location, gameContext);
        result.setId(data.getId());
        return result;
    }

    @Override
    public PlayerAtConditionData mapToDO(PlayerAtCondition condition) {
        PlayerAtConditionData result = new PlayerAtConditionData();
        result.setLocationId(condition.getLocation().getId());
        result.setId(condition.getId());
        return result;
    }
}
