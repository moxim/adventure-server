package com.pdg.adventure.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Map;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Configuration
public class AdventureConfig {

    private final Vocabulary vocabulary = new Vocabulary();
    private final Map<String, Location> allLocations = new HashMap<>();
    private final Map<String, Item> allItems = new HashMap<>();
    private final Map<String, Container> allContainers = new HashMap<>();
    private final MessagesHolder allMessages = new MessagesHolder();
    private final VariableProvider allVariables = new VariableProvider();
    private final GameContext gameContext;

    public AdventureConfig(@Lazy GameContext aGameContext) {
        gameContext = aGameContext;
    }

    public GameContext gameContext() {
        return gameContext;
    }

    @Bean
    public Map<String, Location> allLocations() {
        return allLocations;
    }

    @Bean
    public Map<String, Item> allItems() {
        return allItems;
    }

    @Bean
    public Map<String, Container> allContainers() {
        return allContainers;
    }

    @Bean
    public Vocabulary allWords() {
        return vocabulary;
    }

    @Bean
    public MessagesHolder allMessages() {
        return allMessages;
    }

    @Bean
    public VariableProvider allVariables() {
        return allVariables;
    }
}
