package com.pdg.adventure.server.mapper.condition;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Base class for precondition mappers. This class should NOT be instantiated by Spring.
 * Only concrete subclasses should be annotated with @Service and @AutoRegisterMapper.
 */
public abstract class PreConditionMapper <PD, P> implements Mapper<PD, P> {

    private final MapperSupporter mapperSupporter;

    protected PreConditionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    protected MapperSupporter getMapperSupporter() {
        return mapperSupporter;
    }
}
