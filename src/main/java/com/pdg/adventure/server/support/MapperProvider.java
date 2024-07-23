package com.pdg.adventure.server.support;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.mapper.*;

import java.util.HashMap;
import java.util.Map;

//@Service
public final class MapperProvider {

    private Map<Class<? extends Mapper>, Mapper> mapperMap;

    public MapperProvider(MapperSupporter aMapperSupporter) {
        mapperMap = new HashMap<>();
        registerMapper(VocabularyMapper.class, new VocabularyMapper(aMapperSupporter));
        registerMapper(LocationMapper.class, new LocationMapper(aMapperSupporter));
        registerMapper(DirectionMapper.class, new DirectionMapper(aMapperSupporter));
        registerMapper(ItemContainerMapper.class, new ItemContainerMapper(aMapperSupporter));
        registerMapper(ItemMapper.class, new ItemMapper(aMapperSupporter));
        registerMapper(DescriptionMapper.class, new DescriptionMapper(aMapperSupporter));
        registerMapper(CommandProviderMapper.class, new CommandProviderMapper(aMapperSupporter));
        registerMapper(CommandMapper.class, new CommandMapper(aMapperSupporter));
        registerMapper(CommandChainMapper.class, new CommandChainMapper(aMapperSupporter));
        registerMapper(CommandDescriptionMapper.class, new CommandDescriptionMapper(aMapperSupporter));
    }

    public <T extends Mapper> T getMapper(Class<T> aMapperName) {
        return (T) mapperMap.get(aMapperName);
    }

    /**
     * This method registers a mapper with the provider.
     *
     * @param aMapperClass
     * @param aMapper
     * @return
     * @param <BO>
     * @param <DO>
     */
    public <BO, DO> Mapper<BO, DO> registerMapper(Class<? extends Mapper<DO, BO>> aMapperClass,
                                                  Mapper<DO, BO> aMapper) {
        return mapperMap.put(aMapperClass, aMapper);
    }
}
