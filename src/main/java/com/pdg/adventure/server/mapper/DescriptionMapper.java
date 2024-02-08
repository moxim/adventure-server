package com.pdg.adventure.server.mapper;

import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.support.DescriptionProvider;

public abstract class DescriptionMapper {
    private DescriptionMapper() {
        // don't instantiate me
    }

    public static DescriptionData mapToDO(DescriptionProvider aDescriptionProvider) {
//        DescriptionData result = new DescriptionData();
//        result.setId(aDescriptionProvider.getId());
//        result.setNoun(aDescriptionProvider.getNoun());
//        result.setAdjective(aDescriptionProvider.getAdjective());
//        result.setShortDescription(aDescriptionProvider.getShortDescription());
//        result.setLongDescription(aDescriptionProvider.getLongDescription());
//        return result;
        return null;
    }

    public static DescriptionProvider mapToBO(DescriptionData aDescriptionData) {
//        DescriptionProvider result = new DescriptionProvider(aDescriptionData.getNoun());
//        result.setId(aDescriptionData.getId());
//        result.setAdjective(aDescriptionData.getAdjective());
//        result.setShortDescription(aDescriptionData.getShortDescription());
//        result.setLongDescription(aDescriptionData.getLongDescription());
//        return result;
        return null;
    }
}
