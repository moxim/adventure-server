package com.pdg.adventure.server;

import com.pdg.adventure.server.action.DescribeAction;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.action.MoveAction;
import com.pdg.adventure.server.action.SetVariableAction;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.conditional.EqualsCondition;
import com.pdg.adventure.server.location.Direction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class MiniAdventure {
    private final VariableProvider variableProvider;
    private final Vocabulary vocabulary;
    private final Container pocket;
    private final Item ring;
    private final Location portal;
    private final Location location;

    private static final String SMALL_TEXT = "small";

    public static void main(String[] args) {
        MiniAdventure game = new MiniAdventure();
        game.setup();
        game.run();
    }

    private void run() {
        Environment.setCurrentLocation(location);

        Environment.tell("$> inventory");
        Environment.showContents(pocket, "In the %s you see:");

        Environment.tell("$> look");
        Environment.show(location);

        Environment.tell("$> look at portal");
        portal.applyCommand("look");

        Environment.tell("$> look at ring");
        ring.applyCommand("look");

        Environment.tell("$> enter portal");
        location.applyCommand("enter");

        Environment.tell("$> wear ring");
        ring.applyCommand("wear");

        Environment.tell("$> enter portal");
        location.applyCommand("enter");

    }

    private void setup() {
        setUpVocabulary();
        Environment.tell("You have words!");

        setUpVariables();

        setUpDirections();
        Environment.tell("You have places!");

        setUpItems(location);
        Environment.tell("You have items!");
    }

    private void setUpVariables() {
        variableProvider.set(new Variable("wornRing", "false"));
    }

    private void setUpDirections() {
        Direction toPortal = new Direction("enter", portal, true);
        toPortal.addPreCondition(new EqualsCondition("wornRing", "true", variableProvider));
        location.addDirection(toPortal);
    }

    private void setUpMoveCommands(Item anItem) {
        GenericCommand command = new GenericCommand("take", new MoveAction(anItem, pocket));
        anItem.addCommand(command);
    }

    private void setUpItems(Location location) {
        Item knife = new Item(new DescriptionProvider(SMALL_TEXT, "knife"), true);
        knife.setShortDescription("small sharp knife");
        knife.setLongDescription("The knife is exceptionally sharp. Don't cut yourself!");
        setUpLookCommands(knife);
        setUpMoveCommands(knife);
        location.add(knife);

        Item rabbit = new Item(new DescriptionProvider(SMALL_TEXT, "rabbit"), true);
        rabbit.setLongDescription("The rabbit looks very tasty!");
        rabbit.addCommand(new GenericCommand("look", new DescribeAction(rabbit)));
        GenericCommand cut = new GenericCommand("cut", new MessageAction("You cut the rabbit to pieces."));
        rabbit.addCommand(cut);
        setUpLookCommands(rabbit);
        setUpMoveCommands(rabbit);
        location.add(rabbit);

        GenericCommand wear = new GenericCommand("wear", new MessageAction("You wear the ring."));
        wear.addFollowUpAction(new SetVariableAction("wornRing", "true", variableProvider));
        ring.addCommand(wear);
        setUpLookCommands(ring);
        setUpMoveCommands(ring);
        location.add(ring);

        setUpLookCommands(portal);
    }

    private void setUpLookCommands(Thing aThing) {
        aThing.addCommand(new GenericCommand("look", new DescribeAction(aThing)));
    }

    public MiniAdventure() {
        variableProvider = new VariableProvider();
        vocabulary = new Vocabulary();
        DescriptionProvider locationDescription = new DescriptionProvider("first", "location");
        locationDescription.setShortDescription("You find yourself in a very eerie location.");
        locationDescription.setLongDescription(
                """
                You find yourself in a very eerie location. Pitch black darkness stretches endlessly around you,
                robbing you of any sense of direction.
                Some strange mind must have created it. Suddenly, you notice a faint glowing portal!"""
        );
        location = new Location(locationDescription);
        location.addCommand(new GenericCommand("look", new DescribeAction(location)));

        DescriptionProvider portalDescription = new DescriptionProvider("fading", "portal");
        portalDescription.setShortDescription("a small portal.");
        portalDescription.setLongDescription("The portal slowly fades away already, it looks like it closes soon!");
        portal = new Location(portalDescription);

        // special items, not needed in real game
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 3);
        ring = new Item(new DescriptionProvider("golden", "ring"), true);

        new MessageAction("You enter the game.").execute();
    }

    private void setUpVocabulary() {
        vocabulary.addWord("desc", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("look", "desc");
        vocabulary.addSynonym("describe", "desc");

        vocabulary.addWord("knife", Vocabulary.WordType.NOUN);

        vocabulary.addWord("get", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("take", "get");

        vocabulary.addWord("open", Vocabulary.WordType.VERB);
        vocabulary.addWord("enter", Vocabulary.WordType.VERB);
        vocabulary.addWord("cut", Vocabulary.WordType.VERB);
        vocabulary.addWord("kill", Vocabulary.WordType.VERB);
        vocabulary.addWord("wear", Vocabulary.WordType.VERB);

        vocabulary.addWord("rabbit", Vocabulary.WordType.NOUN);
        vocabulary.addSynonym("hare", "rabbit");

        vocabulary.addWord("ring", Vocabulary.WordType.NOUN);

        vocabulary.addWord("big", Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord(SMALL_TEXT, Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord("golden", Vocabulary.WordType.ADJECTIVE);

        vocabulary.addWord("portal", Vocabulary.WordType.NOUN);
    }

}
