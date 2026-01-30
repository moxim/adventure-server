package com.pdg.adventure;

import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.*;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.condition.PresentCondition;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.engine.*;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.location.GenericDirection;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;

// TODO
//  get rid of ugly casts
//  find them with \(\b[A-Z][A-Za-z0-9]*?\b\)

public class MiniAdventure {
    private static final String SMALL_TEXT = "small";
    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private Location portal;
    private Location location;
    private Location house;
    private Container pocket;
    private AdventureConfig adventureConfig;
    // these hold everything
    private Vocabulary allWords;
    private VariableProvider variableProvider;
    private MessagesHolder allMessages;
    private Container allItems;
    private Map<String, Container> allContainers;
    private Map<String, Location> allLocations;

    public MiniAdventure(AdventureConfig anAdventureConfig, final AdventureMapper anAdventureMapper,
                         AdventureService anAdventureService) {
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        useAdventureConfiguration(anAdventureConfig);
    }

    private void useAdventureConfiguration(final AdventureConfig anAdventureConfig) {
        adventureConfig = anAdventureConfig;
        allWords = adventureConfig.allWords();
        allContainers = adventureConfig.allContainers();
        allMessages = adventureConfig.allMessages();
        allLocations = adventureConfig.allLocations();
        allItems = new GenericContainer(new DescriptionProvider("all items"), 9999);
        variableProvider = adventureConfig.allVariables();
    }

    static void main(String[] args) {
        final MiniAdventure game = new MiniAdventure(new AdventureConfig(), null, null);
        game.setup();
        game.run();
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

        Workflow wf = Environment.setUpWorkflows();
        setUpWorkflowCommands(wf);

        // start on location "1"
        ExecutionResult result = new MovePlayerAction(location, allMessages).execute();
        Environment.tell(result.getResultMessage());
    }

    void run() {

        boolean keepRunning = true;
        while (keepRunning) {
            try {
                setUpMessages();
                System.out.println(new MessageAction(allMessages.getMessage("4"), allMessages).execute());

                Workflow wf = Environment.setUpWorkflows();
                createSpecialWords(allWords);
                setUpWorkflowCommands(wf);

                final GameLoop gameLoop = initializeGameLoop();

                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                gameLoop.run(reader);
                keepRunning = false; // Normal exit

            } catch (ReloadAdventureException e) {
                // we've just reloaded the adventure, so restart the game loop
                Environment.tell(e.getMessage());
            }
        }
    }

    private @NonNull GameLoop initializeGameLoop() {
        final var look = new DescribeAction(() -> {
            long timesVisited = 0;
            Environment.getCurrentLocation().setTimesVisited(timesVisited);
            String result = Environment.getCurrentLocation().getLongDescription();
            Environment.getCurrentLocation().setTimesVisited(++timesVisited);
            return result;
        }, allMessages).execute();
        Environment.tell(look.getResultMessage());

        GameLoop gameLoop = new GameLoop(new Parser(allWords));
        return gameLoop;
    }

    public void setUpMessages() {
        allMessages.addMessage("-13", "You already carry %s.");
        allMessages.addMessage("-12", "A %s appears in the %s.");
        allMessages.addMessage("-11", "The %s evaporates into thin air.");
        allMessages.addMessage("-10", "You carry:");
        allMessages.addMessage("-9", "You put %s into %s.");
        allMessages.addMessage("-8", "The %s is full.");
        allMessages.addMessage("-7", "You can't remove %s.");
        allMessages.addMessage("-6", "You can't wear %s.");

        allMessages.addMessage("-2", "I don't understand, please rephrase.");
        allMessages.addMessage("-1", "You can't do that.");
        allMessages.addMessage("0", "OK.");

        allMessages.addMessage("1", "As you inspect the ring you notice the shape of a portal engraved in it.");
        allMessages.addMessage("2", "You skin the rabbit and are left with a rabbit pelt.");
        allMessages.addMessage("3", "You need a knife.");
        allMessages.addMessage("4", "You enter the game.");
        allMessages.addMessage("5", "This is the first location.");
        allMessages.addMessage("6", """
                You find yourself in a field of lush grass and colourful flowers surrounding a small hut. It looks too
                beautiful to be true, much more like a painting.
                Suddenly, you notice a glowing portal!""");
        allMessages.addMessage("7", "The flowers look beautiful.");
        allMessages.addMessage("8", "You are in a small portal.");
        allMessages.addMessage("9", "The portal slowly fades away already, it looks like it closes soon!");
        allMessages.addMessage("10", "You are in a small brick hut.");
        allMessages.addMessage("11", "This is a small hut made of bricks. There is nothing in here.");
        allMessages.addMessage("12", "There seems to be an invisible barrier. You need some sort of key to enter " +
                                     "the portal.");
        allMessages.addMessage("14", "pair of leathery gloves");
        allMessages.addMessage("15", "These gloves would withstand any harsh treatment!");
        allMessages.addMessage("16", "a small knife");
        allMessages.addMessage("17", "The knife is exceptionally sharp. Don't cut yourself!");
        allMessages.addMessage("18", "The knife is too sharp! You need to wear some gloves.");
        allMessages.addMessage("19", "You may sell it to a trader, but that is part of a different adventure.");
        allMessages.addMessage("20", "You may cook it, but that is part of a different adventure");
        allMessages.addMessage("21", "The rabbit looks very tasty!");
        allMessages.addMessage("22", "The rabbit hops away.");
    }

    private void setUpVocabulary() {
        allWords.createNewWord("quit", Word.Type.VERB);
        allWords.createSynonym("exit", "quit");
        allWords.createSynonym("bye", "quit");
        allWords.createNewWord("save", Word.Type.VERB);
        allWords.createNewWord("load", Word.Type.VERB);
        allWords.createNewWord("inventory", Word.Type.VERB);
        allWords.createSynonym("i", "inventory");
        allWords.createNewWord("north", Word.Type.VERB);
        allWords.createSynonym("n", "north");
        allWords.createNewWord("east", Word.Type.VERB);
        allWords.createSynonym("e", "east");
        allWords.createNewWord("south", Word.Type.VERB);
        allWords.createSynonym("s", "south");
        allWords.createNewWord("west", Word.Type.VERB);
        allWords.createSynonym("w", "west");
        allWords.createNewWord("leave", Word.Type.VERB);
        allWords.createNewWord("describe", Word.Type.VERB);
        allWords.createSynonym("look", "describe");
        allWords.createSynonym("l", "describe");
        allWords.createSynonym("desc", "describe");
        allWords.createSynonym("examine", "describe");
        allWords.createSynonym("inspect", "describe");

        allWords.createNewWord("knife", Word.Type.NOUN);
        allWords.createNewWord("pelt", Word.Type.NOUN);

        allWords.createNewWord("get", Word.Type.VERB);
        allWords.createSynonym("take", "get");
        allWords.createNewWord("drop", Word.Type.VERB);

        allWords.createNewWord("open", Word.Type.VERB);
        allWords.createNewWord("enter", Word.Type.VERB);
        allWords.createSynonym("go", "enter");

        allWords.createNewWord("cut", Word.Type.VERB);
        allWords.createSynonym("kill", "cut");
        allWords.createSynonym("stab", "cut");

        allWords.createNewWord("wear", Word.Type.VERB);
        allWords.createNewWord("remove", Word.Type.VERB);

        allWords.createNewWord("rabbit", Word.Type.NOUN);
        allWords.createSynonym("hare", "rabbit");

        allWords.createNewWord("ring", Word.Type.NOUN);
        allWords.createNewWord("flowers", Word.Type.NOUN);

        allWords.createNewWord("big", Word.Type.ADJECTIVE);
        allWords.createNewWord("skinned", Word.Type.ADJECTIVE);
        allWords.createNewWord(SMALL_TEXT, Word.Type.ADJECTIVE);
        allWords.createNewWord("golden", Word.Type.ADJECTIVE);

        allWords.createNewWord("portal", Word.Type.NOUN);
        allWords.createNewWord("hut", Word.Type.NOUN);
        allWords.createSynonym("house", "hut");

        allWords.createNewWord("leathery", Word.Type.ADJECTIVE);
        allWords.createNewWord("gloves", Word.Type.NOUN);

        allWords.createNewWord("here", Word.Type.NOUN);

        allWords.createNewWord("help", Word.Type.VERB);
    }

    private void setUpVariables() {
        variableProvider.set(new Variable("wornRing", "false"));
    }

    private void setUpPocket() {
        pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);
    }

    private void setUpLocations() {
        DescriptionProvider locationDescription = new DescriptionProvider("first", "location");
        locationDescription.setShortDescription(allMessages.getMessage("5"));
        locationDescription.setLongDescription(allMessages.getMessage("6"));
        location = new Location(locationDescription, pocket);
        setUpLookCommands(location);

        GenericCommandDescription flowerCommandDescription = new GenericCommandDescription("describe", "flowers");
        GenericCommand checkFlowerCommand = new GenericCommand(flowerCommandDescription,
                                                               new MessageAction(allMessages.getMessage("7"),
                                                                                 allMessages));
        location.addCommand(checkFlowerCommand);

        DescriptionProvider portalDescription = new DescriptionProvider("fading", "portal");
        portalDescription.setShortDescription(allMessages.getMessage("8"));
        portalDescription.setLongDescription(allMessages.getMessage("9"));
        portal = new Location(portalDescription, pocket);
        setUpLookCommands(portal);

        DescriptionProvider houseDescription = new DescriptionProvider(SMALL_TEXT, "hut");
        houseDescription.setShortDescription(allMessages.getMessage("10"));
        houseDescription.setLongDescription(allMessages.getMessage("11"));
        house = new Location(houseDescription, pocket);
        setUpLookCommands(house);

        allLocations.put(location.getId(), location);
        allLocations.put(portal.getId(), portal);
        allLocations.put(house.getId(), house);
    }

    private void setUpItems() {
        // gloves
        Item gloves = new Item(new DescriptionProvider("gloves"), true);
        allItems.add(gloves);
        gloves.setShortDescription(allMessages.getMessage("14"));
        gloves.setLongDescription(allMessages.getMessage("15"));
        setUpWearCommands(gloves);
        setUpLookCommands(gloves);
        setUpTakeCommands(gloves);

        // knife
        Item knife = new Item(new DescriptionProvider(SMALL_TEXT, "knife"), true);
        allItems.add(knife);
        knife.setShortDescription(allMessages.getMessage("16"));
        knife.setLongDescription(allMessages.getMessage("17"));
        GenericCommand getNotSuccessful = new GenericCommand(new GenericCommandDescription("get", knife),
                                                             new MessageAction(allMessages.getMessage("18"),
                                                                               allMessages));
        PreCondition glovesWorn = new WornCondition((gloves));
        NotCondition glovesNotWorn = new NotCondition(glovesWorn);
        getNotSuccessful.addPreCondition(glovesNotWorn);
        knife.addCommand(getNotSuccessful);
        setUpLookCommands(knife);
        setUpTakeCommands(knife);

        // rabbit
        Item pelt = new Item(new DescriptionProvider(SMALL_TEXT, "pelt"), true);
        allItems.add(pelt);
        pelt.setLongDescription(allMessages.getMessage("19"));
        setUpLookCommands(pelt);
        setUpTakeCommands(pelt);

        Item skinnedRabbit = new Item(new DescriptionProvider("skinned", "rabbit"), true);
        allItems.add(skinnedRabbit);
        skinnedRabbit.setLongDescription(allMessages.getMessage("20"));
        setUpLookCommands(skinnedRabbit);
        setUpTakeCommands(skinnedRabbit);

        Item rabbit = new Item(new DescriptionProvider(SMALL_TEXT, "rabbit"), true);
        allItems.add(rabbit);
        rabbit.setLongDescription(allMessages.getMessage("21"));
        GenericCommand cutNotSuccessfully = new GenericCommand(new GenericCommandDescription("cut", rabbit),
                                                               new MessageAction(allMessages.getMessage("3"),
                                                                                 allMessages));
        CarriedCondition knifeCarried = new CarriedCondition(knife);
        NotCondition knifeNotCarried = new NotCondition(knifeCarried);
        cutNotSuccessfully.addPreCondition(knifeNotCarried);
        rabbit.addCommand(cutNotSuccessfully);

        GenericCommand cutSuccessfully = new GenericCommand(new GenericCommandDescription("cut", rabbit),
                                                            new MessageAction(allMessages.getMessage("2"),
                                                                              allMessages));
        cutSuccessfully.addPreCondition(knifeCarried);
        cutSuccessfully.addFollowUpAction(new CreateAction(pelt, rabbit::getParentContainer, allMessages));
        cutSuccessfully.addFollowUpAction(new CreateAction(skinnedRabbit, location::getItemContainer, allMessages));
        cutSuccessfully.addFollowUpAction(new DestroyAction(rabbit, allMessages));
        rabbit.addCommand(cutSuccessfully);
        setUpLookCommands(rabbit);
        setUpTakeCommands(rabbit, new MessageAction(allMessages.getMessage("22"), allMessages));

        // ring
        DescriptionProvider ringDescription = new DescriptionProvider("golden", "ring");
        ringDescription.setLongDescription(allMessages.getMessage("1"));
        Item ring = new Item(ringDescription, true);
        allItems.add(ring);
        setUpWearCommands(ring);
        setUpLookCommands(ring);
        setUpTakeCommands(ring);
    }

    private void setUpDirections() {
        Item ring = (Item) allItems.findItemByShortDescription("golden", "ring");

        GenericCommandDescription enterPortalCommandDescription = new GenericCommandDescription("enter", portal);
        GenericCommand enterPortalCommand = new GenericCommand(enterPortalCommandDescription,
                                                               new MovePlayerAction(portal, allMessages));
        enterPortalCommand.addPreCondition(new WornCondition(ring));

        Command enterCommand2 = new GenericCommand(enterPortalCommandDescription,
                                                   new MessageAction(allMessages.getMessage("12"), allMessages));
        enterCommand2.addPreCondition(new NotCondition(new WornCondition(ring)));

        GenericDirection toPortal = new GenericDirection(allLocations, enterCommand2, portal.getId(), true);
        toPortal.addCommand(enterPortalCommand);

        setUpLookCommands(toPortal);
        location.addDirection(toPortal);

        GenericCommandDescription enterHouseCommandDescription = new GenericCommandDescription("enter", house);
        GenericCommand enterHouseCommand = new GenericCommand(enterHouseCommandDescription,
                                                              new MovePlayerAction(house, allMessages));
        GenericDirection toHouse = new GenericDirection(allLocations, enterHouseCommand, house.getId(), true);

        setUpLookCommands(toHouse);
        location.addDirection(toHouse);

        GenericCommandDescription leavePortalCommandDescription = new GenericCommandDescription("leave", location);
        GenericCommand leaveCommand = new GenericCommand(leavePortalCommandDescription,
                                                         new MovePlayerAction(location, allMessages));
        GenericDirection toLocation = new GenericDirection(allLocations, leaveCommand, location.getId());
        portal.addDirection(toLocation);

        GenericCommandDescription leaveHouseCommandDescription = new GenericCommandDescription("north", location);
        leaveCommand = new GenericCommand(leaveHouseCommandDescription, new MovePlayerAction(location, allMessages));
        toLocation = new GenericDirection(allLocations, leaveCommand, location.getId());
        house.addDirection(toLocation);
    }

    private void setUpItemsInFirstLocation(Location aLocation) {
        Item rabbit = (Item) allItems.findItemByShortDescription(SMALL_TEXT, "rabbit");
        aLocation.addItem(rabbit);

        Item ring = (Item) allItems.findItemByShortDescription("golden", "ring");
        aLocation.addItem(ring);
    }

    private void setUpItemsInPortal(Location aLocation) {
        Item knife = (Item) allItems.findItemByShortDescription(SMALL_TEXT, "knife");
        aLocation.addItem(knife);
    }

    private void setUpItemsInHut(Location aLocation) {
        Item gloves = (Item) allItems.findItemByShortDescription("", "gloves");
        aLocation.addItem(gloves);
    }

    private void setUpWorkflowCommands(final Workflow aWorkflow) {
        GenericCommandDescription helpCommandDescription = new GenericCommandDescription("help");
        GenericCommand helpCommand = new GenericCommand(helpCommandDescription, new MessageAction("""
                                                                                                          Look around, examine items, take or drop items, maybe wear items, enter or leave locations.
                                                                                                          Or quit.""",
                                                                                                  allMessages));
        aWorkflow.addInterceptorCommand(helpCommandDescription, helpCommand);

        GenericCommandDescription inventoryCommandDescription = new GenericCommandDescription("inventory");
        GenericCommand inventoryCommand = new GenericCommand(inventoryCommandDescription,
                                                             new InventoryAction(new MessageConsumer(),
                                                                                 new ContainerSupplier(
                                                                                         Environment.getPocket()),
                                                                                 allMessages));
        aWorkflow.addInterceptorCommand(inventoryCommandDescription, inventoryCommand);

        GenericCommandDescription quitCommandDescription = new GenericCommandDescription("quit");
        GenericCommand quitCommand = new GenericCommand(quitCommandDescription, new QuitAction(allMessages));
        aWorkflow.addInterceptorCommand(quitCommandDescription, quitCommand);

        Action lookLocationAction = new DescribeAction(() -> {
            long timesVisited = 0;
            Environment.getCurrentLocation().setTimesVisited(timesVisited);
            String result = Environment.getCurrentLocation().getLongDescription();
            Environment.getCurrentLocation().setTimesVisited(timesVisited++);
            return result;
        }, allMessages);
        GenericCommandDescription lookCommandDescription = new GenericCommandDescription("describe");
        GenericCommand lookCommand = new GenericCommand(lookCommandDescription, lookLocationAction);
        aWorkflow.addInterceptorCommand(lookCommandDescription, lookCommand);

        GenericCommandDescription lookCommandDescription2 = new GenericCommandDescription("describe", "here");
        GenericCommand lookCommand2 = new GenericCommand(lookCommandDescription2, lookLocationAction);
        aWorkflow.addInterceptorCommand(lookCommandDescription2, lookCommand2);

        GenericCommandDescription anyCommandDescription = new GenericCommandDescription("}", "}", "}");
        GenericCommand anyCommand = new GenericCommand(anyCommandDescription,
                                                       new MessageAction("What now? > ", allMessages));
        aWorkflow.addPreCommand(anyCommandDescription, anyCommand);
    }

    private void createSpecialWords(final Vocabulary aVocabulary) {
        aVocabulary.createNewWord("quit", Word.Type.VERB);
        aVocabulary.createSynonym("exit", "quit");
        aVocabulary.createSynonym("bye", "quit");
        aVocabulary.createNewWord("describe", Word.Type.VERB);
        aVocabulary.createSynonym("look", "describe");
        aVocabulary.createSynonym("l", "describe");
        aVocabulary.createSynonym("desc", "describe");
        aVocabulary.createSynonym("examine", "describe");
        aVocabulary.createSynonym("x", "describe");
        aVocabulary.createNewWord("help", Word.Type.VERB);
        aVocabulary.createNewWord("inventory", Word.Type.VERB);
        aVocabulary.createSynonym("i", "inventory");
        aVocabulary.createNewWord("default", Word.Type.NOUN);

        addAdventureIdsToNouns();
    }

    private void setUpLookCommands(Actionable aThing) {
        aThing.addCommand(new GenericCommand(new GenericCommandDescription("describe", aThing),
                                             new DescribeAction(aThing::getLongDescription, allMessages)));
    }

    private void setUpWearCommands(Item anItem) {
        anItem.setIsWearable(true);
        GenericCommand wear = new GenericCommand(new GenericCommandDescription("wear", anItem),
                                                 new WearAction(anItem, allMessages));
        PreCondition carriedCondition = new CarriedCondition(anItem);
        wear.addPreCondition(carriedCondition);
        anItem.addCommand(wear);
        GenericCommand remove = new GenericCommand(new GenericCommandDescription("remove", anItem),
                                                   new RemoveAction(anItem, allMessages));
        remove.addPreCondition(carriedCondition);
        anItem.addCommand(remove);
    }

    private void setUpTakeCommands(Item anItem) {
        GenericCommandDescription getCommandDescription = new GenericCommandDescription("get", anItem);
        GenericCommand takeFailCommand = new GenericCommand(getCommandDescription, new MessageAction(
                String.format(allMessages.getMessage("-13"), anItem.getEnrichedBasicDescription()), allMessages));
        takeFailCommand.addPreCondition(new CarriedCondition(anItem));
        anItem.addCommand(takeFailCommand);

        GenericCommand takeCommand = new GenericCommand(getCommandDescription, new TakeAction(anItem,
                                                                                              new ContainerSupplier(
                                                                                                      Environment.getPocket()),
                                                                                              allMessages));
        takeCommand.addPreCondition(new NotCondition(new CarriedCondition(anItem)));
        takeCommand.addPreCondition(new PresentCondition(anItem));
        anItem.addCommand(takeCommand);

        GenericCommandDescription dropCommandDescription = new GenericCommandDescription("drop", anItem);
        GenericCommand dropAndRemoveCommand = new GenericCommand(dropCommandDescription, new DropAction(anItem,
                                                                                                        new ContainerSupplier(
                                                                                                                Environment.getCurrentLocation()
                                                                                                                           .getItemContainer()),
                                                                                                        allMessages));
        PreCondition wornCondition = new WornCondition(anItem);
        dropAndRemoveCommand.addPreCondition(wornCondition);
        dropAndRemoveCommand.addFollowUpAction(new RemoveAction(anItem, allMessages));
        anItem.addCommand(dropAndRemoveCommand);

        GenericCommand dropCommand = new GenericCommand(dropCommandDescription, new DropAction(anItem,
                                                                                               new ContainerSupplier(
                                                                                                       Environment.getCurrentLocation()
                                                                                                                  .getItemContainer()),
                                                                                               allMessages));
        dropCommand.addPreCondition(new NotCondition(wornCondition));
        dropCommand.addPreCondition(new CarriedCondition(anItem));
        anItem.addCommand(dropCommand);
    }

    private void setUpTakeCommands(Item anItem, MessageAction aMessageAction) {
        GenericCommandDescription getCommandDescription = new GenericCommandDescription("get", anItem);
        GenericCommand takeFailCommand = new GenericCommand(getCommandDescription, aMessageAction);
        anItem.addCommand(takeFailCommand);
    }

    private void addAdventureIdsToNouns() {
        final List<AdventureData> anAdventureDataList = adventureService.getAdventures();
        for (AdventureData adventureData : anAdventureDataList) {
            String adventureId = adventureData.getId();
            allWords.createNewWord(adventureId, Word.Type.NOUN);
            addLoadAdventureToWorkflowCommands(adventureId);
        }
        addLoadAdventureToWorkflowCommands("");
    }

    private void addLoadAdventureToWorkflowCommands(final String aAdventureId) {
        var loadAdventureCommandDescription = new GenericCommandDescription("load", aAdventureId);
        final var loadAdventureAction = new LoadAdventureAction(adventureService, adventureMapper, adventureConfig,
                                                                allMessages);
        loadAdventureAction.setAdventureId(aAdventureId);
        var loadAdventureCommand = new GenericCommand(loadAdventureCommandDescription, loadAdventureAction);
        Environment.getWorkflow().addInterceptorCommand(loadAdventureCommandDescription, loadAdventureCommand);
    }

    public void setLocations(List<Location> locations) {
        for (Location location : locations) {
            allLocations.put(location.getId(), location);
        }
    }
}
