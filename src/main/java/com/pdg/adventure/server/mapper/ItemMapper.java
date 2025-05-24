package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@DependsOn({"descriptionMapper",
            "commandMapper",
            "mapperSupporter"})
public class ItemMapper implements Mapper<ItemData, Item> {
    private Mapper<DescriptionData, DescriptionProvider> descriptionMapper;
    private Mapper<CommandData, Command> commandMapper;

    private final MapperSupporter mapperSupporter;

    public ItemMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }


    @PostConstruct
    public void registerMapper() {
        descriptionMapper = mapperSupporter.getMapper(DescriptionData.class);
        commandMapper = mapperSupporter.getMapper(CommandData.class);
        mapperSupporter.registerMapper(ItemData.class, Item.class, this);
    }

    public Item mapToBO(ItemData anItemData) {
        final Item item = new Item(descriptionMapper.mapToBO(anItemData.getDescriptionData()), anItemData.isContainable());
        item.setId(anItemData.getId());
        item.setIsWearable(anItemData.isWearable());
        item.setIsWorn(anItemData.isWorn());
//        DirectionContainerMapper directionContainerMapper = mapperProvider.getMapper(DirectionContainerMapper.class);
        // TODO
//        item.setParentContainer(containerMapper.mapToBO(anItemData.getParentContainerId()));
        final CommandProviderData commandProviderData = anItemData.getCommandProviderData();

        return item;
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

//    public List<ItemData> mapToDOs(List<Containable> aContainableList) {
//        List<ItemData> itemDataList = new ArrayList<>(aContainableList.size());
//        for (Containable containable : aContainableList) {
//            itemDataList.add(mapToDO((Item)containable));
//        }
//        return itemDataList;
//    }

//    public static List<Item> mapToBOs(List<ItemData> anItemDataList) {
//        final List<Item> itemList = new ArrayList<>(anItemDataList.size());
//        for (ItemData itemData : anItemDataList) {
//            itemList.add(mapToBO(itemData));
//        }
//        return itemList;
//    }
}
