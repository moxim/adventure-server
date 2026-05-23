package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.DescribeAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.exception.ItemNotFoundException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 50, description = "Describe action mapper")
public class DescribeActionMapper extends ActionMapper<DescribeActionData, DescribeAction> {

    private final AdventureConfig adventureConfig;

    public DescribeActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(DescribeActionData.class, DescribeAction.class, this);
    }

    @Override
    public DescribeAction mapToBO(DescribeActionData actionData) {
        String targetId = actionData.getTargetId();
        Supplier<String> target = () -> describe(targetId);
        return new DescribeAction(target, adventureConfig.allMessages());
    }

    @Override
    public DescribeActionData mapToDO(DescribeAction action) {
        DescribeActionData actionData = new DescribeActionData();
        actionData.setId(action.getId());
        actionData.setTargetId(null); // TODO: Not possible to get targetId from DescribeAction, as it only has a Supplier<String> for the description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private String describe(String targetId) {
        Item item = adventureConfig.allItems().get(targetId);
        if (item != null) {
            return item.getLongDescription();
        }
        Location location = adventureConfig.allLocations().get(targetId);
        if (location != null) {
            return location.getLongDescription();
        }
        throw new ItemNotFoundException(targetId);
    }
}
