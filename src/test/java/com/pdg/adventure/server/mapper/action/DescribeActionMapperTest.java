package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.DescribeAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class DescribeActionMapperTest {

    private static final String TARGET_ID = "target-1";
    private static final String ITEM_DESCRIPTION = "a brass lantern, glowing softly";
    private static final String LOCATION_DESCRIPTION = "a dim cave with stalactites";

    @Mock private MapperSupporter mapperSupporter;
    @Mock private AdventureConfig adventureConfig;
    @Mock private MessagesHolder messagesHolder;
    @Mock private Item item;
    @Mock private Location location;

    @InjectMocks private DescribeActionMapper mapper;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(adventureConfig.allMessages()).thenReturn(messagesHolder);
    }

    @Test
    void mapToBO_describesItemWhenTargetIsItem() {
        DescribeActionData data = new DescribeActionData();
        data.setTargetId(TARGET_ID);
        when(adventureConfig.allItems()).thenReturn(Map.of(TARGET_ID, item));
        when(item.getLongDescription()).thenReturn(ITEM_DESCRIPTION);

        DescribeAction action = mapper.mapToBO(data);
        ExecutionResult result = action.execute();

        assertThat(result.getResultMessage()).isEqualTo(ITEM_DESCRIPTION);
    }

    @Test
    void mapToBO_fallsBackToLocationDescription() {
        DescribeActionData data = new DescribeActionData();
        data.setTargetId(TARGET_ID);
        when(adventureConfig.allItems()).thenReturn(Map.of());
        when(adventureConfig.allLocations()).thenReturn(Map.of(TARGET_ID, location));
        when(location.getLongDescription()).thenReturn(LOCATION_DESCRIPTION);

        DescribeAction action = mapper.mapToBO(data);
        ExecutionResult result = action.execute();

        assertThat(result.getResultMessage()).isEqualTo(LOCATION_DESCRIPTION);
    }

    @Test
    void mapToBO_returnsEmptyStringWhenTargetUnknown() {
        DescribeActionData data = new DescribeActionData();
        data.setTargetId("missing");
        when(adventureConfig.allItems()).thenReturn(Map.of());
        when(adventureConfig.allLocations()).thenReturn(Map.of());

        DescribeAction action = mapper.mapToBO(data);
        ExecutionResult result = action.execute();

        assertThat(result.getResultMessage()).isEmpty();
    }

    @Test
    void mapToDO_throwsUnsupportedOperation() {
        assertThatThrownBy(() -> mapper.mapToDO(Mockito.mock(DescribeAction.class)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
