package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.DescriptionProvider;

import java.util.LinkedList;
import java.util.List;

public class GenericContainer extends Item implements Container {

    private final List<Containable> contents = new LinkedList<>();
    private int maxSize;
    private final boolean holdsDirections;

    public GenericContainer(DescriptionProvider aDescriptionProvider, int aMaxSize) {
        this(aDescriptionProvider, false, aMaxSize);
    }

    public GenericContainer(DescriptionProvider aDescriptionProvider,
                            boolean aFlagWhetherItHoldsDirections, int aMaxSize) {
        super(aDescriptionProvider, false);
        holdsDirections = aFlagWhetherItHoldsDirections;
        maxSize = aMaxSize;
    }

    @Override
    public List<Containable> getContents() {
        return contents;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void setMaxSize(int aMaxSize) {
        maxSize = aMaxSize;
    }

    @Override
    public int getSize() {
        return contents.size();
    }

    @Override
    public boolean contains(Containable aThing) {
        for (Containable containable : contents) {
            if (containable.getShortDescription().equals(aThing.getShortDescription())) {
                return true;
            }
        }
        return false;
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

    private String getDescription(Describable aThing) {
        if (holdsDirections) {
            return aThing.getShortDescription();
        }
        return aThing.getEnrichedShortDescription();
    }
    public static final String ALREADY_PRESENT_TEXT = "%s is already present in the %s.";
    public static final String CANNOT_PUT_TEXT = "You can't put the %s into the %s.";
    public static final String ALREADY_FULL_TEXT = " is already full.";

    @Override
    public ExecutionResult add(Containable anItem) {
        ExecutionResult result = new CommandExecutionResult();

        if (contents.contains(anItem)) {
            result.setResultMessage(String.format(ALREADY_PRESENT_TEXT, anItem, this));
        } else if (!anItem.isContainable()) {
            result.setResultMessage(String.format(CANNOT_PUT_TEXT, anItem, this));
        } else if (contents.size() == maxSize) {
            result.setResultMessage(String.format(ALREADY_FULL_TEXT, this));
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
        contents.remove(anItem);
        return result;
    }
}
