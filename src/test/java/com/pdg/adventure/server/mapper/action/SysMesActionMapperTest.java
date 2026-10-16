package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.model.action.SysMesActionData;
import com.pdg.adventure.server.action.SysMesAction;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class SysMesActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;

    private SysMesActionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SysMesActionMapper(mapperSupporter);
    }

    @Test
    void mapToBO_resolvesTheKey() {
        SysMesActionData data = new SysMesActionData();
        data.setMessageKeyId("15");
        data.setId("id");

        SysMesAction result = mapper.mapToBO(data);

        assertThat(result.getMessageKey()).isEqualTo(SystemMessageKey.SM15);
        assertThat(result.getId()).isEqualTo("id");
    }

    @Test
    void mapToBO_withAnUnknownKey_isAConfigurationError() {
        SysMesActionData data = new SysMesActionData();
        data.setMessageKeyId("nope");

        assertThatThrownBy(() -> mapper.mapToBO(data)).isInstanceOf(ConfigurationException.class)
                                                      .hasMessageContaining("nope");
    }

    @Test
    void mapToDO_roundTripsTheKeyId() {
        SysMesAction action = new SysMesAction(SystemMessageKey.SM15);

        SysMesActionData data = mapper.mapToDO(action);

        assertThat(data.getMessageKeyId()).isEqualTo("15");
        assertThat(data.getId()).isEqualTo(action.getId());
    }
}
