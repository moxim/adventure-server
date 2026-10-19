package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.NewLineActionData;
import com.pdg.adventure.server.action.NewLineAction;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class NewLineActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;

    @Test
    void mapToBO_keepsTheId() {
        NewLineActionData data = new NewLineActionData();
        data.setId("id");

        assertThat(new NewLineActionMapper(mapperSupporter).mapToBO(data).getId()).isEqualTo("id");
    }

    @Test
    void mapToDO_keepsTheId() {
        NewLineAction action = new NewLineAction();

        assertThat(new NewLineActionMapper(mapperSupporter).mapToDO(action).getId()).isEqualTo(action.getId());
    }
}
