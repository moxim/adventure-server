package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.ThingData;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Thing;

public class ThingMapper implements Mapper<ThingData, Thing> {

    private MapperSupporter mapperSupporter;

    public ThingMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    public Thing mapToBO(ThingData aThingData) {
        final DescriptionMapper descriptionMapper = mapperSupporter.getMapper(DescriptionMapper.class);
        final CommandProviderMapper commandProviderMapper = mapperSupporter.getMapper(CommandProviderMapper.class);
        Thing result = new Thing(descriptionMapper.mapToBO(aThingData.getDescriptionData()));
        result.setId(aThingData.getId());
        result.setCommandProvider(commandProviderMapper.mapToBO(aThingData.getCommandProviderData()));
        return result;
    }

    public ThingData mapToDO(Thing aThing) {
        ThingData result = new ThingData();
        result.setId(aThing.getId());
        DescriptionMapper descriptionMapper = mapperSupporter.getMapper(DescriptionMapper.class);
        final CommandProviderMapper commandProviderMapper = mapperSupporter.getMapper(CommandProviderMapper.class);
        result.setDescriptionData(descriptionMapper.mapToDO(aThing.getDescriptionProvider()));
        result.setCommandProviderData(commandProviderMapper.mapToDO(aThing.getCommandProvider()));
        return result;
    }
}
