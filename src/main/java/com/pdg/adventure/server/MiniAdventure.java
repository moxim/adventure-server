package com.pdg.adventure.server;

import com.pdg.adventure.server.action.*;
import com.pdg.adventure.server.conditional.CarriedCondition;
import com.pdg.adventure.server.conditional.EqualsCondition;
import com.pdg.adventure.server.conditional.NotCondition;
import com.pdg.adventure.server.conditional.PresentCondition;
import com.pdg.adventure.server.engine.GameLoop;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.parser.DirectionCommand;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MiniAdventure {
    private final VariableProvider variableProvider;
    private final Item ring;
    private Location portal;
    private Location location;
    private final Vocabulary vocabulary;

    private static final String SMALL_TEXT = "small";

    public static void main(String[] args) {
        MiniAdventure game = new MiniAdventure(new Vocabulary());
        game.setup();
        game.run();
    }

    private void run() {
        GameLoop gameLoop = new GameLoop(new Parser(vocabulary));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        gameLoop.run(reader);
    }

    private void setup() {
        setUpVocabulary();
        Environment.tell("You have words!");

        setUpVariables();

        setUpLocations();
        setUpDirections();
        Environment.tell("You have places!");

        Environment.createPocket();
        Environment.setCurrentLocation(location);

        setUpItems(location);
        Environment.tell("You have items!");

        Environment.setUpWorkflows();
        CommandDescription inventoryCommandDescription = new CommandDescription("inventory");
        GenericCommand inventoryCommand = new GenericCommand("i", new InventoryAction());
        Environment.getWorkflow().addAlwaysCommand(inventoryCommandDescription, inventoryCommand);
    }

    private void setUpLocations() {
        DescriptionProvider locationDescription = new DescriptionProvider("first", "location");
        locationDescription.setShortDescription("This is the first location.");
        locationDescription.setLongDescription(
                """
                You find yourself in a field of lush grass and colourful flowers. It looks to beautiful to be true,
                much more like a painting. Some creative mind must have created it.
                Suddenly, you notice a faint glowing portal!"""
        );
        location = new Location(locationDescription);

        CommandDescription flowerCommandDescription = new CommandDescription("desc", "flower");
        GenericCommand checkFlowerCommand = new GenericCommand(flowerCommandDescription, new MessageAction("The flower looks " +
                "beautiful."));
        location.addCommand(checkFlowerCommand);

        DescriptionProvider portalDescription = new DescriptionProvider("fading", "portal");
        portalDescription.setShortDescription("You are in a small portal.");
        portalDescription.setLongDescription("The portal slowly fades away already, it looks like it closes soon!");
        portal = new Location(portalDescription);
    }

    private void setUpVariables() {
        variableProvider.set(new Variable("wornRing", "false"));
    }

    private void setUpDirections() {
        CommandDescription enterPortalCommandDescription = new CommandDescription("enter", portal);
        DirectionCommand enterCommand = new DirectionCommand(enterPortalCommandDescription,
                new MovePlayerAction(portal));
        enterCommand.addPreCondition(new EqualsCondition("wornRing", "true", variableProvider));

        GenericDirection toPortal = new GenericDirection(enterCommand, portal, true);

        setUpLookCommands(toPortal);
        location.addDirection(toPortal);

        CommandDescription leavePortalCommandDescription = new CommandDescription("leave", location);
        DirectionCommand leaveCommand = new DirectionCommand(leavePortalCommandDescription,
                new MovePlayerAction(location));
        GenericDirection toLocation = new GenericDirection(leaveCommand, location);
        portal.addDirection(toLocation);
    }

    private void setUpTakeCommands(Item anItem) {
        GenericCommand takeCommand = new GenericCommand("get", new TakeAction(anItem));
        takeCommand.addPreCondition(new PresentCondition(anItem));
        anItem.addCommand(takeCommand);
        GenericCommand dropCommand = new GenericCommand("drop", new DropAction(anItem));
        dropCommand.addPreCondition(new CarriedCondition(anItem));
        anItem.addCommand(dropCommand);
    }

    private void setUpItems(Location location) {
        Item knife = new Item(new DescriptionProvider(SMALL_TEXT, "knife"), true);
        knife.setShortDescription("a small knife");
        knife.setLongDescription("The knife is exceptionally sharp. Don't cut yourself!");
        setUpLookCommands(knife);
        setUpTakeCommands(knife);
        location.add(knife);

        Item pelt = new Item(new DescriptionProvider(SMALL_TEXT, "pelt"), true);
        pelt.setLongDescription("You may sell it to a trader.");
        setUpLookCommands(pelt);
        setUpTakeCommands(pelt);

        Item skinnedRabbit = new Item(new DescriptionProvider("skinned", "rabbit"), true);
        skinnedRabbit.setLongDescription("You may cook it.");
        setUpLookCommands(skinnedRabbit);
        setUpTakeCommands(skinnedRabbit);

        Item rabbit = new Item(new DescriptionProvider(SMALL_TEXT, "rabbit"), true);
        rabbit.setLongDescription("The rabbit looks very tasty!");
        GenericCommand cutNotSuccessfully = new GenericCommand("cut", new MessageAction("You need a knife."));
        CarriedCondition knifeCarried = new CarriedCondition(knife);
        NotCondition knifeNotCarried = new NotCondition(knifeCarried);
        cutNotSuccessfully.addPreCondition(knifeNotCarried);
        rabbit.addCommand(cutNotSuccessfully);

        GenericCommand cutSuccessfully = new GenericCommand("cut", new MessageAction("You skin the rabbit and are left " +
                "with a rabbit pelt."));
        cutSuccessfully.addPreCondition(knifeCarried);
        cutSuccessfully.addFollowUpAction(new DestroyAction(rabbit, location.getContainer()));
        cutSuccessfully.addFollowUpAction(new CreateAction(pelt, location.getContainer()));
        cutSuccessfully.addFollowUpAction(new CreateAction(skinnedRabbit, location.getContainer()));
        rabbit.addCommand(cutSuccessfully);

        setUpLookCommands(rabbit);
        setUpTakeCommands(rabbit);
        location.add(rabbit);

        GenericCommand wear = new GenericCommand("wear", new MessageAction("You wear the ring."));
        wear.addPreCondition(new CarriedCondition(ring));
        wear.addFollowUpAction(new SetVariableAction("wornRing", "true", variableProvider));
        ring.addCommand(wear);
        setUpLookCommands(ring);
        setUpTakeCommands(ring);
        location.add(ring);

        setUpLookCommands(location);
        setUpLookCommands(portal);
    }

    private void setUpLookCommands(Thing aThing) {
        aThing.addCommand(new GenericCommand("desc", new DescribeAction(aThing)));
    }

    public MiniAdventure(Vocabulary aVocabulary) {
        vocabulary = aVocabulary;
        variableProvider = new VariableProvider();

        // special items, not needed in real game
        DescriptionProvider ringDescription = new DescriptionProvider("golden", "ring");
        ringDescription.setLongDescription("As you inspect the ring you notice the shape of a portal engraved in it.");
        ring = new Item(ringDescription, true);

        new MessageAction("You enter the game.").execute();
    }

    private void setUpVocabulary() {
        vocabulary.addWord("quit", Vocabulary.WordType.VERB);
        vocabulary.addWord("save", Vocabulary.WordType.VERB);
        vocabulary.addWord("load", Vocabulary.WordType.VERB);
        vocabulary.addWord("inventory", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("i", "inventory");
        vocabulary.addWord("north", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("n", "north");
        vocabulary.addWord("east", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("e", "east");
        vocabulary.addWord("south", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("s", "south");
        vocabulary.addWord("west", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("w", "west");
        vocabulary.addWord("leave", Vocabulary.WordType.VERB);
        vocabulary.addWord("desc", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("look", "desc");
        vocabulary.addSynonym("l", "desc");
        vocabulary.addSynonym("describe", "desc");
        vocabulary.addSynonym("examine", "desc");
        vocabulary.addSynonym("inspect", "desc");

        vocabulary.addWord("knife", Vocabulary.WordType.NOUN);
        vocabulary.addWord("pelt", Vocabulary.WordType.NOUN);

        vocabulary.addWord("get", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("take", "get");
        vocabulary.addWord("drop", Vocabulary.WordType.VERB);

        vocabulary.addWord("open", Vocabulary.WordType.VERB);
        vocabulary.addWord("enter", Vocabulary.WordType.VERB);
        vocabulary.addWord("cut", Vocabulary.WordType.VERB);
        vocabulary.addWord("kill", Vocabulary.WordType.VERB);
        vocabulary.addWord("wear", Vocabulary.WordType.VERB);
        vocabulary.addWord("remove", Vocabulary.WordType.VERB);

        vocabulary.addWord("rabbit", Vocabulary.WordType.NOUN);
        vocabulary.addSynonym("hare", "rabbit");

        vocabulary.addWord("ring", Vocabulary.WordType.NOUN);
        vocabulary.addWord("flower", Vocabulary.WordType.NOUN);

        vocabulary.addWord("big", Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord("skinned", Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord(SMALL_TEXT, Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord("golden", Vocabulary.WordType.ADJECTIVE);

        vocabulary.addWord("portal", Vocabulary.WordType.NOUN);
    }

}
