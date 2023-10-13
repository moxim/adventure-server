package com.pdg.adventure.api;

import java.util.ArrayList;
import java.util.List;

public interface Mapper<D, B> {
    B mapToBO(D from);
    D mapToDO(B from);

    default List<B> mapToBOs(List<D> aDataObjectList) {
        List<B> result = new ArrayList<B>(aDataObjectList.size());
        for (D dataObject : aDataObjectList) {
            result.add(mapToBO(dataObject));
        }
        return result;
    }

    default List<D> mapToDOs(List<B> aBusinessObjectList) {
        List<D> result = new ArrayList<D>(aBusinessObjectList.size());
        for (B businessObject : aBusinessObjectList) {
            result.add(mapToDO(businessObject));
        }
        return result;
    }
}
