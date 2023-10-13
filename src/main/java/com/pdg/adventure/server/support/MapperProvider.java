package com.pdg.adventure.server.support;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.mapper.DirectionMapper;
import com.pdg.adventure.server.mapper.ItemContainerMapper;
import com.pdg.adventure.server.mapper.ItemMapper;
import com.pdg.adventure.server.mapper.LocationMapper;
import com.pdg.adventure.server.tangible.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public final class MapperProvider {

    private final Map<Class<? extends Mapper>, Mapper> mapperMap;

    @Autowired
    public MapperProvider(Map<String, Item> aBagOfItems) {
        mapperMap = new HashMap<>();
        mapperMap.put(LocationMapper.class, new LocationMapper(this));
        mapperMap.put(DirectionMapper.class, new DirectionMapper(this));
        mapperMap.put(ItemContainerMapper.class, new ItemContainerMapper(this, aBagOfItems));
        mapperMap.put(ItemMapper.class, new ItemMapper(this));
    }

    public <T extends Mapper> T getMapper(Class<T> aMapperName) {
        return (T) mapperMap.get(aMapperName);
    }

    public <BO, DO> Mapper<BO, DO> registerMapper(Class<? extends Mapper<DO, BO>> aMapperClass,
                                                  Mapper<DO, BO> aMapper) {
        return mapperMap.put(aMapperClass, aMapper);
    }
}
