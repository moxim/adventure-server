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

    private final GameContext gameContext;

    public AdventureConfig(@Lazy GameContext aGameContext) {
        gameContext = aGameContext;
    }

    public GameContext gameContext() {
        return gameContext;
    }

    @Bean
    @Lazy
    public Map<String, Location> allLocations() {
        return new HashMap<>();
    }

    @Bean
    @Lazy
    public Map<String, Item> allItems() {
        return new HashMap<>();
    }

    @Bean
    @Lazy
    public Map<String, Container> allContainers() {
        return new HashMap<>();
    }

    @Bean
    @Lazy
    public Vocabulary allWords() {
        return new Vocabulary();
    }

    @Bean
    @Lazy
    public MessagesHolder allMessages() {
        return new MessagesHolder();
    }

    @Bean
    @Lazy
    public VariableProvider allVariables() {
        return new VariableProvider();
    }
}
