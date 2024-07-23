package com.pdg.adventure.server.tangible;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.DescriptionProvider;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GenericContainer extends Item implements Container {

    @Getter
    @Setter
    private int maxSize;

    private final boolean holdsDirections;
    private List<Containable> contents;

    public GenericContainer(DescriptionProvider aDescriptionProvider, int aMaxSize) {
        this(aDescriptionProvider, false, aMaxSize);
    }

    public GenericContainer(DescriptionProvider aDescriptionProvider,
                            boolean aFlagWhetherItHoldsDirections, int aMaxSize) {
        super(aDescriptionProvider, false);
        maxSize = aMaxSize;
        holdsDirections = aFlagWhetherItHoldsDirections;
        contents = new LinkedList<>();
    }

    @Override
    public List<Containable> getContents() {
        return new ArrayList<>(contents);
    }

    @Override
    public void setContents(List<Containable> aContainableList) {
        contents = new ArrayList<>(aContainableList);
    }

    @Override
    public int getSize() {
        return contents.size();
    }

    @Override
    public boolean contains(Containable aThing) {
        Containable thing = findItemByShortDescription(aThing.getAdjective(), aThing.getNoun());
        return thing != null;
    }

    @Override
    public Containable findItemByShortDescription(String anAdjective, String aNoun) {
        Containable result = null;
        for (Containable containable : contents) {
            if (containable.getNoun().equals(aNoun) &&
                    (VocabularyData.EMPTY_STRING.equals(anAdjective) || containable.getAdjective().equals(anAdjective))) {
                result = containable;
                break;
            }
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
    public String listContents() {
        StringBuilder sb = new StringBuilder();

        if (!isEmpty()) {
            for (int i = 0; i < contents.size() - 1; i++) {
                 sb.append(getDescription(contents.get(i))).append(", ").append(System.getProperty("line.separator"));
             }
             sb.append(getDescription(contents.get(contents.size() - 1))).append(".");
         } else {
            sb.append("nothing.");
        }

        return sb.toString();
    }

    @Override
    public boolean isHoldingDirections() {
        return holdsDirections;
    }

    private String getDescription(Describable aThing) {
        if (holdsDirections) {
            return aThing.getShortDescription();
        }
        return aThing.getEnrichedShortDescription();
    }

    @Override
    public ExecutionResult add(Containable anItem) {
        ExecutionResult result = new CommandExecutionResult();
        String itemDescription = anItem.getEnrichedBasicDescription();
        String containerDescription = getEnrichedBasicDescription();

        if (contents.contains(anItem)) {
            result.setResultMessage(String.format(ALREADY_PRESENT_TEXT, itemDescription, containerDescription));
        } else if (!anItem.isContainable()) {
            result.setResultMessage(String.format(CANNOT_PUT_TEXT, itemDescription, containerDescription));
        } else if (contents.size() == maxSize) {
            result.setResultMessage(String.format(ALREADY_FULL_TEXT, containerDescription));
        } else {
            anItem.setParentContainer(this);
            contents.add(anItem);
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }

    @Override
    public ExecutionResult remove(Containable anItem) {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        if (!contents.remove(anItem)) {
            result.setResultMessage(String.format("There is no %s in %s.", anItem.getShortDescription(),
                    getEnrichedBasicDescription()));
            result.setExecutionState(ExecutionResult.State.FAILURE);
        }
        return result;
    }

    @Override
    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        final List<Containable> items = ItemIdentifier.findItems(this, aCommandDescription);
        List<CommandChain> result = new ArrayList<>();
        for (Containable item : items) {
            final List<CommandChain> commandChain = item.getMatchingCommandChain(aCommandDescription);
                result.addAll(commandChain);
        }
        return result;
    }

    @Override
    public String toString() {
        return "GenericContainer{" +
                "maxSize=" + maxSize +
                ", holdsDirections=" + holdsDirections +
                ", contents=" + contents +
                ", " + super.toString() +
                '}';
    }
}
