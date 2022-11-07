package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.exception.AlreadyPresentException;
import com.pdg.adventure.server.exception.ContainerFullException;
import com.pdg.adventure.server.exception.NotContainableException;
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

    @Override
    public boolean add(Containable anItem) {
        // TODO:
        //   wrap these exceptions in an error type
        if (contents.contains(anItem)) {
            throw new AlreadyPresentException(anItem, this);
        }
        if (!anItem.isContainable()) {
            throw new NotContainableException(anItem, this);
        }
        if (contents.size() == maxSize) {
            throw new ContainerFullException(this);
        }
        anItem.setParentContainer(this);
        return contents.add(anItem);
    }

    @Override
    public boolean remove(Containable anItem) {
        return contents.remove(anItem);
    }
}
