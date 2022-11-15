package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Wearable;
import com.pdg.adventure.server.support.DescriptionProvider;

public class Item extends Thing implements Containable, Wearable {
    private final boolean isContainable;
    private Container parentContainer;
    private boolean isWearable;
    private boolean isWorn;

    public Item(DescriptionProvider aDescriptionProvider, boolean isContainable) {
        super(aDescriptionProvider);
        this.isContainable = isContainable;
        this.isWearable = false;
        this.isWorn = false;
    }

    @Override
    public boolean isWearable() {
        return isWearable;
    }

    @Override
    public void setIsWearable(boolean isWearable) {
        this.isWearable = isWearable;
    }

    @Override
    public boolean isWorn() {
        return isWorn;
    }

    @Override
    public void setIsWorn(boolean isWorn) {
        if (isWorn) {
            isWearable = true;
        }
        this.isWorn = isWorn;
    }

    @Override
    public String getShortDescription() {
        String result = super.getShortDescription();
        if (isWorn) {
            result = result + " (worn)";
        }
        return result;
    }

    @Override
    public boolean isContainable() {
        return isContainable;
    }

    @Override
    public Container getParentContainer() {
        return parentContainer;
    }

    public void setParentContainer(Container aParentContainer) {
        parentContainer = aParentContainer;
    }
}
