package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.BreakActionData;
import com.pdg.adventure.server.action.BreakAction;

@ExtendWith(MockitoExtension.class)
class BreakActionMapperTest {

    @InjectMocks private BreakActionMapper mapper;

    @Test
    void mapToBO_returnsBreakAction() {
        BreakActionData data = new BreakActionData();

        BreakAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(BreakAction.class);
    }

    @Test
    void mapToDO_returnsBreakActionData() {
        BreakAction action = new BreakAction();

        BreakActionData data = mapper.mapToDO(action);

        assertThat(data).isNotNull().isInstanceOf(BreakActionData.class);
    }
}
