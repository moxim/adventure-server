# Annotation-Based Mapper Registration

This package provides annotations to automatically register mappers with the `MapperSupporter`, eliminating the need for manual `@PostConstruct` registration methods.

## Available Annotations

### @RegisterMapper
Explicit registration where you specify both the data object and business object classes:

```java
@Service
@RegisterMapper(
    dataObjectClass = VocabularyData.class,
    businessObjectClass = Vocabulary.class,
    priority = 10
)
public class VocabularyMapper implements Mapper<VocabularyData, Vocabulary> {
    // No @PostConstruct registerMapper() needed!
}
```

### @AutoRegisterMapper
Automatic registration that detects the types from the generic interface:

```java
@Service
@AutoRegisterMapper(
    priority = 20,
    description = "Maps vocabulary data"
)
public class VocabularyMapper implements Mapper<VocabularyData, Vocabulary> {
    // Types are automatically detected from Mapper<VocabularyData, Vocabulary>
}
```

## Features

### Priority-Based Registration
Use the `priority` attribute to control registration order:
- Lower numbers = higher priority (registered first)
- Default priority is 100
- Useful when mappers depend on other mappers

```java
@AutoRegisterMapper(priority = 10)  // High priority
public class VocabularyMapper implements Mapper<VocabularyData, Vocabulary> { }

@AutoRegisterMapper(priority = 50)  // Medium priority
public class LocationMapper implements Mapper<LocationData, Location> { }

@AutoRegisterMapper()  // Default priority (100)
public class ItemMapper implements Mapper<ItemData, Item> { }
```

### Automatic Type Detection
The `@AutoRegisterMapper` annotation automatically extracts generic type arguments from your mapper interface:

```java
// This automatically detects:
// dataObjectClass = DescriptionData.class
// businessObjectClass = DescriptionProvider.class
@AutoRegisterMapper
public class DescriptionMapper implements Mapper<DescriptionData, DescriptionProvider> {
    // Implementation
}
```

## Migration from Manual Registration

### Before (Manual Registration):
```java
@Service
public class VocabularyMapper implements Mapper<VocabularyData, Vocabulary> {
    
    private final MapperSupporter mapperSupporter;

    public VocabularyMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void registerMapper() {
        mapperSupporter.registerMapper(VocabularyData.class, Vocabulary.class, this);
    }
    
    // mapping methods...
}
```

### After (Annotation-Based):
```java
@Service
@AutoRegisterMapper(priority = 10)
public class VocabularyMapper implements Mapper<VocabularyData, Vocabulary> {
    
    private final MapperSupporter mapperSupporter;

    public VocabularyMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    // No @PostConstruct needed!
    
    // mapping methods...
}
```

## Benefits

1. **Less Boilerplate**: No need for `@PostConstruct` registration methods
2. **Type Safety**: Automatic type detection reduces errors
3. **Dependency Management**: Priority-based registration handles mapper dependencies
4. **Centralized Registration**: All registration logic is handled by the annotation processors
5. **Debugging**: Clear logging shows registration order and any issues

## Implementation Details

The system uses Spring's `BeanPostProcessor` to scan for annotated mappers during application startup:

1. `MapperRegistrationProcessor` handles `@RegisterMapper` annotations
2. `AutoMapperRegistrationProcessor` handles `@AutoRegisterMapper` annotations
3. Both processors queue mappers and register them when `MapperSupporter` is ready
4. Registration happens in priority order (lower numbers first)

## Logging

Enable debug logging to see registration details:

```properties
# application.properties
logging.level.com.pdg.adventure.server.annotation=DEBUG
```

This will show:
- Which mappers are queued for registration
- Registration order based on priority
- Success/failure of individual registrations
- Generic type detection results

## Best Practices

1. **Use @AutoRegisterMapper** for most cases - it's simpler and less error-prone
2. **Set priorities** when mappers have dependencies (vocabulary, descriptions, etc.)
3. **Use descriptive names** for the `description` field to document mapper purpose
4. **Test registration order** in complex dependency scenarios
5. **Monitor logs** during development to ensure proper registration

## Troubleshooting

### Generic Type Detection Issues
If `@AutoRegisterMapper` can't detect types:
- Ensure your mapper directly implements `Mapper<DO, BO>`
- Avoid complex inheritance hierarchies
- Use `@RegisterMapper` with explicit types as fallback

### Registration Order Issues  
If mappers fail due to dependencies:
- Set appropriate priorities (lower = earlier)
- Check logs for registration order
- Consider lazy initialization in mapper constructors