package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.api.PreCondition;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.Item;

public class CarriedCondition implements PreCondition {

    private final Item item;

    public CarriedCondition(Item anItem) {
        item = anItem;
    }

    @Override
    public boolean isValid() {
        return Environment.getPocket().contains(item);
    }
}
