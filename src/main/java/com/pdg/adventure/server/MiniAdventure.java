package com.pdg.adventure.server;

import com.pdg.adventure.server.action.DescribeAction;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.MoveAction;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.location.Direction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class MiniAdventure {
    private final Vocabulary vocabulary;
    private final Container pocket;
    private final Location location;
    private final Item ring;

    private static final String SMALL_TEXT = "small";

    public static void main(String[] args) {
        MiniAdventure game = new MiniAdventure();

        game.setup();

        Environment.tell("$> look");
        Environment.show(game.location);

        new MoveAction(game.ring, game.pocket).execute();

        Environment.showContents(game.pocket, "In the %s you see:");

        Environment.tell("$> look");
        Environment.show(game.location);
    }

    private void setup() {
        setUpVocabulary();
        new MessageAction("You have words!").execute();
        setUpItems(location.getContainer());
        new MessageAction("You have items!").execute();
        setUpDirections();
    }

    private void setUpDirections() {
        DescriptionProvider portalDescription = new DescriptionProvider("fading", "portal");
        portalDescription.setLongDescription("The portal fades slowly already, it looks like it closes soon!");
        Location portal = new Location(portalDescription);

        Direction toPortal = new Direction("enter", portal);
        location.addDirection(toPortal);
    }

    private void setUpMoveCommands(Item anItem) {
        GenericCommand command = new GenericCommand("take", new MoveAction(anItem, pocket));
        anItem.addCommand(command);
    }

    private void setUpItems(Container container) {
        Item knife = new Item(new DescriptionProvider(SMALL_TEXT, "knife"), true);
        knife.setShortDescription("small sharp knife");
        knife.setLongDescription("The knife is exceptionally sharp. Don't cut yourself!");
        setUpMoveCommands(knife);

        knife.addCommand(new GenericCommand("look", new DescribeAction(knife)));
        container.add(knife);

        Item rabbit = new Item(new DescriptionProvider(SMALL_TEXT, "rabbit"), true);
        rabbit.setLongDescription("The rabbit looks very tasty!");
        rabbit.addCommand(new GenericCommand("look", new DescribeAction(rabbit)));
        GenericCommand cut = new GenericCommand("cut", new MessageAction("You cut the rabbit to pieces."));
        rabbit.addCommand(cut);
        setUpMoveCommands(rabbit);

        container.add(rabbit);
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

        ring = new Item(new DescriptionProvider("golden", "ring"), true);
        location.getContainer().add(ring);
        setUpMoveCommands(ring);
    }

    private void setUpVocabulary() {
        vocabulary.addWord("desc", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("look", "desc");

        vocabulary.addWord("knife", Vocabulary.WordType.NOUN);

        vocabulary.addWord("get", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("take", "get");

        vocabulary.addWord("open", Vocabulary.WordType.VERB);
        vocabulary.addWord("enter", Vocabulary.WordType.VERB);
        vocabulary.addWord("cut", Vocabulary.WordType.VERB);
        vocabulary.addWord("kill", Vocabulary.WordType.VERB);

        vocabulary.addWord("rabbit", Vocabulary.WordType.NOUN);
        vocabulary.addSynonym("hare", "rabbit");

        vocabulary.addWord("ring", Vocabulary.WordType.NOUN);

        vocabulary.addWord("big", Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord(SMALL_TEXT, Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord("golden", Vocabulary.WordType.ADJECTIVE);

        vocabulary.addWord("portal", Vocabulary.WordType.NOUN);
    }

}
