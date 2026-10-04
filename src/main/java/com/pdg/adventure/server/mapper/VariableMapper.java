package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.VariableData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * Maps an authored {@link VariableData} to the runtime {@link Variable} (starting at its initial
 * value) and back. {@code AdventureMapper} uses it to fill the shared {@code VariableProvider}
 * whenever an adventure is loaded.
 */
@Service
@AutoRegisterMapper(priority = 10, description = "Variable mapping")
public class VariableMapper implements Mapper<VariableData, VariableProvider> {

    @Override
    public VariableProvider mapToBO(VariableData from) {
        return new VariableProvider(from.getVariables());
    }

    @Override
    public VariableData mapToDO(VariableProvider from) {
        return new VariableData(from.getAllAsMap());
    }
}
