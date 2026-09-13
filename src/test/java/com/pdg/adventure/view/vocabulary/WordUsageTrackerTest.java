package com.pdg.adventure.view.vocabulary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;

class WordUsageTrackerTest {

    private AdventureData adventureData;
    private VocabularyData vocabularyData;
    private Word targetWord;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        vocabularyData = new VocabularyData();
        targetWord = new Word("sword", Word.Type.NOUN);
    }

    private WordUsageTracker tracker() {
        return new WordUsageTracker(adventureData, vocabularyData);
    }

    // --- getAllWordUsages: guard clauses ---

    @Test
    void getAllWordUsages_withNullAdventureData_returnsEmptyList() {
        WordUsageTracker tracker = new WordUsageTracker(null, vocabularyData);

        List<WordUsage> usages = tracker.getAllWordUsages(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void getAllWordUsages_withNullTargetWord_returnsEmptyList() {
        List<WordUsage> usages = tracker().getAllWordUsages(null);

        assertThat(usages).isEmpty();
    }

    @Test
    void getAllWordUsages_withNoUsagesAnywhere_returnsEmptyList() {
        List<WordUsage> usages = tracker().getAllWordUsages(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void getAllWordUsages_withNullVocabularyData_stillFindsLocationUsages() {
        LocationData location = locationWithDescriptionUsing(targetWord);
        adventureData.setLocationData(mapOf(location));

        WordUsageTracker tracker = new WordUsageTracker(adventureData, null);
        List<WordUsage> usages = tracker.getAllWordUsages(targetWord);

        assertThat(usages).extracting(u -> u.usageType).contains("Adjective", "Noun");
    }

    // --- special-word usages (take/drop/examine/synonym) ---

    @Test
    void getAllWordUsages_whenWordIsTakeVerb_reportsTakeVerbUsage() {
        vocabularyData.setTakeWord(targetWord);

        List<WordUsage> usages = tracker().getAllWordUsages(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Take Verb");
        assertThat(usages.get(0).itemType).isEqualTo(WordUsageTracker.SPECIAL);
    }

    @Test
    void getAllWordUsages_whenWordIsDropVerb_reportsDropVerbUsage() {
        vocabularyData.setDropWord(targetWord);

        List<WordUsage> usages = tracker().getAllWordUsages(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Drop Verb");
    }

    @Test
    void getAllWordUsages_whenWordIsExamineVerb_reportsExamineVerbUsage() {
        vocabularyData.setExamineWord(targetWord);

        List<WordUsage> usages = tracker().getAllWordUsages(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Examine Verb");
    }

    @Test
    void getAllWordUsages_whenWordIsNeitherTakeNorDropNorExamine_reportsNoSpecialUsage() {
        vocabularyData.setTakeWord(new Word("get", Word.Type.VERB));

        List<WordUsage> usages = tracker().getAllWordUsages(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void getAllWordUsages_whenAnotherWordIsSynonymOfTarget_reportsSynonymUsage() {
        Word synonym = new Word("blade", targetWord);
        synonym.setType(Word.Type.NOUN);
        vocabularyData.addWord(targetWord);
        vocabularyData.addWord(synonym);

        List<WordUsage> usages = tracker().getAllWordUsages(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Synonym");
        assertThat(usages.get(0).itemId).contains("blade").contains("NOUN");
        assertThat(usages.get(0).itemType).isEqualTo("Word");
    }

    @Test
    void getAllWordUsages_whenNoWordPointsToTargetAsSynonym_reportsNoSynonymUsage() {
        Word unrelated = new Word("other", Word.Type.NOUN);
        vocabularyData.addWord(targetWord);
        vocabularyData.addWord(unrelated);

        List<WordUsage> usages = tracker().getAllWordUsages(targetWord);

        assertThat(usages).isEmpty();
    }

    // --- location-scoped usages: description, command provider, directions, items ---

    @Test
    void findWordUsagesInLocations_withNullLocationData_returnsEmptyList() {
        adventureData.setLocationData(null);

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_whenLocationDescriptionUsesWordAsAdjective_reportsIt() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        DescriptionData description = new DescriptionData();
        description.setAdjective(targetWord);
        location.setDescriptionData(description);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Adjective");
        assertThat(usages.get(0).itemId).isEqualTo("loc-1");
        assertThat(usages.get(0).itemType).isEqualTo("Location");
    }

    @Test
    void findWordUsagesInLocations_whenLocationDescriptionUsesWordAsNoun_reportsIt() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        DescriptionData description = new DescriptionData();
        description.setNoun(targetWord);
        location.setDescriptionData(description);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Noun");
    }

    @Test
    void findWordUsagesInLocations_withNullDescriptionData_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setDescriptionData(null);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_whenLocationCommandUsesWordAsVerb_reportsIt() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setCommandProviderData(commandProviderUsing(targetWord, null, null));
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Verb in Command");
        assertThat(usages.get(0).itemType).isEqualTo("Location");
    }

    @Test
    void findWordUsagesInLocations_whenLocationCommandUsesWordAsAdjectiveAndNoun_reportsBoth() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setCommandProviderData(commandProviderUsing(null, targetWord, targetWord));
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).extracting(u -> u.usageType)
                .containsExactlyInAnyOrder("Adjective in Command", "Noun in Command");
    }

    @Test
    void findWordUsagesInLocations_withNullCommandProviderData_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setCommandProviderData(null);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_withNullAvailableCommands_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(null);
        location.setCommandProviderData(provider);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_withNullCommandChainInMap_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(new HashMap<>(Map.of()));
        provider.getAvailableCommands().put("spec", null);
        location.setCommandProviderData(provider);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_withNullCommandDataInChain_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        CommandChainData chain = new CommandChainData();
        chain.getCommands().add(null);
        CommandProviderData provider = new CommandProviderData();
        provider.getAvailableCommands().put("spec", chain);
        location.setCommandProviderData(provider);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_withCommandDataWithNoCommandDescription_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        CommandData command = new CommandData();
        command.setCommandDescription(null);
        CommandChainData chain = new CommandChainData();
        chain.getCommands().add(command);
        CommandProviderData provider = new CommandProviderData();
        provider.getAvailableCommands().put("spec", chain);
        location.setCommandProviderData(provider);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_whenDirectionDescriptionUsesWord_reportsDirectionUsage() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        DirectionData direction = new DirectionData();
        direction.setId("dir-1");
        DescriptionData description = new DescriptionData();
        description.setNoun(targetWord);
        direction.setDescriptionData(description);
        Set<DirectionData> directions = new HashSet<>();
        directions.add(direction);
        location.setDirectionsData(directions);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Noun");
        assertThat(usages.get(0).itemId).isEqualTo("dir-1");
        assertThat(usages.get(0).itemType).isEqualTo("Direction");
    }

    @Test
    void findWordUsagesInLocations_whenDirectionCommandUsesWord_reportsDirectionUsage() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        DirectionData direction = new DirectionData();
        direction.setId("dir-1");
        direction.setCommandData(commandDataUsing(targetWord, null, null));
        Set<DirectionData> directions = new HashSet<>();
        directions.add(direction);
        location.setDirectionsData(directions);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Verb in Command");
        assertThat(usages.get(0).itemType).isEqualTo("Direction");
    }

    @Test
    void findWordUsagesInLocations_withNullDirectionsData_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setDirectionsData(null);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_whenItemDescriptionUsesWord_reportsItemUsage() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        ItemData item = new ItemData();
        item.setId("item-1");
        DescriptionData description = new DescriptionData();
        description.setAdjective(targetWord);
        item.setDescriptionData(description);
        ItemContainerData container = new ItemContainerData("loc-1");
        container.getItems().add(item);
        location.setItemContainerData(container);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Adjective");
        assertThat(usages.get(0).itemId).isEqualTo("item-1");
        assertThat(usages.get(0).itemType).isEqualTo("Item");
    }

    @Test
    void findWordUsagesInLocations_whenItemCommandUsesWord_reportsItemUsage() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        ItemData item = new ItemData();
        item.setId("item-1");
        item.setCommandProviderData(commandProviderUsing(null, null, targetWord));
        ItemContainerData container = new ItemContainerData("loc-1");
        container.getItems().add(item);
        location.setItemContainerData(container);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).hasSize(1);
        assertThat(usages.get(0).usageType).isEqualTo("Noun in Command");
        assertThat(usages.get(0).itemType).isEqualTo("Item");
    }

    @Test
    void findWordUsagesInLocations_withNullItemInList_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.getItems().add(null);
        location.setItemContainerData(container);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_withNullItemContainerData_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(null);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void findWordUsagesInLocations_withNullItemsListInContainer_isSkippedSafely() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(null);
        location.setItemContainerData(container);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().findWordUsagesInLocations(targetWord);

        assertThat(usages).isEmpty();
    }

    @Test
    void getAllWordUsages_aggregatesSpecialWordAndLocationUsagesTogether() {
        vocabularyData.setTakeWord(targetWord);

        LocationData location = locationWithDescriptionUsing(targetWord);
        adventureData.setLocationData(mapOf(location));

        List<WordUsage> usages = tracker().getAllWordUsages(targetWord);

        assertThat(usages).extracting(u -> u.usageType)
                .contains("Take Verb", "Adjective", "Noun");
    }

    // --- createUsagesText / createGroupedUsages ---

    @Test
    void createUsagesText_withNoUsages_reportsZeroUsages() {
        StringBuilder text = tracker().createUsagesText(List.of(), 16);

        assertThat(text.toString()).startsWith("Found 0 usage(s):");
    }

    @Test
    void createUsagesText_groupsUsagesByItemTypeAndListsEachOne() {
        List<WordUsage> usages = List.of(
                new WordUsage("Adjective", "loc-1", "Location"),
                new WordUsage("Noun in Command", "item-1", "Item"));

        StringBuilder text = tracker().createUsagesText(usages, 16);
        String message = text.toString();

        assertThat(message).startsWith("Found 2 usage(s):");
        assertThat(message).contains("Locations:").contains("loc-1").contains("(Adjective)");
        assertThat(message).contains("Items:").contains("item-1").contains("(Noun in Command)");
    }

    @Test
    void createUsagesText_whenUsageTypeMatchesGroupType_omitsRedundantParenthetical() {
        // Constructed directly (never happens via getAllWordUsages) to exercise the branch
        // where the per-entry usage type equals the group's own key.
        List<WordUsage> usages = List.of(new WordUsage("Location", "loc-1", "Location"));

        StringBuilder text = tracker().createUsagesText(usages, 16);

        assertThat(text.toString()).contains("loc-1").doesNotContain("(Location)");
    }

    @Test
    void createUsagesText_includesAllUsages_evenWhenMaxUsagesToShowIsSmaller() {
        List<WordUsage> usages = List.of(
                new WordUsage("Adjective", "loc-1", "Location"),
                new WordUsage("Adjective", "loc-2", "Location"),
                new WordUsage("Adjective", "loc-3", "Location"));

        StringBuilder text = tracker().createUsagesText(usages, 1);

        assertThat(text.toString()).contains("loc-1").contains("loc-2").contains("loc-3");
    }

    // --- fixtures ---

    private static Map<String, LocationData> mapOf(LocationData location) {
        Map<String, LocationData> map = new HashMap<>();
        map.put(location.getId(), location);
        return map;
    }

    private static LocationData locationWithDescriptionUsing(Word word) {
        LocationData location = new LocationData();
        location.setId("loc-1");
        DescriptionData description = new DescriptionData();
        description.setAdjective(word);
        description.setNoun(word);
        location.setDescriptionData(description);
        return location;
    }

    private static CommandProviderData commandProviderUsing(Word verb, Word adjective, Word noun) {
        CommandProviderData provider = new CommandProviderData();
        CommandChainData chain = new CommandChainData();
        chain.getCommands().add(commandDataUsing(verb, adjective, noun));
        provider.getAvailableCommands().put("spec", chain);
        return provider;
    }

    private static CommandData commandDataUsing(Word verb, Word adjective, Word noun) {
        CommandDescriptionData description = new CommandDescriptionData();
        description.setVerb(verb);
        description.setAdjective(adjective);
        description.setNoun(noun);
        return new CommandData(description);
    }
}
