package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.support.DescriptionProvider;

import java.util.Objects;
import java.util.UUID;

public class Thing implements Describable {

    private final DescriptionProvider descriptionProvider;

    private final UUID id;

    public Thing(DescriptionProvider aDescriptionProvider) {
        descriptionProvider = aDescriptionProvider;
        id = UUID.randomUUID();
    }

    @Override
    public String getAdjective() {
        return descriptionProvider.getAdjective();
    }

    @Override
    public String getNoun() {
        return descriptionProvider.getNoun();
    }

    @Override
    public String getShortDescription() {
        return descriptionProvider.getShortDescription();
    }

    public void setShortDescription(String aShortDescription) {
        descriptionProvider.setShortDescription(aShortDescription);
    }

    @Override
    public String getLongDescription() {
        return descriptionProvider.getLongDescription();
    }

    public void setLongDescription(String aLongDescription) {
        descriptionProvider.setLongDescription(aLongDescription);
    }

    @Override
    public boolean equals(Object aO) {
        if (this == aO) return true;
        if (!(aO instanceof Thing thing)) return false;
        return id.equals(thing.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getShortDescription();
    }

}
