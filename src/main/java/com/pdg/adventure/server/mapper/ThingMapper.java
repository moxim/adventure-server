package com.pdg.adventure.server.mapper;

import com.pdg.adventure.model.ThingData;
import com.pdg.adventure.server.tangible.Thing;

public abstract class ThingMapper {
    private ThingMapper() {
        // don't instantiate me
    }

    public static Thing mapFrom(ThingData aThingData) {
        Thing result = new Thing(DescriptionMapper.map(aThingData.getDescriptionData()));
        result.setId(aThingData.getId());
        result.setCommandProvider(CommandProviderMapper.map(aThingData.getCommandProviderData()));
        return result;
    }

    public static ThingData mapFrom(Thing aThing) {
        ThingData result = new ThingData();
        result.setId(aThing.getId());
        result.setDescriptionData(DescriptionMapper.map(aThing.getDescriptionProvider()));
        result.setCommandProviderData(CommandProviderMapper.map(aThing.getCommandProvider()));
        return result;
    }
}
