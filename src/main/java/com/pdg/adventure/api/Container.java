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

    void setMaxSize(int aMaxSize);

    /** The most weight this container takes in; {@link Integer#MAX_VALUE} means unlimited, the default. */
    int getMaxWeight();

    void setMaxWeight(int aMaxWeight);

    /** The summed weight of the items in this container. */
    int getCarriedWeight();

    boolean isHoldingDirections();

    Containable findItemByShortDescription(String anAdjective, String aNoun);

}
