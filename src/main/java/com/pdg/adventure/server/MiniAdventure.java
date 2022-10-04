package com.pdg.adventure.server;

import com.pdg.adventure.server.action.*;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import com.pdg.adventure.server.vocabulary.Word;

public class MiniAdventure {
    private final Vocabulary vocabulary;
    private final Container container;

    private static final String SMALL_TEXT = "small";

    public static void main(String[] args) {
        MiniAdventure game = new MiniAdventure();

        game.setup();

        new MoveAction(new Item(new DescriptionProvider("ring"), true),
                new GenericContainer(new DescriptionProvider("box"), 3)).execute();
    }

    private void setup() {
        setUpVocabulary();
        new MessageAction("You have words!").execute();
        setUpItems();
        new MessageAction("You have items!").execute();
    }

    private void setUpMoveCommands(Item anItem) {
        CommandDescription description = new CommandDescription("take", anItem);
        GenericCommand command = new GenericCommand(description, new MoveAction(anItem, container));
        anItem.addCommand(command);
    }

    private void setUpItems() {
        Item knife = new Item(new DescriptionProvider(SMALL_TEXT, "knife"), true);
        knife.setShortDescription("A small sharp knife.");
        knife.setLongDescription("The knife is exceptionally sharp. Don't cut yourself!");

        knife.addAction(new DescribeAction(knife));
        container.addItem(knife);

        Item rabbit = new Item(new DescriptionProvider(SMALL_TEXT, "rabbit"), true);
        rabbit.setLongDescription("The rabbit looks very tasty!");
        rabbit.addAction(new DescribeAction(rabbit));
        GenericCommand cut = new GenericCommand(new CommandDescription("cut",  rabbit), new MessageAction(
                "You cut the rabbit to pieces."));
        rabbit.addCommand(cut);

        container.addItem(rabbit);
    }

    public MiniAdventure() {
        vocabulary = new Vocabulary();
        container = new GenericContainer(new DescriptionProvider("MiniAdventure"), 5);
        new MessageAction("You enter the game.").execute();
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

        vocabulary.addWord("big", Word.WordType.ADJECTIVE);
        vocabulary.addWord(SMALL_TEXT, Word.WordType.ADJECTIVE);

        vocabulary.addWord("portal", Word.WordType.NOUN);
    }

}
