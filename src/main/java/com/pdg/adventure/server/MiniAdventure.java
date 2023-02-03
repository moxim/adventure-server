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
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import com.pdg.adventure.server.vocabulary.Word;

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
    private final MessagesHolder messageHolder;
    private final Container itemHolder;

    private static final String SMALL_TEXT = "small";

    public static void main(String[] args) {
        MiniAdventure game = new MiniAdventure(new Vocabulary(), new MessagesHolder(),
                                               new GenericContainer(new DescriptionProvider("all items"), 9999));
        game.setup();
        game.run();
    }

    public MiniAdventure(Vocabulary aVocabulary, MessagesHolder aMessageHolder, Container aContainer) {
        vocabulary = aVocabulary;
        messageHolder = aMessageHolder;
        itemHolder = aContainer;
        variableProvider = new VariableProvider();
        new MessageAction(messageHolder.getMessage("4"), messageHolder).execute();
    }

    private void run() {
        GameLoop gameLoop = new GameLoop(new Parser(vocabulary));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        gameLoop.run(reader);
    }

    private void setup() {
        setUpMessages();
        setUpVocabulary();
        Environment.tell("You have words!");

        setUpVariables();
        Environment.tell("You have variables!");

        setUpPocket();
        Environment.setPocket(pocket);

        setUpLocations();
        Environment.setCurrentLocation(location);
        Environment.tell("You have places!");

        setUpItems();
        Environment.tell("You have items!");

        setUpDirections();

        setUpItemsInFirstLocation(location);
        setUpItemsInPortal(portal);
        setUpItemsInHut(house);
        Environment.tell("You have items in places!");

        Environment.setUpWorkflows();
        setUpWorkflowCommands();

        // start on location "1"
        ExecutionResult result =
                new MovePlayerAction(location, Environment::setCurrentLocation, messageHolder).execute();
        Environment.tell(result.getResultMessage());
    }

    private void setUpWorkflowCommands() {
        GenericCommandDescription inventoryCommandDescription = new GenericCommandDescription("inventory");
        GenericCommand inventoryCommand = new GenericCommand(inventoryCommandDescription,
                                                             new InventoryAction(new MessageConsumer(),
                                                                                 new ContainerSupplier(
                                                                                         Environment.getPocket()),
                                                                                 messageHolder));
        Environment.getWorkflow().addInterceptorCommand(inventoryCommandDescription, inventoryCommand);

        GenericCommandDescription quitCommandDescription = new GenericCommandDescription("quit");
        GenericCommand quitCommand = new GenericCommand(quitCommandDescription, new QuitAction(messageHolder));
        Environment.getWorkflow().addInterceptorCommand(quitCommandDescription, quitCommand);

        Action lookLocationAction = new DescribeAction(() -> {
            Environment.getCurrentLocation().setHasBeenVisited(false);
            String result = Environment.getCurrentLocation().getLongDescription();
            Environment.getCurrentLocation().setHasBeenVisited(true);
            return result;
        }, messageHolder);
        GenericCommandDescription lookCommandDescription = new GenericCommandDescription("describe");
        GenericCommand lookCommand = new GenericCommand(lookCommandDescription, lookLocationAction);
        Environment.getWorkflow().addInterceptorCommand(lookCommandDescription, lookCommand);

        GenericCommandDescription lookCommandDescription2 = new GenericCommandDescription("describe", "here");
        GenericCommand lookCommand2 = new GenericCommand(lookCommandDescription2, lookLocationAction);
        Environment.getWorkflow().addInterceptorCommand(lookCommandDescription2, lookCommand2);

        GenericCommandDescription anyCommandDescription = new GenericCommandDescription("}", "}", "}");
        GenericCommand anyCommand =
                new GenericCommand(anyCommandDescription, new MessageAction("What now? > ", messageHolder));
        Environment.getWorkflow().addPreCommand(anyCommandDescription, anyCommand);
    }

    private void setUpLocations() {
        DescriptionProvider locationDescription = new DescriptionProvider("first", "location");
        locationDescription.setShortDescription(messageHolder.getMessage("5"));
        locationDescription.setLongDescription(messageHolder.getMessage("6"));
        location = new Location(locationDescription, new ContainerSupplier(pocket));
        setUpLookCommands(location);

        GenericCommandDescription flowerCommandDescription = new GenericCommandDescription("describe", "flowers");
        GenericCommand checkFlowerCommand = new GenericCommand(flowerCommandDescription,
                                                               new MessageAction(messageHolder.getMessage("7"),
                                                                                 messageHolder));
        location.addCommand(checkFlowerCommand);

        DescriptionProvider portalDescription = new DescriptionProvider("fading", "portal");
        portalDescription.setShortDescription(messageHolder.getMessage("8"));
        portalDescription.setLongDescription(messageHolder.getMessage("9"));
        portal = new Location(portalDescription, new ContainerSupplier(pocket));
        setUpLookCommands(portal);

        DescriptionProvider houseDescription = new DescriptionProvider(SMALL_TEXT, "hut");
        houseDescription.setShortDescription(messageHolder.getMessage("10"));
        houseDescription.setLongDescription(messageHolder.getMessage("11"));
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
        DirectionCommand enterPortalCommand = new DirectionCommand(enterPortalCommandDescription,
                                                                   new MovePlayerAction(portal,
                                                                                        Environment::setCurrentLocation,
                                                                                        messageHolder));
        enterPortalCommand.addPreCondition(new WornCondition(ring));

        Command enterCommand2 = new GenericCommand(enterPortalCommandDescription,
                                                   new MessageAction(messageHolder.getMessage("12"), messageHolder));
        enterCommand2.addPreCondition(new NotCondition(new WornCondition(ring)));

        GenericDirection toPortal = new GenericDirection(enterCommand2, portal, true);
        toPortal.addCommand(enterPortalCommand);

        setUpLookCommands(toPortal);
        location.addDirection(toPortal);

        GenericCommandDescription enterHouseCommandDescription = new GenericCommandDescription("enter", house);
        DirectionCommand enterHouseCommand = new DirectionCommand(enterHouseCommandDescription,
                                                                  new MovePlayerAction(house,
                                                                                       Environment::setCurrentLocation,
                                                                                       messageHolder));
        GenericDirection toHouse = new GenericDirection(enterHouseCommand, house, true);

        setUpLookCommands(toHouse);
        location.addDirection(toHouse);

        GenericCommandDescription leavePortalCommandDescription = new GenericCommandDescription("leave", location);
        DirectionCommand leaveCommand = new DirectionCommand(leavePortalCommandDescription,
                                                             new MovePlayerAction(location,
                                                                                  Environment::setCurrentLocation,
                                                                                  messageHolder));
        GenericDirection toLocation = new GenericDirection(leaveCommand, location);
        portal.addDirection(toLocation);

        GenericCommandDescription leaveHouseCommandDescription = new GenericCommandDescription("north", location);
        leaveCommand = new DirectionCommand(leaveHouseCommandDescription,
                                            new MovePlayerAction(location, Environment::setCurrentLocation,
                                                                 messageHolder));
        toLocation = new GenericDirection(leaveCommand, location);
        house.addDirection(toLocation);
    }

    private void setUpTakeCommands(Item anItem) {
        GenericCommandDescription getCommandDescription = new GenericCommandDescription("get", anItem);
        GenericCommand takeFailCommand = new GenericCommand(getCommandDescription, new MessageAction(
                String.format(messageHolder.getMessage("-13"), anItem.getEnrichedBasicDescription()), messageHolder));
        takeFailCommand.addPreCondition(new CarriedCondition(anItem));
        anItem.addCommand(takeFailCommand);

        GenericCommand takeCommand = new GenericCommand(getCommandDescription, new TakeAction(anItem,
                                                                                              new ContainerSupplier(
                                                                                                      Environment.getCurrentLocation()
                                                                                                                 .getContainer()),
                                                                                              messageHolder));
        takeCommand.addPreCondition(new NotCondition(new CarriedCondition(anItem)));
        takeCommand.addPreCondition(new PresentCondition(anItem));
        anItem.addCommand(takeCommand);

        GenericCommandDescription dropCommandDescription = new GenericCommandDescription("drop", anItem);
        GenericCommand dropAndRemoveCommand = new GenericCommand(dropCommandDescription, new DropAction(anItem,
                                                                                                        new ContainerSupplier(
                                                                                                                Environment.getCurrentLocation()
                                                                                                                           .getContainer()),
                                                                                                        messageHolder));
        PreCondition wornCondition = new WornCondition(anItem);
        dropAndRemoveCommand.addPreCondition(wornCondition);
        dropAndRemoveCommand.addFollowUpAction(new RemoveAction(anItem, messageHolder));
        anItem.addCommand(dropAndRemoveCommand);

        GenericCommand dropCommand = new GenericCommand(dropCommandDescription, new DropAction(anItem,
                                                                                               new ContainerSupplier(
                                                                                                       Environment.getCurrentLocation()
                                                                                                                  .getContainer()),
                                                                                               messageHolder));
        dropCommand.addPreCondition(new NotCondition(wornCondition));
        dropCommand.addPreCondition(new CarriedCondition(anItem));
        anItem.addCommand(dropCommand);
    }

    private void setUpItems() {
        // gloves
        Item gloves = new Item(new DescriptionProvider("gloves"), true);
        itemHolder.add(gloves);
        gloves.setShortDescription(messageHolder.getMessage("14"));
        gloves.setLongDescription(messageHolder.getMessage("15"));
        setUpWearCommands(gloves);
        setUpLookCommands(gloves);
        setUpTakeCommands(gloves);

        // knife
        Item knife = new Item(new DescriptionProvider(SMALL_TEXT, "knife"), true);
        itemHolder.add(knife);
        knife.setShortDescription(messageHolder.getMessage("16"));
        knife.setLongDescription(messageHolder.getMessage("17"));
        GenericCommand getNotSuccessful = new GenericCommand(new GenericCommandDescription("get", knife),
                                                             new MessageAction(messageHolder.getMessage("18"),
                                                                               messageHolder));
        PreCondition glovesWorn = new WornCondition((gloves));
        NotCondition glovesNotWorn = new NotCondition(glovesWorn);
        getNotSuccessful.addPreCondition(glovesNotWorn);
        knife.addCommand(getNotSuccessful);
        setUpLookCommands(knife);
        setUpTakeCommands(knife);

        // rabbit
        Item pelt = new Item(new DescriptionProvider(SMALL_TEXT, "pelt"), true);
        itemHolder.add(pelt);
        pelt.setLongDescription(messageHolder.getMessage("19"));
        setUpLookCommands(pelt);
        setUpTakeCommands(pelt);

        Item skinnedRabbit = new Item(new DescriptionProvider("skinned", "rabbit"), true);
        itemHolder.add(skinnedRabbit);
        skinnedRabbit.setLongDescription(messageHolder.getMessage("20"));
        setUpLookCommands(skinnedRabbit);
        setUpTakeCommands(skinnedRabbit);

        Item rabbit = new Item(new DescriptionProvider(SMALL_TEXT, "rabbit"), true);
        itemHolder.add(rabbit);
        rabbit.setLongDescription(messageHolder.getMessage("21"));
        GenericCommand cutNotSuccessfully = new GenericCommand(new GenericCommandDescription("cut", rabbit),
                                                               new MessageAction(messageHolder.getMessage("3"),
                                                                                 messageHolder));
        CarriedCondition knifeCarried = new CarriedCondition(knife);
        NotCondition knifeNotCarried = new NotCondition(knifeCarried);
        cutNotSuccessfully.addPreCondition(knifeNotCarried);
        rabbit.addCommand(cutNotSuccessfully);

        GenericCommand cutSuccessfully = new GenericCommand(new GenericCommandDescription("cut", rabbit),
                                                            new MessageAction(messageHolder.getMessage("2"),
                                                                              messageHolder));
        cutSuccessfully.addPreCondition(knifeCarried);
        cutSuccessfully.addFollowUpAction(new CreateAction(pelt, rabbit::getParentContainer, messageHolder));
        cutSuccessfully.addFollowUpAction(new CreateAction(skinnedRabbit, location::getContainer, messageHolder));
        cutSuccessfully.addFollowUpAction(new DestroyAction(rabbit, messageHolder));
        rabbit.addCommand(cutSuccessfully);
        setUpLookCommands(rabbit);

        // ring
        DescriptionProvider ringDescription = new DescriptionProvider("golden", "ring");
        ringDescription.setLongDescription(messageHolder.getMessage("1"));
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
        GenericCommand wear = new GenericCommand(new GenericCommandDescription("wear", anItem),
                                                 new WearAction(anItem, messageHolder));
        PreCondition carriedCondition = new CarriedCondition(anItem);
        wear.addPreCondition(carriedCondition);
        anItem.addCommand(wear);
        GenericCommand remove = new GenericCommand(new GenericCommandDescription("remove", anItem),
                                                   new RemoveAction(anItem, messageHolder));
        remove.addPreCondition(carriedCondition);
        anItem.addCommand(remove);
    }

    private void setUpLookCommands(Thing aThing) {
        aThing.addCommand(new GenericCommand(new GenericCommandDescription("describe", aThing),
                                             new DescribeAction(aThing::getLongDescription, messageHolder)));
    }

    private void setUpMessages() {
        messageHolder.addMessage("-13", "You already carry %s.");
        messageHolder.addMessage("-12", "A %s appears in the %s.");
        messageHolder.addMessage("-11", "The %s evaporates into thin air.");
        messageHolder.addMessage("-10", "You carry:");
        messageHolder.addMessage("-9", "You put %s into %s.");
        messageHolder.addMessage("-8", "The %s is full.");
        messageHolder.addMessage("-7", "You can't remove %s.");
        messageHolder.addMessage("-6", "You can't wear %s.");

        messageHolder.addMessage("1", "As you inspect the ring you notice the shape of a portal engraved in it.");
        messageHolder.addMessage("2", "You skin the rabbit and are left with a rabbit pelt.");
        messageHolder.addMessage("3", "You need a knife.");
        messageHolder.addMessage("4", "You enter the game.");
        messageHolder.addMessage("5", "This is the first location.");
        messageHolder.addMessage("6", """
                You find yourself in a field of lush grass and colourful flowers surrounding a small hut. It looks too
                beautiful to be true, much more like a painting.
                Suddenly, you notice a glowing portal!""");
        messageHolder.addMessage("7", "The flowers look beautiful.");
        messageHolder.addMessage("8", "You are in a small portal.");
        messageHolder.addMessage("9", "The portal slowly fades away already, it looks like it closes soon!");
        messageHolder.addMessage("10", "You are in a small brick hut.");
        messageHolder.addMessage("11", "This is a small hut made of bricks. There is nothing in here.");
        messageHolder.addMessage("12",
                                 "There seems to be an invisible barrier. You need some sort of key to enter " + "the portal.");
        messageHolder.addMessage("14", "pair of leathery gloves");
        messageHolder.addMessage("15", "These gloves would withstand any harsh treatment!");
        messageHolder.addMessage("16", "a small knife");
        messageHolder.addMessage("17", "The knife is exceptionally sharp. Don't cut yourself!");
        messageHolder.addMessage("18", "The knife is too sharp! You need to wear some gloves.");
        messageHolder.addMessage("19", "You may sell it to a trader.");
        messageHolder.addMessage("20", "You may cook it.");
        messageHolder.addMessage("21", "The rabbit looks very tasty!");
    }

    private void setUpVocabulary() {
        vocabulary.addWord("quit", Word.Type.VERB);
        vocabulary.addWord("save", Word.Type.VERB);
        vocabulary.addWord("load", Word.Type.VERB);
        vocabulary.addWord("inventory", Word.Type.VERB);
        vocabulary.addSynonym("i", "inventory");
        vocabulary.addWord("north", Word.Type.VERB);
        vocabulary.addSynonym("n", "north");
        vocabulary.addWord("east", Word.Type.VERB);
        vocabulary.addSynonym("e", "east");
        vocabulary.addWord("south", Word.Type.VERB);
        vocabulary.addSynonym("s", "south");
        vocabulary.addWord("west", Word.Type.VERB);
        vocabulary.addSynonym("w", "west");
        vocabulary.addWord("leave", Word.Type.VERB);
        vocabulary.addWord("describe", Word.Type.VERB);
        vocabulary.addSynonym("look", "describe");
        vocabulary.addSynonym("l", "describe");
        vocabulary.addSynonym("desc", "describe");
        vocabulary.addSynonym("examine", "describe");
        vocabulary.addSynonym("inspect", "describe");

        vocabulary.addWord("knife", Word.Type.NOUN);
        vocabulary.addWord("pelt", Word.Type.NOUN);

        vocabulary.addWord("get", Word.Type.VERB);
        vocabulary.addSynonym("take", "get");
        vocabulary.addWord("drop", Word.Type.VERB);

        vocabulary.addWord("open", Word.Type.VERB);
        vocabulary.addWord("enter", Word.Type.VERB);
        vocabulary.addSynonym("go", "enter");

        vocabulary.addWord("cut", Word.Type.VERB);
        vocabulary.addSynonym("kill", "cut");
        vocabulary.addSynonym("stab", "cut");

        vocabulary.addWord("wear", Word.Type.VERB);
        vocabulary.addWord("remove", Word.Type.VERB);

        vocabulary.addWord("rabbit", Word.Type.NOUN);
        vocabulary.addSynonym("hare", "rabbit");

        vocabulary.addWord("ring", Word.Type.NOUN);
        vocabulary.addWord("flowers", Word.Type.NOUN);

        vocabulary.addWord("big", Word.Type.ADJECTIVE);
        vocabulary.addWord("skinned", Word.Type.ADJECTIVE);
        vocabulary.addWord(SMALL_TEXT, Word.Type.ADJECTIVE);
        vocabulary.addWord("golden", Word.Type.ADJECTIVE);

        vocabulary.addWord("portal", Word.Type.NOUN);
        vocabulary.addWord("hut", Word.Type.NOUN);
        vocabulary.addSynonym("house", "hut");
        vocabulary.addWord("gloves", Word.Type.NOUN);
        vocabulary.addWord("here", Word.Type.NOUN);
    }
}
