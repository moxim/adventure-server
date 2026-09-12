package com.pdg.adventure.server.tangible;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Wearable;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
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
            result = result + SystemMessageKey.SM10.defaultText();
        }
        int light = getLight();
        if (light > 0) {
            result = result + " " + SystemMessageKey.SM67.defaultText();;
            result = result + "\n" + SystemMessageKey.SM66.defaultText().formatted(getStrippedBasicDescription(), light);
        }
        return result;
    }

    @Override
    public String getLongDescription() {
        String result = super.getLongDescription();
        if (isWorn) {
            result = result + SystemMessageKey.SM10.defaultText();
        }
        int light = getLight();
        if (light > 0) {
            result = result + " " + SystemMessageKey.SM67.defaultText();;
            result = result + "\n" + SystemMessageKey.SM66.defaultText().formatted(getStrippedBasicDescription(), light);
        }
        return result;
    }

    @Override
    public String getEnrichedShortDescription() {
        String result = super.getEnrichedShortDescription();
        if (isWorn) {
            result = result + SystemMessageKey.SM10.defaultText();
        }
        int light = getLight();
        if (light > 0) {
            result = result + " " + SystemMessageKey.SM67.defaultText();;
            result = result + "\n" + SystemMessageKey.SM66.defaultText().formatted(getStrippedBasicDescription(), light);
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

    @Override
    public void setParentContainer(Container aParentContainer) {
        parentContainer = aParentContainer;
    }

    @Override
    public String toString() {
        return "Item{" +
               "isContainable=" + isContainable +
               ", parentContainer (Id)=" + (parentContainer == null ? "null" : parentContainer.getId()) +
               ", isWearable=" + isWearable +
               ", isWorn=" + isWorn +
               ", " + super.toString() +
               '}';
    }
}
