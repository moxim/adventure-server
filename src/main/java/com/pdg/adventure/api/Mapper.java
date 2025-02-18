package com.pdg.adventure.api;

import java.util.ArrayList;
import java.util.List;

public interface Mapper<DO, BO> {
    BO mapToBO(DO from);
    DO mapToDO(BO from);

    default List<BO> mapToBOs(List<DO> aDataObjectList) {
        List<BO> result = new ArrayList<BO>(aDataObjectList.size());
        for (DO dataObject : aDataObjectList) {
            result.add(mapToBO(dataObject));
        }
        return result;
    }

    default List<DO> mapToDOs(List<BO> aBusinessObjectList) {
        List<DO> result = new ArrayList<DO>(aBusinessObjectList.size());
        for (BO businessObject : aBusinessObjectList) {
            result.add(mapToDO(businessObject));
        }
        return result;
    }
}
