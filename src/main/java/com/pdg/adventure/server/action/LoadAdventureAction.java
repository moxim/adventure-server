package com.pdg.adventure.server.action;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.server.Adventure;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class LoadAdventureAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(LoadAdventureAction.class);

    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final AdventureConfig adventureConfig;

    @Setter
    private String adventureId;

    public LoadAdventureAction(AdventureService anAdventureService, AdventureMapper anAdventureMapper,
                               AdventureConfig anAdventureConfig, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        adventureConfig = anAdventureConfig;
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

        adventureConfig.allMessages().clear();
        for (MessageData messageData : adventureData.getMessages().values()) {
            adventureConfig.allMessages().addMessage(messageData.getMessageId(), messageData.getText());
        }

        Adventure savedAdventure = adventureMapper.mapToBO(adventureData);

        final var adventureLocations = savedAdventure.getLocations();

        if (adventureLocations.isEmpty()) {
            LOG.error("The adventure '{}' has no locations defined. Please add locations and try again.",
                      adventureData.getTitle());
            return;
        }

        adventureConfig.allLocations().clear();
        adventureLocations.forEach(location -> adventureConfig.allLocations().put(location.getId(), location));

        String startLocationId = savedAdventure.getCurrentLocationId();
        Location startLocation = adventureConfig.allLocations().get(startLocationId);

        if (startLocation == null) {
            LOG.error("Warning: Could not find location with ID: '{}'", startLocationId);
            LOG.error("Available location IDs: {}", adventureConfig.allLocations().keySet());
            // Fallback to first available location
            startLocation = adventureLocations.getFirst();
            LOG.error("Using fallback location: {}", startLocation.getId());
        }

        Environment.setCurrentLocation(startLocation);

        Environment.setPocket(savedAdventure.getPocket());

        throw new ReloadAdventureException("Adventure reloaded, restarting game...");
    }

    private void listAdventures() {
        adventureService.getAdventures().forEach(
                adventure -> LOG.info("Available adventure - ID: {}, Title: {}", adventure.getId(),
                                      adventure.getTitle()));
    }
}
