package com.pdg.adventure.server;

import com.pdg.adventure.server.action.*;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.location.Direction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import com.pdg.adventure.server.vocabulary.Word;

public class MiniAdventure {
    private final Vocabulary vocabulary;
    private final Container pocket;
    private final Location location;

    private static final String SMALL_TEXT = "small";

    public static void main(String[] args) {
        MiniAdventure game = new MiniAdventure();

        game.setup();

        Environment.show(game.location);

        new MoveAction(new Item(new DescriptionProvider("ring"), true), game.pocket).execute();
    }

    private void setup() {
        setUpVocabulary();
        new MessageAction("You have words!").execute();
        setUpItems(location.getContainer());
        new MessageAction("You have items!").execute();
        setUpDirections();
    }

    private void setUpDirections() {
        CommandDescription enterPortal = new CommandDescription("enter", "portal");
        DescriptionProvider portalDescription = new DescriptionProvider("glowing", "portal");
        portalDescription.setLongDescription("The portal fades already, it looks like it closes soon!");
        Location portal = new Location(portalDescription);
        Direction toPortal = new Direction(portal.getContainer(), enterPortal);
        location.addDirection(toPortal);
    }

    private void setUpMoveCommands(Item anItem) {
        CommandDescription description = new CommandDescription("take", anItem);
        GenericCommand command = new GenericCommand(description, new MoveAction(anItem, pocket));
        anItem.addCommand(command);
    }

    private void setUpItems(Container container) {
        Item knife = new Item(new DescriptionProvider(SMALL_TEXT, "knife"), true);
        knife.setShortDescription("small sharp knife");
        knife.setLongDescription("The knife is exceptionally sharp. Don't cut yourself!");
        setUpMoveCommands(knife);

        knife.addAction(new DescribeAction(knife));
        container.addItem(knife);

        Item rabbit = new Item(new DescriptionProvider(SMALL_TEXT, "rabbit"), true);
        rabbit.setLongDescription("The rabbit looks very tasty!");
        rabbit.addAction(new DescribeAction(rabbit));
        GenericCommand cut = new GenericCommand(new CommandDescription("cut",  rabbit), new MessageAction(
                "You cut the rabbit to pieces."));
        rabbit.addCommand(cut);
        setUpMoveCommands(rabbit);

        Item ring = new Item(new DescriptionProvider("golden", "ring"), true);
        container.addItem(ring);
        setUpMoveCommands(ring);

        container.addItem(rabbit);
    }

    public MiniAdventure() {
        vocabulary = new Vocabulary();
        DescriptionProvider locationDescription = new DescriptionProvider("first", "location");
        locationDescription.setShortDescription("first location looks eerie");
        locationDescription.setLongDescription("This is indeed an eerie looking location. Some strange mind must have" +
                " created it");
        location = new Location(locationDescription);
        new MessageAction("You enter the game.").execute();
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 3);
    }

    private void setUpVocabulary() {
        Word desc = new Word("desc", Word.WordType.VERB);
        vocabulary.addWord(desc);
        vocabulary.addSynonym("look", desc);

        Word knife = new Word("knife", Word.WordType.NOUN);
        vocabulary.addWord(knife);

        Word get = new Word("get", Word.WordType.VERB);
        vocabulary.addWord(get);
        vocabulary.addSynonym("take", get);

        vocabulary.addWord("open", Word.WordType.VERB);
        vocabulary.addWord("enter", Word.WordType.VERB);
        vocabulary.addWord("cut", Word.WordType.VERB);
        vocabulary.addWord("kill", Word.WordType.VERB);

        Word rabbit = new Word("rabbit", Word.WordType.NOUN);
        vocabulary.addWord(rabbit);
        vocabulary.addSynonym("hare", rabbit);

        Word ring = new Word("ring", Word.WordType.NOUN);
        vocabulary.addWord(ring);

        vocabulary.addWord("big", Word.WordType.ADJECTIVE);
        vocabulary.addWord(SMALL_TEXT, Word.WordType.ADJECTIVE);
        vocabulary.addWord("golden", Word.WordType.ADJECTIVE);

        vocabulary.addWord("portal", Word.WordType.NOUN);
    }

}
