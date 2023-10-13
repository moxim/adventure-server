package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemContainerMapper implements Mapper<ItemContainerData, GenericContainer> {

    private final Map<String, Item> allItems;
    private final MapperProvider mapperProvider;

    @Autowired
    public ItemContainerMapper(MapperProvider aMapperProvider, Map<String, Item> aBagOfItems) {
        allItems = aBagOfItems;
        mapperProvider = aMapperProvider;
    }

    public GenericContainer mapToBO(ItemContainerData anItemContainerData) {
        DescriptionProvider descriptionProvider = DescriptionMapper.mapToBO(anItemContainerData.getDescriptionData());
        GenericContainer container = new GenericContainer(descriptionProvider, anItemContainerData.getMaxSize());
        container.setId(anItemContainerData.getId());
        ItemMapper itemMapper = mapperProvider.getMapper(ItemMapper.class);
        List<Containable> itemList = new ArrayList<>(anItemContainerData.getContents().size());
//        for (String itemId : anItemContainerData.getContents()) {
//            container.add(allItems.get(itemId));
//        }
        container.setMaxSize(anItemContainerData.getMaxSize());
        return container;
    }

    public ItemContainerData mapToDO(GenericContainer aContainer) {
        ItemContainerData itemContainerData = new ItemContainerData();
        itemContainerData.setId(aContainer.getId());
        DescriptionData descriptionData = DescriptionMapper.mapToDO(aContainer.getDescriptionProvider());
        itemContainerData.setDescriptionData(descriptionData);
//        for (Containable containable : aContainer.getContents()) {
//            itemContainerData.getContents().add(containable);
//        }
        itemContainerData.setMaxSize(aContainer.getMaxSize());
        return itemContainerData;
    }
}
