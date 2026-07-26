package com.pdg.adventure.server.mapper.action;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.CreateAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 50, description = "Create action mapper")
public class CreateActionMapper extends ActionMapper<CreateActionData, CreateAction> {

    private final AdventureConfig adventureConfig;

    public CreateActionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
    }

    @Override
    public CreateAction mapToBO(CreateActionData actionData) {
        return new CreateAction(
                getMapperSupporter().requireMappedItem(actionData.getThingId(), actionData),
                new ContainerSupplier(resolveContainer(actionData.getContainerProviderId())),
                adventureConfig.allMessages());
    }

    @Override
    public CreateActionData mapToDO(CreateAction action) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private Container resolveContainer(String containerProviderId) {
        Container container = adventureConfig.allContainers().get(containerProviderId);
        if (container != null) {
            return container;
        }
        Location location = adventureConfig.allLocations().get(containerProviderId);
        if (location != null) {
            return location.getItemContainer();
        }
        throw new IllegalStateException("Cannot resolve containerProviderId: " + containerProviderId);
    }
}
