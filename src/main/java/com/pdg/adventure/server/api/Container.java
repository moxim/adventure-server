package com.pdg.adventure.server.api;

import java.util.List;

public interface Container extends Describable {
    List<Containable> getContents();

    ExecutionResult add(Containable aThing);

    ExecutionResult remove(Containable aThing);

    void setMaxSize(int aMaxSize);

    int getMaxSize();

    int getSize();

    boolean contains(Containable aThing);

    boolean isEmpty();

    String listContents();
}
