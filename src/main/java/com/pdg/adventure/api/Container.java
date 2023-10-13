package com.pdg.adventure.api;

import java.util.List;

public interface Container extends Actionable {
    String ALREADY_PRESENT_TEXT = "%s is already present in the %s.";
    String CANNOT_PUT_TEXT = "You can't put the %s into the %s.";
    String ALREADY_FULL_TEXT = "%s is already full.";

    List<Containable> getContents();

    void setContents(List<Containable> aContainableList);

    ExecutionResult add(Containable aThing);

    ExecutionResult remove(Containable aThing);

    int getSize();

    boolean contains(Containable aThing);

    boolean isEmpty();

    String listContents();

    int getMaxSize();

    boolean isHoldingDirections();

    Containable findItemByShortDescription(String anAdjective, String aNoun);
}
