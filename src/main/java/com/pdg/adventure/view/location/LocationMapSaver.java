package com.pdg.adventure.view.location;

import com.vaadin.flow.component.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;

/** Saves the locations a world map dialog changed and tells the author if that failed instead of failing silently. */
final class LocationMapSaver {
    private static final Logger LOG = LoggerFactory.getLogger(LocationMapSaver.class);

    private LocationMapSaver() {
    }

    /**
     * Saves the locations one by one (there is no transaction). A failure is logged and shown to the author, who is
     * asked to reload the map because it may show changes that were not stored.
     */
    static void saveAll(AdventureService aService, Collection<LocationData> aLocations) {
        try {
            aLocations.forEach(aService::saveLocationData);
        } catch (RuntimeException e) {
            LOG.error("Could not save the world map changes", e);
            Notification.show("Could not save all changes (" + e.getMessage() + "). Reload the map to see what was "
                              + "stored.", 8000, Notification.Position.MIDDLE);
        }
    }
}
