package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.parser.GenericCommandProvider;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 40, description = "Item mapping with dependencies")
public class ItemMapper implements Mapper<ItemData, Item> {
    private final Mapper<DescriptionData, DescriptionProvider> descriptionMapper;
    private final CommandProviderMapper commandProviderMapper;
    private final Mapper<CommandData, Command> commandMapper;
    private final MapperSupporter mapperSupporter;

    public ItemMapper(MapperSupporter aMapperSupporter,
                      DescriptionMapper aDescriptionMapper,
                      CommandProviderMapper aCommandProviderMapper,
                      CommandMapper aCommandMapper) {
        mapperSupporter = aMapperSupporter;
        descriptionMapper = aDescriptionMapper;
        commandProviderMapper = aCommandProviderMapper;
        commandMapper = aCommandMapper;
        mapperSupporter.registerMapper(ItemData.class, Item.class, this);
    }

    public Item mapToBO(ItemData anItemData) {
        final Item item = new Item(descriptionMapper.mapToBO(anItemData.getDescriptionData()),
                                   anItemData.isContainable());
        item.setId(anItemData.getId());
        item.setIsWearable(anItemData.isWearable());
        item.setIsWorn(anItemData.isWorn());
        mapperSupporter.addMappedItem(item);
//        DirectionContainerMapper directionContainerMapper = mapperProvider.getMapper(DirectionContainerMapper.class);
        // TODO
//        item.setParentContainer(anItemData.getParentContainerId());
        return item;
    }

    @Override
    public List<Item> mapToBOs(List<ItemData> anItemDataList) {
        final List<Item> itemList = new ArrayList<>(anItemDataList.size());
        for (ItemData itemData : anItemDataList) {
            itemList.add(mapToBO(itemData));
        }
        anItemDataList.forEach(anItemData -> {
            final Item item = mapperSupporter.getMappedItem(anItemData.getId());
            final CommandProviderData commandProviderData = anItemData.getCommandProviderData();
            final GenericCommandProvider commandProvider = commandProviderMapper.mapToBO(commandProviderData);
            item.setCommandProvider(commandProvider);
            item.setParentContainer(mapperSupporter.getMappedContainer(anItemData.getParentContainerId()));
        });
        return itemList;
    }

    public ItemData mapToDO(Item anItem) {
        ItemData itemData = new ItemData();
        itemData.setId(anItem.getId());
        itemData.setDescriptionData(descriptionMapper.mapToDO(anItem.getDescriptionProvider()));
        itemData.setContainable(anItem.isContainable());
        itemData.setWearable(anItem.isWearable());
        itemData.setWorn(anItem.isWorn());
//        DirectionContainerMapper directionContainerMapper = mapperProvider.getMapper(DirectionContainerMapper.class);
//        itemData.setParentContainer(anItem.getParentContainer());
        return itemData;
    }

//    @Override
//    public List<ItemData> mapToDOs(List<Containable> aContainableList) {
//        List<ItemData> itemDataList = new ArrayList<>(aContainableList.size());
//        for (Containable containable : aContainableList) {
//            itemDataList.add(mapToDO((Item)containable));
//        }
//        return itemDataList;
//    }

}
