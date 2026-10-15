package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.TurnsActionData;
import com.pdg.adventure.server.action.TurnsAction;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class TurnsActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;

    @InjectMocks private TurnsActionMapper mapper;

    @Test
    void mapToBO_buildsAnActionThatReadsTurnsFromTheSharedVariables() {
        VariableProvider variables = new VariableProvider();
        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 3);
        when(mapperSupporter.getVariableProvider()).thenReturn(variables);

        TurnsAction result = mapper.mapToBO(new TurnsActionData());

        assertThat(result).isNotNull();
        assertThat(result.execute().getResultMessage()).isEqualTo("You have taken 3 turns.");
    }

    @Test
    void mapToDO_throwsUnsupportedOperation() {
        assertThatThrownBy(() -> mapper.mapToDO(Mockito.mock(TurnsAction.class)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
