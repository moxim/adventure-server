package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 45, description = "Item container mapping with item dependencies")
public class ItemContainerMapper implements Mapper<ItemContainerData, GenericContainer> {

    private final MapperSupporter mapperSupporter;
    private final Map<String, Item> allItems;
    private final Mapper<DescriptionData, DescriptionProvider> descriptionMapper;
    private final ItemMapper itemMapper;

    public ItemContainerMapper(MapperSupporter aMapperSupporter,
                               DescriptionMapper aDescriptionMapper,
                               ItemMapper aItemMapper) {
        mapperSupporter = aMapperSupporter;
        allItems = aMapperSupporter.getMappedItems();
        descriptionMapper = aDescriptionMapper;
        itemMapper = aItemMapper;
    }

    @Override
    public GenericContainer mapToBO(ItemContainerData anItemContainerData) {
        GenericContainer container = registerContainer(anItemContainerData);
        mapItemCommands(anItemContainerData);
        return container;
    }

    /**
     * Phase 1: create the container and register it and its items with the MapperSupporter,
     * without mapping any item commands. Commands may reference items in other containers by id,
     * so every container's items must be registered before any command is mapped.
     */
    public GenericContainer registerContainer(ItemContainerData anItemContainerData) {
        final DescriptionData descriptionData = anItemContainerData.getDescriptionData();
        DescriptionProvider descriptionProvider = descriptionMapper.mapToBO(descriptionData);
        GenericContainer container = new GenericContainer(descriptionProvider, anItemContainerData.getMaxSize());
        container.setId(anItemContainerData.getId());
        container.setMaxSize(anItemContainerData.getMaxSize());
        mapperSupporter.addMappedContainer(container);
        List<Item> itemList = itemMapper.registerItems(anItemContainerData.getItems());
        container.setContents(itemList);
        return container;
    }

    /**
     * Phase 2: map the commands of this container's items. Must run after all items of the
     * adventure are registered.
     */
    public void mapItemCommands(ItemContainerData anItemContainerData) {
        itemMapper.mapCommands(anItemContainerData.getItems());
    }

    @Override
    public ItemContainerData mapToDO(GenericContainer aContainer) {
        ItemContainerData itemContainerData = new ItemContainerData(aContainer.getParentContainer().getId());
        itemContainerData.setId(aContainer.getId());
        DescriptionData descriptionData = descriptionMapper.mapToDO(aContainer.getDescriptionProvider());
        itemContainerData.setDescriptionData(descriptionData);
        // TODO: map this
//        for (Containable containable : aContainer.getContents()) {
//            itemContainerData.getContents().add(containable);
//        }
        itemContainerData.setMaxSize(aContainer.getMaxSize());
        return itemContainerData;
    }


}
