package com.pdg.adventure.api;

import java.util.List;

public interface Container extends Actionable {

    List<Containable> getContents();

    void setContents(List<? extends Containable> aContainableList);

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
