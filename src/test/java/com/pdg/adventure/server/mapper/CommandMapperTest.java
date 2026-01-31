package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.condition.HereCondition;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Comprehensive unit tests for CommandMapper covering all major code paths:
 * 1. Basic command mapping (BO ↔ DO)
 * 2. Commands with main actions
 * 3. Commands with follow-up actions
 * 4. Commands with preconditions
 * 5. Commands with null actions
 * 6. Dynamic action mapper resolution
 * 7. Multiple follow-up actions
 * 8. Complex command scenarios
 * <p>
 * These tests maximize code coverage by testing:
 * - mapToBO: lines 29-50 (all branches)
 * - mapToDO: lines 53-63 (all branches)
 * - Action mapper resolution via MapperSupporter
 * - List processing for follow-up actions and preconditions
 */
@ExtendWith(MockitoExtension.class)
class CommandMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private CommandDescriptionMapper commandDescriptionMapper;

    @Mock
    private Mapper<? extends ActionData, ?> actionMapper;

    @Mock
    private Mapper<? extends PreConditionData, ?> conditionMapper;

    @Mock
    private CommandDescription commandDescription;

    @Mock
    private CommandDescriptionData commandDescriptionData;

    @Mock
    private ActionData mainActionData;

    @Mock
    private Action mainAction;

    @Mock
    private HereCondition preCondition1;

    @Mock
    private HereCondition preCondition2;

    @Mock
    private HereConditionData preConditionData1;

    @Mock
    private HereConditionData preConditionData2;

    private CommandMapper commandMapper;

    @BeforeEach
    void setUp() {
        commandMapper = new CommandMapper(mapperSupporter, commandDescriptionMapper);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts basic CommandData with main action to Command")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldConvertBasicCommandDataWithMainAction() {
        // Given: CommandData with main action only
        CommandData commandData = new CommandData(commandDescriptionData);
        commandData.setId("take-sword-cmd");
        commandData.setAction(mainActionData);

        when(commandDescriptionMapper.mapToBO(commandDescriptionData)).thenReturn(commandDescription);
        when(mapperSupporter.getMapper(mainActionData.getClass())).thenReturn((Mapper) actionMapper);
        when(((Mapper<ActionData, Action>) actionMapper).mapToBO(mainActionData)).thenReturn(mainAction);

        // When: mapping to business object
        Command result = commandMapper.mapToBO(commandData);

        // Then: command should be created with proper properties
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("take-sword-cmd");
        assertThat(result.getDescription()).isEqualTo(commandDescription);
        assertThat(result.getAction()).isEqualTo(mainAction);
        assertThat(result.getFollowUpActions()).isEmpty();
        assertThat(result.getPreconditions()).isEmpty();

        verify(commandDescriptionMapper).mapToBO(commandDescriptionData);
        verify(mapperSupporter).getMapper(mainActionData.getClass());
    }

    @Test
    @DisplayName("Test 2: mapToBO - handles null main action correctly")
    void mapToBO_shouldHandleNullMainAction() {
        // Given: CommandData with null main action
        CommandData commandData = new CommandData(commandDescriptionData);
        commandData.setId("look-cmd");
        // action is null

        when(commandDescriptionMapper.mapToBO(commandDescriptionData)).thenReturn(commandDescription);

        // When: mapping to business object
        Command result = commandMapper.mapToBO(commandData);

        // Then: command should be created with null action
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("look-cmd");
        assertThat(result.getAction()).isNull();

        verify(commandDescriptionMapper).mapToBO(commandDescriptionData);
        verify(mapperSupporter, never()).getMapper(any());
        verify(actionMapper, never()).mapToBO(any());
    }

    @Test
    @DisplayName("Test 3: mapToBO - maps command with follow-up actions")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldMapCommandWithFollowUpActions() {
        // Given: CommandData with main action and follow-up actions
        ActionData followUpAction1Data = mock(ActionData.class);
        ActionData followUpAction2Data = mock(ActionData.class);
        Action followUpAction1 = mock(Action.class);
        Action followUpAction2 = mock(Action.class);

        List<ActionData> followUpActions = new ArrayList<>();
        followUpActions.add(followUpAction1Data);
        followUpActions.add(followUpAction2Data);

        CommandData commandData = new CommandData(commandDescriptionData);
        commandData.setId("open-door-cmd");
        commandData.setAction(mainActionData);
        commandData.setFollowUpActions(followUpActions);

        when(commandDescriptionMapper.mapToBO(commandDescriptionData)).thenReturn(commandDescription);
        when(mapperSupporter.getMapper(any())).thenReturn((Mapper) actionMapper);
        when(((Mapper<ActionData, Action>) actionMapper).mapToBO(any(ActionData.class)))
                .thenReturn(mainAction, followUpAction1, followUpAction2);

        // When: mapping to business object
        Command result = commandMapper.mapToBO(commandData);

        // Then: all follow-up actions should be added
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("open-door-cmd");
        assertThat(result.getFollowUpActions()).hasSize(2);
        assertThat(result.getFollowUpActions()).containsExactly(followUpAction1, followUpAction2);
    }

//    There must not be a null value in the follow-up actions list as per CommandData#setFollowUpActions contract
//    @DisplayName("Test 4: mapToBO - handles null follow-up actions in list")

    @Test
    @DisplayName("Test 5: mapToBO - maps command with preconditions")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldMapCommandWithPreconditions() {
        // Given: CommandData with preconditions
        // Note: In the actual code, PreConditions in CommandData are of type PreConditionData
        // which is different from PreCondition used in Command (line 46-48 of CommandMapper)
        List<PreConditionData> preconditionsData = new ArrayList<>();
        preconditionsData.add(preConditionData1);
        preconditionsData.add(preConditionData2);

        CommandData commandData = new CommandData(commandDescriptionData);
        commandData.setId("take-hidden-item-cmd");
        commandData.setAction(mainActionData);
        commandData.setPreConditions(preconditionsData);

        when(commandDescriptionMapper.mapToBO(commandDescriptionData)).thenReturn(commandDescription);
        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && ActionData.class.isAssignableFrom(clazz)))).thenReturn((Mapper) actionMapper);
        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz)))).thenReturn((Mapper) conditionMapper);
        when(((Mapper<ActionData, Action>) actionMapper).mapToBO(any(ActionData.class))).thenReturn(mainAction);
        when(((Mapper<HereConditionData, HereCondition>) conditionMapper).mapToBO(any(HereConditionData.class)))
                .thenReturn(preCondition1, preCondition2);

        // When: mapping to business object
        Command result = commandMapper.mapToBO(commandData);

        // Then: all preconditions should be added
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("take-hidden-item-cmd");
        assertThat(result.getPreconditions()).hasSize(2);
        assertThat(result.getPreconditions()).contains(preCondition1, preCondition2);
    }

    @Test
    @DisplayName("Test 6: mapToBO - maps complex command with all features")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldMapComplexCommandWithAllFeatures() {
        // Given: CommandData with main action, follow-up actions, and preconditions
        ActionData followUpActionData = mock(ActionData.class);
        Action followUpAction = mock(Action.class);

        List<ActionData> followUpActions = new ArrayList<>();
        followUpActions.add(followUpActionData);

        List<PreConditionData> preconditionsData = new ArrayList<>();
        preconditionsData.add(preConditionData1);

        CommandData commandData = new CommandData(commandDescriptionData);
        commandData.setId("full-featured-cmd");
        commandData.setAction(mainActionData);
        commandData.setFollowUpActions(followUpActions);
        commandData.setPreConditions(preconditionsData);

        when(commandDescriptionMapper.mapToBO(commandDescriptionData)).thenReturn(commandDescription);
        when(mapperSupporter.getMapper(any())).thenReturn((Mapper) actionMapper);
        when(((Mapper<ActionData, Action>) actionMapper).mapToBO(any(ActionData.class)))
                .thenReturn(mainAction, followUpAction);

        // When: mapping to business object
        Command result = commandMapper.mapToBO(commandData);

        // Then: all features should be present
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("full-featured-cmd");
        assertThat(result.getAction()).isEqualTo(mainAction);
        assertThat(result.getFollowUpActions()).hasSize(1);
        assertThat(result.getPreconditions()).hasSize(1);
    }

    @Test
    @DisplayName("Test 7: mapToDO - converts Command to CommandData")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldConvertCommandToCommandData() {
        // Given: Command business object
        Command command = new GenericCommand(commandDescription, mainAction);
        command.setId("drop-item-cmd");

        when(commandDescriptionMapper.mapToDO(commandDescription)).thenReturn(commandDescriptionData);
        when(mapperSupporter.getMapper(mainAction.getClass())).thenReturn((Mapper) actionMapper);
        when(((Mapper<ActionData, Action>) actionMapper).mapToDO(mainAction)).thenReturn(mainActionData);

        // When: mapping to data object
        CommandData result = commandMapper.mapToDO(command);

        // Then: CommandData should be created with proper properties
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("drop-item-cmd");
        assertThat(result.getCommandDescription()).isEqualTo(commandDescriptionData);
        assertThat(result.getAction()).isEqualTo(mainActionData);
        // Note: mapToDO sets preconditions and followUpActions to null (TODO in production code)
        assertThat(result.getPreConditions()).isNull();
        assertThat(result.getFollowUpActions()).isNull();

        verify(commandDescriptionMapper).mapToDO(commandDescription);
    }

    @Test
    @DisplayName("Test 8: mapToDO - preserves command ID during conversion")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldPreserveCommandIdDuringConversion() {
        // Given: Multiple commands with different IDs
        Command command1 = new GenericCommand(commandDescription, mainAction);
        command1.setId("cmd-001");

        Command command2 = new GenericCommand(commandDescription, mainAction);
        command2.setId("cmd-002");

        when(commandDescriptionMapper.mapToDO(commandDescription)).thenReturn(commandDescriptionData);
        when(mapperSupporter.getMapper(mainAction.getClass())).thenReturn((Mapper) actionMapper);
        when(((Mapper<ActionData, Action>) actionMapper).mapToDO(mainAction)).thenReturn(mainActionData);

        // When: mapping both commands
        CommandData result1 = commandMapper.mapToDO(command1);
        CommandData result2 = commandMapper.mapToDO(command2);

        // Then: IDs should be preserved and different
        assertThat(result1.getId()).isEqualTo("cmd-001");
        assertThat(result2.getId()).isEqualTo("cmd-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());

        verify(commandDescriptionMapper, times(2)).mapToDO(commandDescription);
    }
}
