package com.pdg.adventure.server;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Ided;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Component
public class Adventure implements Ided {

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String currentLocationId;

    @Getter
    @Setter
    private GenericContainer pocket;

    private final Map<String, Location> locationMap;
    private final Vocabulary allWords;
    private final transient MessagesHolder allMessages;
    private final Map<String, Item> allItems;
    private final Map<String, Container> allContainers;
    private final transient VariableProvider variableProvider;

    // Todo: must I autowire? where?
    @Autowired
    public Adventure(Vocabulary aBagOfAllWords, Map<String, Container> aContainerBag, MessagesHolder aBagOfAllMessages,
                     Map<String, Item> aContainerForAllItems) {
        id = UUID.randomUUID().toString();
        locationMap = new HashMap<>();
        pocket = new GenericContainer(new DescriptionProvider("Player Pocket"), 20);

        allWords = aBagOfAllWords;
        allContainers = aContainerBag;
        allMessages = aBagOfAllMessages;
        allItems = aContainerForAllItems;
        variableProvider = new VariableProvider();
    }

    public void run() {
        System.out.println("Current location: " + currentLocationId);
        System.out.println("Pocket: " + pocket);
        System.out.println("All locations: " + locationMap);

/*
        GameLoop gameLoop = new GameLoop(new Parser(allWords));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        gameLoop.run(reader);
*/
    }

    public void setLocations(List<Location> aLocationList) {
        for (Location location : aLocationList) {
            locationMap.put(location.getId(), location);
        }
        if (!aLocationList.isEmpty() && (currentLocationId == null || currentLocationId.isEmpty())) {
            currentLocationId = aLocationList.getFirst().getId();
        }
    }

    public List<Location> getLocations() {
        return new ArrayList<>(locationMap.values());
    }

    public Vocabulary getVocabulary() {
        return allWords;
    }

    @Override
    public String toString() {
        return "Adventure{" +
               "id='" + id + '\'' +
               ", currentLocationId=" + currentLocationId +
               ", pocket=" + pocket +
               ", locationMap=" + locationMap +
               '}';
    }
}
