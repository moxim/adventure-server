package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.exception.AlreadyPresentException;
import com.pdg.adventure.server.exception.ContainerFullException;
import com.pdg.adventure.server.exception.NotContainableException;
import com.pdg.adventure.server.support.DescriptionProvider;

import java.util.LinkedList;
import java.util.List;

public class GenericContainer extends Item implements Container {

    private final List<Containable> contents = new LinkedList<>();
    private int maxSize;

    public GenericContainer(DescriptionProvider aDescriptionProvider, int aMaxSize) {
        super(aDescriptionProvider, false);
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
    public int getCurrentSize() {
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
    public void add(Containable anItem) {
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
        contents.add(anItem);
    }

    @Override
    public boolean remove(Containable anItem) {
        return contents.remove(anItem);
    }
}
