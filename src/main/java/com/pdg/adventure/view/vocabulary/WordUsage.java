package com.pdg.adventure.view.vocabulary;

public class WordUsage {
    final String usageType;
    final String itemId;
    final String itemType;

    WordUsage(String usageType, String itemId, String itemType) {
        this.usageType = usageType;
        this.itemId = itemId;
        this.itemType = itemType;
    }
}
