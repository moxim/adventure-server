package com.pdg.adventure.server.testhelper;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.parser.GenericCommandDescription;

public class TestSupporter {
    private TestSupporter() {
        // don't instantiate me
    }

    public static boolean conditionToBoolean(PreCondition aCondition) {
        final ExecutionResult executionResult = aCondition.check();
        return executionResult.getExecutionState() == ExecutionResult.State.SUCCESS;
    }

    public static boolean addItemToBoolean(Container aContainer, Containable anItem) {
        final ExecutionResult executionResult = aContainer.add(anItem);
        return executionResult.getExecutionState() == ExecutionResult.State.SUCCESS;
    }

    public static boolean removeItemToBoolean(Container aContainer, Containable anItem) {
        final ExecutionResult executionResult = aContainer.remove(anItem);
        return executionResult.getExecutionState() == ExecutionResult.State.SUCCESS;
    }

    public static boolean applyCommandToBoolean(Containable anItem, GenericCommandDescription aCommandDescription) {
        final ExecutionResult executionResult = anItem.applyCommand(aCommandDescription);
        return executionResult.getExecutionState() == ExecutionResult.State.SUCCESS;
    }


    public static CommandData createCommand(String anId, VocabularyData aVocabularyData) {
        final CommandData commandData = new CommandData();
        commandData.setId(anId);
        commandData.setCommandDescription(TestSupporter.createCommandDescriptionData(anId, aVocabularyData));
        return commandData;
    }

    public static CommandDescriptionData createCommandDescriptionData(String anId, VocabularyData aVocabularyData) {
        CommandDescriptionData result = new CommandDescriptionData();
        result.setId(anId);
        Word verb = new Word(anId + "_verb", Word.Type.VERB);
        result.setVerb(verb);
        Word adjective = new Word(anId + "_adjective", Word.Type.ADJECTIVE);
        result.setAdjective(adjective);
        Word noun = new Word(anId + "_noun", Word.Type.NOUN);
        result.setNoun(noun);

        aVocabularyData.addWord(verb);
        aVocabularyData.addWord(adjective);
        aVocabularyData.addWord(noun);

        return result;
    }

    /**
     * An adventure with two locations (cellar -> attic), a lamp in the cellar, a key in the pocket, a picture on the
     * cellar, a "take"/"grab" vocabulary, a message and the variable score=3. Not persisted: save the picture(s)
     * first, then the adventure. Persisted it spans 1 adventure, 2 locations, 3 containers, 2 items, 1 picture,
     * 1 vocabulary and 2 words.
     */
    public static AdventureData createSampleAdventure(String aTitle) {
        AdventureData adventure = new AdventureData();
        adventure.setTitle(aTitle);
        adventure.setFont(AdventureFont.MEDIEVAL_SHARP);

        Word take = adventure.getVocabularyData().createWord("take", Word.Type.VERB);
        adventure.getVocabularyData().createSynonym("grab", take);

        LocationData cellar = new LocationData();
        LocationData attic = new LocationData();
        ItemData lamp = new ItemData();
        lamp.setAdventureId(adventure.getId());
        lamp.setLocationId(cellar.getId());
        lamp.setParentContainerId(cellar.getItemContainerData().getId());
        cellar.getItemContainerData().getItems().add(lamp);

        DirectionData up = new DirectionData();
        up.setDestinationId(attic.getId());
        cellar.getDirectionsData().add(up);

        PictureData picture = new PictureData();
        picture.setAdventureId(adventure.getId());
        picture.setName("cellar.png");
        picture.setContent(new byte[] {1, 2, 3});
        picture.setContentType("image/png");
        cellar.setPictureId(picture.getId());
        adventure.getPictureData().put(picture.getId(), picture);

        adventure.getLocationData().put(cellar.getId(), cellar);
        adventure.getLocationData().put(attic.getId(), attic);
        adventure.setCurrentLocationId(cellar.getId());

        ItemData key = new ItemData();
        key.setAdventureId(adventure.getId());
        adventure.getPlayerPocket().getItems().add(key);

        MessageData message = new MessageData("greeting", "hello");
        adventure.getMessages().put(message.getId(), message);

        adventure.getVariableData().addVariable("score", 3);
        return adventure;
    }
}
