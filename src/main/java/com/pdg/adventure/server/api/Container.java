package com.pdg.adventure.server.api;

import com.pdg.adventure.server.tangible.Item;

import java.util.List;

public interface Container extends Describable {
    List<Item> getContents();

    void addItem(Item anItem);

    boolean removeItem(Item anItem);

    void setMaxSize(int aMaxSize);

    int getMaxSize();

    int getCurrentSize();
}
