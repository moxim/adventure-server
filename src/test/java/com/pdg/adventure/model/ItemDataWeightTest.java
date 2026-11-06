package com.pdg.adventure.model;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins how {@link ItemData#getWeight()} behaves when read from MongoDB: documents stored before
 * weight existed have no {@code weight} key and must load as the default (1), not as Java's 0.
 */
class ItemDataWeightTest {

    private final MappingMongoConverter converter = createConverter();

    private static MappingMongoConverter createConverter() {
        MongoCustomConversions conversions = new MongoCustomConversions(List.of());
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        mappingContext.afterPropertiesSet();
        MappingMongoConverter result = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, mappingContext);
        result.setCustomConversions(conversions);
        result.afterPropertiesSet();
        return result;
    }

    @Test
    void newItemData_hasWeightOne() {
        assertThat(new ItemData().getWeight()).isEqualTo(1);
    }

    @Test
    void documentWithoutWeight_loadsWithDefaultWeightOne() {
        Document legacyDocument = new Document("lumen", 5);

        ItemData result = converter.read(ItemData.class, legacyDocument);

        assertThat(result.getWeight()).isEqualTo(1);
    }

    @Test
    void documentWithWeight_loadsStoredWeight_includingZero() {
        assertThat(converter.read(ItemData.class, new Document("weight", 12)).getWeight()).isEqualTo(12);
        assertThat(converter.read(ItemData.class, new Document("weight", 0)).getWeight()).isZero();
    }

    @Test
    void weight_isWrittenToTheDocument() {
        ItemData itemData = new ItemData();
        itemData.setWeight(9);

        Document written = new Document();
        converter.write(itemData, written);

        assertThat(written.getInteger("weight")).isEqualTo(9);
    }
}
