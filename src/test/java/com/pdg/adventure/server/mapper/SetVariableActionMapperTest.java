package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.SetVariableAction;
import com.pdg.adventure.server.mapper.action.SetVariableActionMapper;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@SpringBootTest
class SetVariableActionMapperTest {

    private AdventureConfig adventureConfig;

    private MapperSupporter mapperSupporter;
    private MessagesHolder messagesHolder;
    private VariableProvider variableProvider;

    @Autowired
    public SetVariableActionMapperTest(AdventureConfig anAdventureConfig) {
        adventureConfig = anAdventureConfig;
        mapperSupporter = new MapperSupporter(adventureConfig);
        messagesHolder = adventureConfig.allMessages();
        variableProvider = adventureConfig.allVariables();
    }

    @Test
    void mapToDOAndBO() {
        SetVariableAction setVariableAction = new SetVariableAction("name", "value", variableProvider, messagesHolder);
        Mapper<SetVariableActionData, SetVariableAction> mapper = new SetVariableActionMapper(mapperSupporter);
        final SetVariableActionData setVariableActionData = mapper.mapToDO(setVariableAction);
        assertThat(setVariableActionData).isNotNull();
        assertThat(setVariableActionData).isInstanceOf(ActionData.class);
        assertThat(setVariableActionData.getActionName()).isEqualTo("SetVariableActionData");
        assertThat(setVariableActionData.getVariableName()).isEqualTo("name");
        assertThat(setVariableActionData.getVariableValue()).isEqualTo("value");
        final SetVariableAction setVariableActionReborn = mapper.mapToBO(setVariableActionData);
        assertThat(setVariableActionReborn).isNotNull();
        assertThat(setVariableActionReborn).isInstanceOf(SetVariableAction.class);
        assertThat(setVariableActionReborn.getVariableName()).isEqualTo("name");
        assertThat(setVariableActionReborn.getVariableValue()).isEqualTo("value");
    }
}
