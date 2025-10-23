package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.ThingData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.parser.CommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Thing;

@Service
@AutoRegisterMapper(priority = 35, description = "Thing mapping with description and command dependencies")
public class ThingMapper implements Mapper<ThingData, Thing> {

    private Mapper<DescriptionData, DescriptionProvider> descriptionMapper;
    private Mapper<CommandProviderData, CommandProvider> commandProviderMapper;

    private final MapperSupporter mapperSupporter;
    public ThingMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void initializeDependencies() {
        descriptionMapper = mapperSupporter.getMapper(DescriptionData.class);
        commandProviderMapper = mapperSupporter.getMapper(CommandProviderData.class);
    }

    public Thing mapToBO(ThingData aThingData) {
        Thing result = new Thing(descriptionMapper.mapToBO(aThingData.getDescriptionData()));
        result.setId(aThingData.getId());
        result.setCommandProvider(commandProviderMapper.mapToBO(aThingData.getCommandProviderData()));
        return result;
    }

    public ThingData mapToDO(Thing aThing) {
        ThingData result = new ThingData();
        result.setId(aThing.getId());
        result.setDescriptionData(descriptionMapper.mapToDO(aThing.getDescriptionProvider()));
        result.setCommandProviderData(commandProviderMapper.mapToDO(aThing.getCommandProvider()));
        return result;
    }
}
