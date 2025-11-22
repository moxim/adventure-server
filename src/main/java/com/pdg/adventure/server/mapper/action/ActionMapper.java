package com.pdg.adventure.server.mapper.action;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Base class for action mappers. This class should NOT be instantiated by Spring.
 * Only concrete subclasses should be annotated with @Service and @AutoRegisterMapper.
 */
public abstract class ActionMapper<AD, A> implements Mapper<AD, A> {

    private final MapperSupporter mapperSupporter;

    protected ActionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    protected MapperSupporter getMapperSupporter() {
        return mapperSupporter;
    }
}
