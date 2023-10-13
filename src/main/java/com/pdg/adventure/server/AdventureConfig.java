package com.pdg.adventure.server;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AdventureConfig {

    @Bean
    public Map<String, Location> allLocations() {
        return new HashMap<>();
    }

    @Bean
    public Map<String, Item> allItems() {
        return new HashMap<>();
    }

    @Bean
    public Map<String, Container> allContainers()  {
        return new HashMap<>();
    }

    @Bean
    public Vocabulary allWords() {
        return new Vocabulary();
    }

    @Bean
    public MessagesHolder allMessages() {
        return new MessagesHolder();
    }

    @Bean
    public VariableProvider allVocabulary() {
        return new VariableProvider();
    }


}
