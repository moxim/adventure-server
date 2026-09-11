package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.server.action.QuitAction;

@ExtendWith(MockitoExtension.class)
class QuitActionMapperTest {

    @InjectMocks private QuitActionMapper mapper;

    @Test
    void mapToBO_returnsQuitAction() {
        QuitActionData data = new QuitActionData();

        QuitAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(QuitAction.class);
    }

    @Test
    void mapToDO_returnsQuitActionData() {
        QuitAction action = new QuitAction();

        QuitActionData data = mapper.mapToDO(action);

        assertThat(data).isNotNull().isInstanceOf(QuitActionData.class);
    }
}
