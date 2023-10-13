package com.pdg.adventure.views.support;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.LocationData;

public class ViewSupporter {

    public static String formatId(Ided anIdedData) {
        return anIdedData.getId().substring(0, 8);
    }

    public static String formatDescription(Describable aDescribable) {
        String shortDescription = aDescribable.getShortDescription();
        if (shortDescription.isEmpty()) {
            shortDescription = aDescribable.getNoun() + " / " + aDescribable.getAdjective();
        }
        return shortDescription;
    }

    public static String formatDescription(LocationData aLocationData) {
        String result = aLocationData.getDescriptionData().getShortDescription();
        if (result.isEmpty()) {
            result = aLocationData.getDescriptionData().getNoun() + " / " + aLocationData.getDescriptionData().getAdjective();
        }
        return result;
    }
}
