package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.VariableData;
import com.pdg.adventure.server.support.VariableProvider;

class VariableMapperTest {

    private final VariableMapper mapper = new VariableMapper();

    @Test
    void mapToBO_carriesNameAndInitialValue() {
        VariableData data = new VariableData();
        data.setVariables(Map.of("lives", 3));
        VariableProvider variable = mapper.mapToBO(data);

        assertThat(variable.get("lives").get().name()).isEqualTo("lives");
        assertThat(variable.get("lives").get().value()).isEqualTo(3);
    }

    @Test
    void mapToDO_roundTrips() {
        VariableProvider provider = new VariableProvider();
        provider.set("score", 9);
        VariableData data = mapper.mapToDO(provider);

        data.getVariables().forEach((name, value) -> {
            assertThat(name).isEqualTo("score");
            assertThat(value).isEqualTo(9);
        });
    }
}
