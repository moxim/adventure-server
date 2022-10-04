package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.exception.AlreadyPresentException;
import com.pdg.adventure.server.exception.ContainerFullException;
import com.pdg.adventure.server.exception.NotContainableException;
import com.pdg.adventure.server.support.DescriptionProvider;

import java.util.LinkedList;
import java.util.List;

public class GenericContainer extends Thing implements Container {

    private final List<Item> contents = new LinkedList<>();
    private int maxSize;

    public GenericContainer(DescriptionProvider aDescriptionProvider, int aMaxSize) {
        super(aDescriptionProvider);
        maxSize = aMaxSize;
    }

    @Override
    public List<Item> getContents() {
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
    public boolean contains(Describable aThing) {
        for (Containable containable : contents) {
            if (containable.getShortDescription().equals(aThing.getShortDescription())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addItem(Item anItem) {
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
    public boolean removeItem(Item anItem) {
        return contents.remove(anItem);
    }
}
