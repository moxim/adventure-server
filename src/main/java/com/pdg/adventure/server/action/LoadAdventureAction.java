package com.pdg.adventure.server.action;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;

import com.pdg.adventure.CommandFactory;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.tangible.Thing;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.storage.service.AdventureService;

public class LoadAdventureAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(LoadAdventureAction.class);

    private final transient AdventureService adventureService;
    private final transient AdventureMapper adventureMapper;
    private final transient AdventureConfig adventureConfig;
    private final transient GameContext gameContext;

    @Setter
    private String adventureId;

    public LoadAdventureAction(AdventureService anAdventureService, AdventureMapper anAdventureMapper,
                               @Lazy AdventureConfig anAdventureConfig,
                               GameContext aGameContext) {
        super(anAdventureConfig.allMessages());
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        adventureConfig = anAdventureConfig;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        loadAdventure(adventureId);
        return result;
    }

    public void loadAdventure(final String anAdventureId) {
        if (anAdventureId == null || anAdventureId.isBlank()) {
            listAdventures();
            return;
        }
        final Optional<AdventureData> loadedAdventure = adventureService.findAdventureById(anAdventureId);
        if (loadedAdventure.isEmpty()) {
            listAdventures();
            return;
        }
        AdventureData adventureData = loadedAdventure.get();
        LOG.info("Loaded adventure: {}", adventureData.getTitle());

        if (adventureData.getLocationData().isEmpty()) {
            LOG.error("The adventure '{}' has no locations defined. Please add locations and try again.",
                      adventureData.getTitle());
            return;
        }

        adventureConfig.allMessages().clear();
        registerEngineMessages(adventureConfig.allMessages());
        for (MessageData messageData : adventureData.getMessages().values()) {
            adventureConfig.allMessages().addMessage(messageData.getMessageId(), messageData.getText());
        }

        // Reset all registries before mapping. Mapping registers the new adventure's locations,
        // containers and items as it goes; anything left over from a previously loaded adventure
        // would let references resolve against stale objects.
        adventureConfig.allLocations().clear();
        adventureConfig.allItems().clear();
        adventureConfig.allContainers().clear();

        Adventure savedAdventure = adventureMapper.mapToBO(adventureData);

        final var adventureLocations = savedAdventure.getLocations();

        CommandFactory commandFactory = new CommandFactory(
                adventureConfig.allMessages(), gameContext, adventureData.getVocabularyData());
        commandFactory.applyExamineFallback(adventureConfig.allLocations().values());
        List<Thing> loadedItems = adventureLocations.stream()
                .filter(loc -> loc.getItemContainer() != null)
                .flatMap(loc -> loc.getItemContainer().getContents().stream())
                .filter(c -> c instanceof Thing)
                .map(c -> (Thing) c)
                .toList();
        commandFactory.applyExamineFallback(loadedItems);

        String startLocationId = savedAdventure.getCurrentLocationId();
        Location startLocation = adventureConfig.allLocations().get(startLocationId);

        if (startLocation == null) {
            LOG.error("Warning: Could not find location with ID: '{}'", startLocationId);
            LOG.error("Available location IDs: {}", adventureConfig.allLocations().keySet());
            // Fallback to first available location
            startLocation = adventureLocations.getFirst();
            LOG.error("Using fallback location: {}", startLocation.getId());
        }

        gameContext.setCurrentLocation(startLocation);

        gameContext.setPocket(savedAdventure.getPocket());

        gameContext.setWorkflowData(adventureData.getWorkflowData());

        throw new ReloadAdventureException("Adventure reloaded, restarting game...");
    }

    // Generic, adventure-independent outcome messages that Action classes look up by fixed id
    // (WearAction "-6", RemoveAction "-7", MoveItemAction "-8"/"-9", InventoryAction "-10",
    // DestroyAction "-11", CreateAction "-12") - authors never define these themselves, so they
    // must be re-seeded every time allMessages is cleared, regardless of which adventure is
    // loaded. Mirrors the subset of MiniAdventureContent.setUpMessages() that isn't specific to
    // the console's built-in demo adventure.
    private void registerEngineMessages(MessagesHolder aMessagesHolder) {
        aMessagesHolder.addMessage("-6", "You can't wear %s.");
        aMessagesHolder.addMessage("-7", "You can't remove %s.");
        aMessagesHolder.addMessage("-8", "The %s is full.");
        aMessagesHolder.addMessage("-9", "You put %s into %s.");
        aMessagesHolder.addMessage("-10", "You carry:");
        aMessagesHolder.addMessage("-11", "The %s evaporates into thin air.");
        aMessagesHolder.addMessage("-12", "A %s appears in the %s.");
    }

    private void listAdventures() {
        adventureService.getAdventures().forEach(
                adventure -> LOG.info("Available adventure - ID: {}, Title: {}", adventure.getId(),
                                      adventure.getTitle()));
    }
}
