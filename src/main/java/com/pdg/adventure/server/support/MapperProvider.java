package com.pdg.adventure.server.support;

import java.util.HashMap;
import java.util.Map;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.*;
import com.pdg.adventure.server.mapper.action.ActionMapper;
import com.pdg.adventure.server.parser.CommandProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

//@Service
final class MapperProvider {

    private final Map<Class<?>, Mapper<?, ?>> mapperMap;

    private MapperProvider(MapperSupporter aMapperSupporter) {
        mapperMap = new HashMap<>();
        registerMapper(VocabularyData.class, Vocabulary.class, new VocabularyMapper(aMapperSupporter));
        registerMapper(LocationData.class, Location.class, new LocationMapper(aMapperSupporter));
        registerMapper(DirectionData.class, Direction.class, new DirectionMapper(aMapperSupporter));
        registerMapper(ItemContainerData.class, GenericContainer.class, new ItemContainerMapper(aMapperSupporter));
        registerMapper(ItemData.class, Item.class, new ItemMapper(aMapperSupporter));
        registerMapper(DescriptionData.class, DescriptionProvider.class, new DescriptionMapper(aMapperSupporter));
        registerMapper(CommandProviderData.class, CommandProvider.class, new CommandProviderMapper(aMapperSupporter));
        registerMapper(CommandData.class, Command.class, new CommandMapper(aMapperSupporter));
        registerMapper(CommandChainData.class, CommandChain.class, new CommandChainMapper(aMapperSupporter));
        registerMapper(CommandDescriptionData.class, CommandDescription.class, new CommandDescriptionMapper(aMapperSupporter));
        registerMapper(ActionData.class, Action.class, new ActionMapper(aMapperSupporter));
//        registerMapper(SetVariableActionData.class, SetVariableAction.class, new SetVariableActionMapper(aMapperSupporter));
    }

    /**
     * This method returns a mapper for the given DTO class.
     *
     * @param aDataObjectMapperName The class of the data object.
     * @param <BO> The type of the business object.
     * @param <DO> The type of the data object.
     * @return The mapper for the given DTO class.
     */
    public <BO, DO> Mapper<BO, DO> getMapper(Class<?> aDataObjectMapperName) {
        return (Mapper<BO, DO>) mapperMap.get(aDataObjectMapperName);
    }

    /**
     * This method registers a mapper with the provider.
     *
     * @param aDOClass The class of the data object.
     * @param aBOClass The class of the business object.
     * @param aMapper The mapper to register.
     * @param <BO> The type of the business object.
     * @param <DO> The type of the data object.
     */
    public <BO, DO> void registerMapper(Class<?> aDOClass, Class<?> aBOClass, Mapper<DO, BO> aMapper) {
        mapperMap.put(aBOClass, aMapper);
        mapperMap.put(aDOClass, aMapper);
    }
}
