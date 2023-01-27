package com.pdg.adventure.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.pdg.adventure.api.*;
import com.pdg.adventure.server.action.*;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.condition.PresentCondition;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.engine.GameLoop;
import com.pdg.adventure.server.engine.MessageConsumer;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.DirectionCommand;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;
import com.pdg.adventure.server.vocabulary.Vocabulary;

// TODO
//  get rid of ugly casts
//  find them with \(\b[A-Z][A-Za-z0-9]*?\b\)

public class MiniAdventure {
    private final VariableProvider variableProvider;
    private Location portal;
    private Location location;
    private Location house;
    private Container pocket;

    private final Vocabulary vocabulary;
    private final Container itemHolder;

    private static final String SMALL_TEXT = "small";

    public static void main(String[] args) {
        MiniAdventure game = new MiniAdventure(new Vocabulary(),
                new GenericContainer(new DescriptionProvider("all items"), 99));
        game.setup();
        game.run();
    }

    public MiniAdventure(Vocabulary aVocabulary, Container aContainer) {
        vocabulary = aVocabulary;
        itemHolder = aContainer;
        variableProvider = new VariableProvider();
        new MessageAction("You enter the game.").execute();
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
        Environment.tell("You have variables!");

        setUpPocket();

        setUpItems();
        Environment.tell("You have items!");

        setUpLocations();
        setUpDirections();
        Environment.setPocket(pocket);
        Environment.setCurrentLocation(location);
        Environment.tell("You have places!");

        setUpItemsInFirstLocation(location);
        setUpItemsInPortal(portal);
        setUpItemsInHut(house);
        Environment.tell("You have items in places!");

        Environment.setUpWorkflows();
        setUpWorkflowCommands();

        // start on location "1"
        ExecutionResult result = new MovePlayerAction(location).execute();
        Environment.tell(result.getResultMessage());
    }

    private static void setUpWorkflowCommands() {
        GenericCommandDescription inventoryCommandDescription = new GenericCommandDescription("inventory");
        GenericCommand inventoryCommand = new GenericCommand(inventoryCommandDescription, new InventoryAction(
                new MessageConsumer(), new ContainerSupplier(Environment.getPocket())));
        Environment.getWorkflow().addInterceptorCommand(inventoryCommandDescription, inventoryCommand);

        GenericCommandDescription quitCommandDescription = new GenericCommandDescription("quit");
        GenericCommand quitCommand = new GenericCommand(quitCommandDescription, new QuitAction());
        Environment.getWorkflow().addInterceptorCommand(quitCommandDescription, quitCommand);

        Action lookLocationAction = new DescribeAction(() -> {
            Environment.getCurrentLocation().setHasBeenVisited(false);
            String  result = Environment.getCurrentLocation().getLongDescription();
            Environment.getCurrentLocation().setHasBeenVisited(true);
            return result;
        });
        GenericCommandDescription lookCommandDescription = new GenericCommandDescription("describe");
        GenericCommand lookCommand = new GenericCommand(lookCommandDescription, lookLocationAction);
        Environment.getWorkflow().addInterceptorCommand(lookCommandDescription, lookCommand);

        GenericCommandDescription lookCommandDescription2 = new GenericCommandDescription("describe", "here");
        GenericCommand lookCommand2 = new GenericCommand(lookCommandDescription2, lookLocationAction);
        Environment.getWorkflow().addInterceptorCommand(lookCommandDescription2, lookCommand2);

        GenericCommandDescription anyCommandDescription = new GenericCommandDescription("}", "}", "}");
        GenericCommand anyCommand = new GenericCommand(anyCommandDescription, new MessageAction("What now? > "));
        Environment.getWorkflow().addPreCommand(anyCommandDescription, anyCommand);
    }

    private void setUpLocations() {
        DescriptionProvider locationDescription = new DescriptionProvider("first", "location");
        locationDescription.setShortDescription("This is the first location.");
        locationDescription.setLongDescription(
                """
                You find yourself in a field of lush grass and colourful flowers surrounding a small hut. It looks too
                beautiful to be true, much more like a painting.
                Suddenly, you notice a glowing portal!"""
        );
        location = new Location(locationDescription, new ContainerSupplier(pocket));
        setUpLookCommands(location);

        GenericCommandDescription flowerCommandDescription = new GenericCommandDescription("describe", "flowers");
        GenericCommand checkFlowerCommand = new GenericCommand(flowerCommandDescription, new MessageAction("The flowers look " +
                "beautiful."));
        location.addCommand(checkFlowerCommand);

        DescriptionProvider portalDescription = new DescriptionProvider("fading", "portal");
        portalDescription.setShortDescription("You are in a small portal.");
        portalDescription.setLongDescription("The portal slowly fades away already, it looks like it closes soon!");
        portal = new Location(portalDescription, new ContainerSupplier(pocket));
        setUpLookCommands(portal);

        DescriptionProvider houseDescription = new DescriptionProvider(SMALL_TEXT, "hut");
        houseDescription.setShortDescription("You are in a small brick hut.");
        houseDescription.setLongDescription("This is a small hut made of bricks. There is nothing in here.");
        house = new Location(houseDescription, new ContainerSupplier(pocket));
        setUpLookCommands(house);
    }

    private void setUpPocket() {
        pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);
    }

    private void setUpVariables() {
        variableProvider.set(new Variable("wornRing", "false"));
    }

    private void setUpDirections() {
        Item ring = (Item) itemHolder.findItemByShortDescription("golden", "ring");

        GenericCommandDescription enterPortalCommandDescription = new GenericCommandDescription("enter", portal);
        DirectionCommand enterPortalCommand = new DirectionCommand(enterPortalCommandDescription, new MovePlayerAction(portal));
        enterPortalCommand.addPreCondition(new WornCondition(ring));

        Command enterCommand2 = new GenericCommand(enterPortalCommandDescription,
                new MessageAction("There seems to be an invisible barrier. You need some sort of key to enter the portal."));
        enterCommand2.addPreCondition(new NotCondition(new WornCondition(ring)));

        GenericDirection toPortal = new GenericDirection(enterCommand2, portal, true);
        toPortal.addCommand(enterPortalCommand);

        setUpLookCommands(toPortal);
        location.addDirection(toPortal);

        GenericCommandDescription enterHouseCommandDescription = new GenericCommandDescription("enter", house);
        DirectionCommand enterHouseCommand = new DirectionCommand(enterHouseCommandDescription, new MovePlayerAction(house));
        GenericDirection toHouse = new GenericDirection(enterHouseCommand, house, true);

        setUpLookCommands(toHouse);
        location.addDirection(toHouse);

        GenericCommandDescription leavePortalCommandDescription = new GenericCommandDescription("leave", location);
        DirectionCommand leaveCommand = new DirectionCommand(leavePortalCommandDescription, new MovePlayerAction(location));
        GenericDirection toLocation = new GenericDirection(leaveCommand, location);
        portal.addDirection(toLocation);

        GenericCommandDescription leaveHouseCommandDescription = new GenericCommandDescription("north", location);
        leaveCommand = new DirectionCommand(leaveHouseCommandDescription, new MovePlayerAction(location));
        toLocation = new GenericDirection(leaveCommand, location);
        house.addDirection(toLocation);
    }

    private void setUpTakeCommands(Item anItem) {
        GenericCommandDescription getCommandDescription = new GenericCommandDescription("get", anItem);
        GenericCommand takeFailCommand = new GenericCommand(getCommandDescription,
                new MessageAction(String.format("You already carry %s.", anItem.getEnrichedBasicDescription())));
        takeFailCommand.addPreCondition(new CarriedCondition(anItem));
        anItem.addCommand(takeFailCommand);

        GenericCommand takeCommand = new GenericCommand(getCommandDescription, new TakeAction(anItem));
        takeCommand.addPreCondition(new NotCondition(new CarriedCondition(anItem)));
        takeCommand.addPreCondition(new PresentCondition(anItem));
        anItem.addCommand(takeCommand);

        GenericCommandDescription dropCommandDescription = new GenericCommandDescription("drop", anItem);
        GenericCommand dropAndRemoveCommand = new GenericCommand(dropCommandDescription, new DropAction(anItem));
        PreCondition wornCondition = new WornCondition(anItem);
        dropAndRemoveCommand.addPreCondition(wornCondition);
        dropAndRemoveCommand.addFollowUpAction(new RemoveAction(anItem));
        anItem.addCommand(dropAndRemoveCommand);

        GenericCommand dropCommand = new GenericCommand(dropCommandDescription, new DropAction(anItem));
        dropCommand.addPreCondition(new NotCondition(wornCondition));
        dropCommand.addPreCondition(new CarriedCondition(anItem));
        anItem.addCommand(dropCommand);
    }

    private void setUpItems() {
        // gloves
        Item gloves = new Item(new DescriptionProvider("gloves"), true);
        itemHolder.add(gloves);
        gloves.setShortDescription("pair of leathery gloves");
        gloves.setLongDescription("These gloves would withstand much harsh treatment!");
        setUpWearCommands(gloves);
        setUpLookCommands(gloves);
        setUpTakeCommands(gloves);

        // knife
        Item knife = new Item(new DescriptionProvider(SMALL_TEXT, "knife"), true);
        itemHolder.add(knife);
        knife.setShortDescription("a small knife");
        knife.setLongDescription("The knife is exceptionally sharp. Don't cut yourself!");
        GenericCommand getNotSuccessful = new GenericCommand(new GenericCommandDescription("get", knife), new MessageAction(
                "The knife is too sharp! You need to wear some gloves."));
        PreCondition glovesWorn = new WornCondition((gloves));
        NotCondition glovesNotWorn = new NotCondition(glovesWorn);
        getNotSuccessful.addPreCondition(glovesNotWorn);
        knife.addCommand(getNotSuccessful);
        setUpLookCommands(knife);
        setUpTakeCommands(knife);

        // rabbit
        Item pelt = new Item(new DescriptionProvider(SMALL_TEXT, "pelt"), true);
        itemHolder.add(pelt);
        pelt.setLongDescription("You may sell it to a trader.");
        setUpLookCommands(pelt);
        setUpTakeCommands(pelt);

        Item skinnedRabbit = new Item(new DescriptionProvider("skinned", "rabbit"), true);
        itemHolder.add(skinnedRabbit);
        skinnedRabbit.setLongDescription("You may cook it.");
        setUpLookCommands(skinnedRabbit);
        setUpTakeCommands(skinnedRabbit);

        Item rabbit = new Item(new DescriptionProvider(SMALL_TEXT, "rabbit"), true);
        itemHolder.add(rabbit);
        rabbit.setLongDescription("The rabbit looks very tasty!");
        GenericCommand cutNotSuccessfully = new GenericCommand(new GenericCommandDescription("cut", rabbit),
                new MessageAction("You need a knife."));
        CarriedCondition knifeCarried = new CarriedCondition(knife);
        NotCondition knifeNotCarried = new NotCondition(knifeCarried);
        cutNotSuccessfully.addPreCondition(knifeNotCarried);
        rabbit.addCommand(cutNotSuccessfully);

        GenericCommand cutSuccessfully = new GenericCommand(new GenericCommandDescription("cut", rabbit), new MessageAction(
                "You skin the rabbit and are left with a rabbit pelt."));
        cutSuccessfully.addPreCondition(knifeCarried);
        cutSuccessfully.addFollowUpAction(new CreateAction(pelt, rabbit));
        cutSuccessfully.addFollowUpAction(new CreateAction(skinnedRabbit, rabbit));
        cutSuccessfully.addFollowUpAction(new DestroyAction(rabbit, rabbit));
        rabbit.addCommand(cutSuccessfully);
        setUpLookCommands(rabbit);

        // ring
        DescriptionProvider ringDescription = new DescriptionProvider("golden", "ring");
        ringDescription.setLongDescription("As you inspect the ring you notice the shape of a portal engraved in it.");
        Item ring = new Item(ringDescription, true);
        itemHolder.add(ring);
        setUpWearCommands(ring);
        setUpLookCommands(ring);
        setUpTakeCommands(ring);
    }

    private void setUpItemsInPortal(Location aLocation) {
        Item knife = (Item) itemHolder.findItemByShortDescription(SMALL_TEXT, "knife");
        aLocation.addItem(knife);
    }

    private void setUpItemsInHut(Location aLocation) {
        Item gloves = (Item) itemHolder.findItemByShortDescription("", "gloves");
        aLocation.addItem(gloves);
    }

    private void setUpItemsInFirstLocation(Location aLocation) {
        Item rabbit = (Item) itemHolder.findItemByShortDescription(SMALL_TEXT, "rabbit");
        aLocation.addItem(rabbit);

        Item ring = (Item) itemHolder.findItemByShortDescription("golden", "ring");
        aLocation.addItem(ring);
    }

    private void setUpWearCommands(Item anItem) {
        anItem.setIsWearable(true);
        GenericCommand wear = new GenericCommand(new GenericCommandDescription("wear", anItem), new WearAction(anItem));
        PreCondition carriedCondition = new CarriedCondition(anItem);
        wear.addPreCondition(carriedCondition);
        anItem.addCommand(wear);
        GenericCommand remove = new GenericCommand(new GenericCommandDescription("remove", anItem), new RemoveAction(anItem));
        remove.addPreCondition(carriedCondition);
        anItem.addCommand(remove);
    }

    private void setUpLookCommands(Thing aThing) {
        aThing.addCommand(new GenericCommand(new GenericCommandDescription("describe", aThing),
            new DescribeAction(aThing::getLongDescription)));
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
        vocabulary.addWord("describe", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("look", "describe");
        vocabulary.addSynonym("l", "describe");
        vocabulary.addSynonym("desc", "describe");
        vocabulary.addSynonym("examine", "describe");
        vocabulary.addSynonym("inspect", "describe");

        vocabulary.addWord("knife", Vocabulary.WordType.NOUN);
        vocabulary.addWord("pelt", Vocabulary.WordType.NOUN);

        vocabulary.addWord("get", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("take", "get");
        vocabulary.addWord("drop", Vocabulary.WordType.VERB);

        vocabulary.addWord("open", Vocabulary.WordType.VERB);
        vocabulary.addWord("enter", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("go", "enter");

        vocabulary.addWord("cut", Vocabulary.WordType.VERB);
        vocabulary.addSynonym("kill", "cut");
        vocabulary.addSynonym("stab", "cut");

        vocabulary.addWord("wear", Vocabulary.WordType.VERB);
        vocabulary.addWord("remove", Vocabulary.WordType.VERB);

        vocabulary.addWord("rabbit", Vocabulary.WordType.NOUN);
        vocabulary.addSynonym("hare", "rabbit");

        vocabulary.addWord("ring", Vocabulary.WordType.NOUN);
        vocabulary.addWord("flowers", Vocabulary.WordType.NOUN);

        vocabulary.addWord("big", Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord("skinned", Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord(SMALL_TEXT, Vocabulary.WordType.ADJECTIVE);
        vocabulary.addWord("golden", Vocabulary.WordType.ADJECTIVE);

        vocabulary.addWord("portal", Vocabulary.WordType.NOUN);
        vocabulary.addWord("hut", Vocabulary.WordType.NOUN);
        vocabulary.addSynonym("house", "hut");
        vocabulary.addWord("gloves", Vocabulary.WordType.NOUN);
        vocabulary.addWord("here", Vocabulary.WordType.NOUN);
    }
}
