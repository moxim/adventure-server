package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.exceptions.AlreadyPresentException;
import com.pdg.adventure.server.exceptions.ContainerFullException;
import com.pdg.adventure.server.exceptions.NotContainableException;
import com.pdg.adventure.server.support.DescriptionProvider;

import java.util.LinkedList;
import java.util.List;

public class DefaultContainer extends Thing implements Container {

    private final List<Item> contents = new LinkedList<>();
    private int maxSize;

    public DefaultContainer(DescriptionProvider aDescriptionProvider, int aMaxSize) {
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
        contents.add(anItem);
    }

    @Override
    public boolean removeItem(Item anItem) {
        return contents.remove(anItem);
    }
}
