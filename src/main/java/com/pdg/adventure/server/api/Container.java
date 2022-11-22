package com.pdg.adventure.server.api;

import java.util.List;

public interface Container extends Describable {
    String ALREADY_PRESENT_TEXT = "%s is already present in the %s.";
    String CANNOT_PUT_TEXT = "You can't put the %s into the %s.";
    String ALREADY_FULL_TEXT = "%s is already full.";

    List<Containable> getContents();

    ExecutionResult add(Containable aThing);

    ExecutionResult remove(Containable aThing);

    void setMaxSize(int aMaxSize);

    int getMaxSize();

    int getSize();

    boolean contains(Containable aThing);

    boolean isEmpty();

    String listContents();

    Containable findItemByShortDescription(String anAdjective, String aNoun);
}
