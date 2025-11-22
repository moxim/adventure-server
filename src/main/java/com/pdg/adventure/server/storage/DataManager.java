package com.pdg.adventure.server.storage;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basic.CommandDescriptionData;

public class DataManager {
    private final AdventureService service;

    public DataManager(AdventureService service) {
        this.service = service;
    }

    public AdventureData loadAdventure(String adventureId) {
        return service.findAdventureById(adventureId);
    }

    public LocationData loadLocation(String adventureId, String locationId) {
        AdventureData adventure = loadAdventure(adventureId);
        return adventure.getLocationData().getOrDefault(locationId, new LocationData());
    }

    public void saveLocation(AdventureData adventure, LocationData location) {
        adventure.getLocationData().put(location.getId(), location);
        service.saveLocationData(location);
        service.saveAdventureData(adventure);
    }

    public void saveCommand(LocationData location, CommandDescriptionData command) {
        CommandProviderData provider = location.getCommandProviderData();
        provider.getAvailableCommands().computeIfAbsent(command.getCommandSpecification(), k -> new CommandChainData())
                .getCommands().add(new CommandData(command));
        service.saveLocationData(location);
    }
}
