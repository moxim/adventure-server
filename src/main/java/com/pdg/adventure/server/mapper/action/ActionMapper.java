package com.pdg.adventure.server.mapper.action;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Base class for action mappers. This class should NOT be instantiated by Spring.
 * Only concrete subclasses should be annotated with @Service and @AutoRegisterMapper.
 */
public abstract class ActionMapper<ActionData, Action> implements Mapper<ActionData, Action> {

    private final MapperSupporter mapperSupporter;

    public ActionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    protected MapperSupporter getMapperSupporter() {
        return mapperSupporter;
    }

    @Override
    public Action mapToBO(ActionData from) {
        return null;
    }

    @Override
    public ActionData mapToDO(Action from) {
        return null;
    }
}
