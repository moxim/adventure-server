package com.pdg.adventure.api;

public interface Wearable extends Containable {
    boolean isWearable();
    void setIsWearable(boolean isWearable);
    boolean isWorn();
    void setIsWorn(boolean isWorn);
}
