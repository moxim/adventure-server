package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

@Service
public class NewThingMapper {
/*

<dependency>
  <groupId>org.modelmapper</groupId>
  <artifactId>modelmapper</artifactId>
  <version>3.1.1</version>
</dependency>

        private final ModelMapper modelMapper;

        public NewThingMapper() {
            this.modelMapper = new ModelMapper();
            this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        }

        public ThingDto toDto(ThingData thingData) {
            ThingDto thingDto = modelMapper.map(thingData, ThingDto.class);
            thingDto.setDescription(modelMapper.map(thingData.getDescriptionData(), DescriptionDto.class));
            thingDto.setCommandProvider(modelMapper.map(thingData.getCommandProviderData(), CommandProviderDto.class));
            return thingDto;
        }

        public ThingData toEntity(ThingDto thingDto) {
            ThingData thingData = modelMapper.map(thingDto, ThingData.class);
            thingData.setDescriptionData(modelMapper.map(thingDto.getDescription(), DescriptionData.class));
            thingData.setCommandProviderData(modelMapper.map(thingDto.getCommandProvider(), CommandProviderData.class));
            return thingData;
        }
 */
}
