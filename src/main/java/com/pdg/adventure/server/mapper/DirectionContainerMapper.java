package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

@Service
public class DirectionContainerMapper
//        implements Mapper<DirectionContainerData, DirectionContainer>
{
//
//    private final MapperProvider mapperProvider;
//    private final DirectionMapper directionMapper;
//
//    @Autowired
//    public  DirectionContainerMapper(MapperProvider aMapperProvider) {
//        mapperProvider = aMapperProvider;
//        directionMapper = new DirectionMapper(mapperProvider);
//    }
//
//    public DirectionContainer mapToBO(DirectionContainerData aDirectionContainerData) {
//        DescriptionProvider descriptionProvider = DescriptionMapper.map(aDirectionContainerData.getDescriptionData());
//        DirectionContainer container = new DirectionContainer(descriptionProvider);
//        container.setId(aDirectionContainerData.getId());
//        for (DirectionData directionData : aDirectionContainerData.getContents()) {
//            container.add(directionMapper.mapToBO(directionData));
//        }
//        return container;
//    }
//
//    public DirectionContainerData mapToDO(DirectionContainer aContainer) {
//        DirectionContainerData itemContainerData = new DirectionContainerData();
//        for (Direction direction : aContainer.getContents()) {
//            itemContainerData.getContents().add(directionMapper.mapToDO(direction));
//        }
//        return itemContainerData;
//    }
}
