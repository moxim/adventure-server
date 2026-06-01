# Condition Mappers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement 9 missing condition mappers (plus 1 model change and @Getter additions) to complete the bidirectional mapping between `PreConditionData` subclasses and their runtime `AbstractCondition` counterparts.

**Architecture:** Option A — `AndConditionData`/`OrConditionData` are changed to embed child `PreConditionData` objects inline (like `NotConditionData`) rather than referencing them by ID string, enabling recursive mapper delegation identical to `NotConditionMapper`. All 9 mappers extend `PreConditionMapper<DO, BO>`, call `aMapperSupporter.registerMapper(...)` in their constructor, and are annotated `@Service @AutoRegisterMapper`. Leaf conditions use priority 20; composite conditions (And, Or) use priority 10.

**Tech Stack:** Java 25, Spring Boot, Lombok (`@Data`, `@Getter`), JUnit 5 + AssertJ + Mockito, Maven

---

## File Map

**Modified — model:**
- `server/src/main/java/com/pdg/adventure/model/condition/AndConditionData.java` — change String ID fields to embedded `PreConditionData`
- `server/src/main/java/com/pdg/adventure/model/condition/OrConditionData.java` — same

**Modified — BO conditions (add `@Getter` on fields needed by mapToDO):**
- `server/src/main/java/com/pdg/adventure/server/condition/EqualsCondition.java` — `@Getter` on `variableName`, `value`
- `server/src/main/java/com/pdg/adventure/server/condition/GreaterThanCondition.java` — `@Getter` on `variableName`, `value`
- `server/src/main/java/com/pdg/adventure/server/condition/LowerThanCondition.java` — `@Getter` on `variableName`, `value`
- `server/src/main/java/com/pdg/adventure/server/condition/SameCondition.java` — `@Getter` on `variableNameOne`, `variableNameTwo`
- `server/src/main/java/com/pdg/adventure/server/condition/WornCondition.java` — `@Getter` on `thing`
- `server/src/main/java/com/pdg/adventure/server/condition/PlayerAtCondition.java` — `@Getter` on `location`
- `server/src/main/java/com/pdg/adventure/server/condition/ItemAtCondition.java` — `@Getter` on `thing`, `location`
- `server/src/main/java/com/pdg/adventure/server/condition/AndCondition.java` — `@Getter` on `preCondition`, `anotherPreCondition`
- `server/src/main/java/com/pdg/adventure/server/condition/OrCondition.java` — `@Getter` on `preCondition`, `anotherPreCondition`

**Created — mappers:**
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/EqualsConditionMapper.java`
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/GreaterThanConditionMapper.java`
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/LowerThanConditionMapper.java`
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/SameConditionMapper.java`
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/WornConditionMapper.java`
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/PlayerAtConditionMapper.java`
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/ItemAtConditionMapper.java`
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/AndConditionMapper.java`
- `server/src/main/java/com/pdg/adventure/server/mapper/condition/OrConditionMapper.java`

**Created — tests:**
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/EqualsConditionMapperTest.java`
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/GreaterThanConditionMapperTest.java`
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/LowerThanConditionMapperTest.java`
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/SameConditionMapperTest.java`
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/WornConditionMapperTest.java`
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/PlayerAtConditionMapperTest.java`
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/ItemAtConditionMapperTest.java`
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/AndConditionMapperTest.java`
- `server/src/test/java/com/pdg/adventure/server/mapper/condition/OrConditionMapperTest.java`

---

## Background: Key Patterns

**`CarriedConditionMapper` is the canonical leaf mapper template.** Every leaf mapper follows this structure:

```java
@Service
@AutoRegisterMapper(priority = 20, description = "XCondition mapper")
public class XConditionMapper extends PreConditionMapper<XConditionData, XCondition> {

    private final /* deps */ dep;

    public XConditionMapper(MapperSupporter aMapperSupporter, /* other deps */) {
        super(aMapperSupporter);
        dep = ...;
        aMapperSupporter.registerMapper(XConditionData.class, XCondition.class, this);
    }

    @Override
    public XCondition mapToBO(XConditionData data) {
        XCondition result = new XCondition(/* resolved args */);
        result.setId(data.getId());
        return result;
    }

    @Override
    public XConditionData mapToDO(XCondition condition) {
        XConditionData result = new XConditionData();
        result.set...(...);
        result.setId(condition.getId());
        return result;
    }
}
```

**`NotConditionMapper` is the canonical composite mapper template.** `AndConditionMapper` and `OrConditionMapper` follow this pattern exactly, but with two child conditions instead of one:

```java
@Service
@AutoRegisterMapper(priority = 10, description = "...")
public class AndConditionMapper extends PreConditionMapper<AndConditionData, AndCondition> {
    public AndConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(AndConditionData.class, AndCondition.class, this);
    }
    @Override
    public AndCondition mapToBO(AndConditionData data) {
        PreCondition first = (PreCondition) getMapperSupporter()
            .getMapper(data.getPreCondition().getClass()).mapToBO(data.getPreCondition());
        PreCondition second = (PreCondition) getMapperSupporter()
            .getMapper(data.getAnotherPreCondition().getClass()).mapToBO(data.getAnotherPreCondition());
        AndCondition result = new AndCondition(first, second);
        result.setId(data.getId());
        return result;
    }
    // mapToDO is symmetric
}
```

**Test structure** (every test class):
- `@ExtendWith(MockitoExtension.class)` + `@MockitoSettings(strictness = Strictness.LENIENT)`
- `@Mock MapperSupporter mapperSupporter`
- `@BeforeEach`: `doNothing().when(mapperSupporter).registerMapper(any(), any(), any())` then construct mapper manually
- 5 `@Test @DisplayName` methods: mapToBO happy path, mapToBO verifies resolution, mapToDO happy path, mapToDO preserves ID, round-trip

**Variable conditions** (Equals, GreaterThan, LowerThan, Same) use `getMapperSupporter().getVariableProvider()` — no direct `VariableProvider` injection needed. In tests, `when(mapperSupporter.getVariableProvider()).thenReturn(mockVariableProvider)`.

**ID propagation** is mandatory in both directions:
- `mapToBO`: `result.setId(data.getId())`
- `mapToDO`: `result.setId(condition.getId())`

**Test run command:**
```bash
cd server && mvn test -Dtest="<TestClassName>" -DfailIfNoTests=false 2>&1 | tail -20
```

**Full suite (excluding MongoDB infrastructure tests):**
```bash
cd server && mvn test "-Dexclude=**/storage/**Test.java,**/AdventureBuilderTest.java" 2>&1 | tail -30
```

---

## Task 1: Update AndConditionData and OrConditionData

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/model/condition/AndConditionData.java`
- Modify: `server/src/main/java/com/pdg/adventure/model/condition/OrConditionData.java`

- [ ] **Step 1: Replace `AndConditionData` fields**

Replace the entire file content:

```java
package com.pdg.adventure.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AndConditionData extends PreConditionData {
    private PreConditionData preCondition;
    private PreConditionData anotherPreCondition;
}
```

- [ ] **Step 2: Replace `OrConditionData` fields**

Replace the entire file content:

```java
package com.pdg.adventure.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class OrConditionData extends PreConditionData {
    private PreConditionData preCondition;
    private PreConditionData anotherPreCondition;
}
```

- [ ] **Step 3: Verify compilation**

```bash
cd server && mvn compile -q 2>&1 | tail -10
```

Expected: no output (clean compile).

- [ ] **Step 4: Commit**

```bash
cd server && git add src/main/java/com/pdg/adventure/model/condition/AndConditionData.java \
  src/main/java/com/pdg/adventure/model/condition/OrConditionData.java
git commit -m "refactor: embed child conditions inline in AndConditionData and OrConditionData"
```

---

## Task 2: Add @Getter to BO Condition Classes

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/EqualsCondition.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/GreaterThanCondition.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/LowerThanCondition.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/SameCondition.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/WornCondition.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/PlayerAtCondition.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/ItemAtCondition.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/AndCondition.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/condition/OrCondition.java`

- [ ] **Step 1: Update `EqualsCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public class EqualsCondition extends AbstractVariableCondition {

    @Getter
    private final String variableName;
    @Getter
    private final String value;

    public EqualsCondition(String aVariableName, String aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableName = aVariableName;
        value = aValue;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        final Variable envVariable = getVariable(variableName);
        if (envVariable.aValue().equals(value)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
```

- [ ] **Step 2: Update `GreaterThanCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class GreaterThanCondition extends AbstractVariableCondition {

    @Getter
    private final String variableName;
    @Getter
    private final Number value;

    public GreaterThanCondition(String aVariableName, Number aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableName = aVariableName;
        value = aValue;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        final Variable envVariable = getVariable(variableName);
        int envVal;
        try {
            envVal = Integer.parseInt(envVariable.aValue());
        } catch (NumberFormatException _) {
            throw new ConfigurationException("This variable does not contain a number: " + variableName);
        }
        int iVal;
        try {
            iVal = Integer.parseInt(value.toString());
        } catch (NumberFormatException _) {
            throw new ConfigurationException("This value is not a number: " + value);
        }
        if (envVal > iVal) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
```

- [ ] **Step 3: Update `LowerThanCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LowerThanCondition extends AbstractVariableCondition {

    @Getter
    private final String variableName;
    @Getter
    private final Number value;

    public LowerThanCondition(String aVariableName, Number aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableName = aVariableName;
        value = aValue;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        final Variable envVariable = getVariable(variableName);
        int envVal;
        try {
            envVal = Integer.parseInt(envVariable.aValue());
        } catch (NumberFormatException _) {
            throw new ConfigurationException("This variable does not contain a number: " + variableName);
        }
        int iVal;
        try {
            iVal = Integer.parseInt(value.toString());
        } catch (NumberFormatException _) {
            throw new ConfigurationException("This value is not a number: " + value);
        }
        if (envVal < iVal) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
```

- [ ] **Step 4: Update `SameCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SameCondition extends AbstractVariableCondition {

    @Getter
    private final String variableNameOne;
    @Getter
    private final String variableNameTwo;

    public SameCondition(String aVariableNameOne, String aVariableNameTwo, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableNameOne = aVariableNameOne;
        variableNameTwo = aVariableNameTwo;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();

        if (variableProvider.get(variableNameOne).equals(variableProvider.get(variableNameTwo))) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }

        return result;
    }
}
```

- [ ] **Step 5: Update `WornCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Wearable;
import com.pdg.adventure.server.parser.CommandExecutionResult;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class WornCondition extends AbstractCondition {

    @Getter
    private final Wearable thing;

    public WornCondition(Wearable aThing) {
        thing = aThing;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (thing.isWorn()) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        } else {
            result.setResultMessage("You are not wearing %s.".formatted(thing.getEnrichedBasicDescription()));
        }
        return result;
    }
}
```

- [ ] **Step 6: Update `PlayerAtCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PlayerAtCondition extends AbstractCondition {

    @Getter
    private final Location location;
    private final transient GameContext gameContext;

    public PlayerAtCondition(Location aLocation, GameContext aGameContext) {
        location = aLocation;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (gameContext.getCurrentLocation().equals(location)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
```

- [ ] **Step 7: Update `ItemAtCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.tangible.Item;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ItemAtCondition extends AbstractCondition {

    @Getter
    private final Location location;
    @Getter
    private final Item thing;

    public ItemAtCondition(Item aThing, Location aLocation) {
        thing = aThing;
        location = aLocation;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        if (location.contains(thing)) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
```

- [ ] **Step 8: Update `AndCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AndCondition extends AbstractCondition {

    @Getter
    private final PreCondition preCondition;
    @Getter
    private final PreCondition anotherPreCondition;

    public AndCondition(PreCondition aPreCondition, PreCondition andAnotherPreCondition) {
        preCondition = aPreCondition;
        anotherPreCondition = andAnotherPreCondition;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = preCondition.check();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            ExecutionResult rightResult = anotherPreCondition.check();
            if (rightResult.getExecutionState() == ExecutionResult.State.FAILURE) {
                result = rightResult;
            }
        }
        return result;
    }
}
```

- [ ] **Step 9: Update `OrCondition`**

Replace the entire file content:

```java
package com.pdg.adventure.server.condition;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class OrCondition extends AbstractCondition {

    @Getter
    private final PreCondition preCondition;
    @Getter
    private final PreCondition anotherPreCondition;

    public OrCondition(PreCondition aPreCondition, PreCondition andAnotherPreCondition) {
        preCondition = aPreCondition;
        anotherPreCondition = andAnotherPreCondition;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = preCondition.check();
        if (result.getExecutionState() == ExecutionResult.State.FAILURE) {
            ExecutionResult rightResult = anotherPreCondition.check();
            if (rightResult.getExecutionState() == ExecutionResult.State.SUCCESS) {
                result = rightResult;
            }
        }
        return result;
    }
}
```

- [ ] **Step 10: Verify compilation**

```bash
cd server && mvn compile -q 2>&1 | tail -10
```

Expected: no output (clean compile).

- [ ] **Step 11: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/condition/EqualsCondition.java \
  src/main/java/com/pdg/adventure/server/condition/GreaterThanCondition.java \
  src/main/java/com/pdg/adventure/server/condition/LowerThanCondition.java \
  src/main/java/com/pdg/adventure/server/condition/SameCondition.java \
  src/main/java/com/pdg/adventure/server/condition/WornCondition.java \
  src/main/java/com/pdg/adventure/server/condition/PlayerAtCondition.java \
  src/main/java/com/pdg/adventure/server/condition/ItemAtCondition.java \
  src/main/java/com/pdg/adventure/server/condition/AndCondition.java \
  src/main/java/com/pdg/adventure/server/condition/OrCondition.java
git commit -m "feat: add @Getter to condition BO fields required by mapToDO"
```

---

## Task 3: EqualsConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/EqualsConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/EqualsConditionMapperTest.java`

`EqualsConditionData` has `final` fields — its only constructor is `new EqualsConditionData(String variableName, String value)`. The `id` field is inherited and mutable, so `setId()` works after construction. `EqualsCondition` fields are accessed via the `@Getter` annotations added in Task 2.

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/EqualsConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.server.condition.EqualsCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EqualsConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private EqualsConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new EqualsConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts EqualsConditionData to EqualsCondition")
    void mapToBO_shouldConvertEqualsConditionDataToEqualsCondition() {
        EqualsConditionData data = new EqualsConditionData("score", "100");
        data.setId("equals-001");

        EqualsCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("equals-001");
        assertThat(result.getVariableName()).isEqualTo("score");
        assertThat(result.getValue()).isEqualTo("100");
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        EqualsConditionData data = new EqualsConditionData("lives", "3");

        EqualsCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts EqualsCondition to EqualsConditionData")
    void mapToDO_shouldConvertEqualsConditionToEqualsConditionData() {
        EqualsCondition condition = new EqualsCondition("level", "5", variableProvider);
        condition.setId("equals-002");

        EqualsConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("equals-002");
        assertThat(result.getVariableName()).isEqualTo("level");
        assertThat(result.getValue()).isEqualTo("5");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        EqualsCondition condition1 = new EqualsCondition("x", "1", variableProvider);
        condition1.setId("equals-id-001");

        EqualsCondition condition2 = new EqualsCondition("x", "1", variableProvider);
        condition2.setId("equals-id-002");

        EqualsConditionData result1 = mapper.mapToDO(condition1);
        EqualsConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("equals-id-001");
        assertThat(result2.getId()).isEqualTo("equals-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        EqualsConditionData original = new EqualsConditionData("score", "100");
        original.setId("round-trip-equals");

        EqualsCondition bo = mapper.mapToBO(original);
        EqualsConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableName()).isEqualTo(original.getVariableName());
        assertThat(roundTrip.getValue()).isEqualTo(original.getValue());
    }
}
```

- [ ] **Step 2: Run the test — expect failure (class not found)**

```bash
cd server && mvn test -Dtest="EqualsConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error or `ClassNotFoundException` for `EqualsConditionMapper`.

- [ ] **Step 3: Create `EqualsConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/EqualsConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.EqualsCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "EqualsCondition mapper")
public class EqualsConditionMapper extends PreConditionMapper<EqualsConditionData, EqualsCondition> {

    public EqualsConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(EqualsConditionData.class, EqualsCondition.class, this);
    }

    @Override
    public EqualsCondition mapToBO(EqualsConditionData data) {
        EqualsCondition result = new EqualsCondition(data.getVariableName(), data.getValue(),
                                                     getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public EqualsConditionData mapToDO(EqualsCondition condition) {
        EqualsConditionData result = new EqualsConditionData(condition.getVariableName(), condition.getValue());
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="EqualsConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/EqualsConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/EqualsConditionMapperTest.java
git commit -m "feat: add EqualsConditionMapper with tests"
```

---

## Task 4: GreaterThanConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/GreaterThanConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/GreaterThanConditionMapperTest.java`

`GreaterThanConditionData` has mutable (non-final) fields — use no-arg constructor + setters. `GreaterThanCondition.value` is `Number`.

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/GreaterThanConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.server.condition.GreaterThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GreaterThanConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private GreaterThanConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new GreaterThanConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts GreaterThanConditionData to GreaterThanCondition")
    void mapToBO_shouldConvertGreaterThanConditionDataToGreaterThanCondition() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setId("gt-001");
        data.setVariableName("score");
        data.setValue(50);

        GreaterThanCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("gt-001");
        assertThat(result.getVariableName()).isEqualTo("score");
        assertThat(result.getValue()).isEqualTo(50);
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setVariableName("lives");
        data.setValue(0);

        GreaterThanCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts GreaterThanCondition to GreaterThanConditionData")
    void mapToDO_shouldConvertGreaterThanConditionToGreaterThanConditionData() {
        GreaterThanCondition condition = new GreaterThanCondition("level", 10, variableProvider);
        condition.setId("gt-002");

        GreaterThanConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("gt-002");
        assertThat(result.getVariableName()).isEqualTo("level");
        assertThat(result.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        GreaterThanCondition condition1 = new GreaterThanCondition("x", 5, variableProvider);
        condition1.setId("gt-id-001");

        GreaterThanCondition condition2 = new GreaterThanCondition("x", 5, variableProvider);
        condition2.setId("gt-id-002");

        GreaterThanConditionData result1 = mapper.mapToDO(condition1);
        GreaterThanConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("gt-id-001");
        assertThat(result2.getId()).isEqualTo("gt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        GreaterThanConditionData original = new GreaterThanConditionData();
        original.setId("round-trip-gt");
        original.setVariableName("score");
        original.setValue(50);

        GreaterThanCondition bo = mapper.mapToBO(original);
        GreaterThanConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableName()).isEqualTo(original.getVariableName());
        assertThat(roundTrip.getValue()).isEqualTo(original.getValue());
    }
}
```

- [ ] **Step 2: Run the test — expect failure**

```bash
cd server && mvn test -Dtest="GreaterThanConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error for `GreaterThanConditionMapper`.

- [ ] **Step 3: Create `GreaterThanConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/GreaterThanConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.GreaterThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "GreaterThanCondition mapper")
public class GreaterThanConditionMapper extends PreConditionMapper<GreaterThanConditionData, GreaterThanCondition> {

    public GreaterThanConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(GreaterThanConditionData.class, GreaterThanCondition.class, this);
    }

    @Override
    public GreaterThanCondition mapToBO(GreaterThanConditionData data) {
        GreaterThanCondition result = new GreaterThanCondition(data.getVariableName(), data.getValue(),
                                                               getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public GreaterThanConditionData mapToDO(GreaterThanCondition condition) {
        GreaterThanConditionData result = new GreaterThanConditionData();
        result.setVariableName(condition.getVariableName());
        result.setValue(condition.getValue());
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="GreaterThanConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/GreaterThanConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/GreaterThanConditionMapperTest.java
git commit -m "feat: add GreaterThanConditionMapper with tests"
```

---

## Task 5: LowerThanConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/LowerThanConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/LowerThanConditionMapperTest.java`

Identical structure to `GreaterThanConditionMapper`. `LowerThanConditionData` fields are mutable.

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/LowerThanConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.LowerThanConditionData;
import com.pdg.adventure.server.condition.LowerThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LowerThanConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private LowerThanConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new LowerThanConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts LowerThanConditionData to LowerThanCondition")
    void mapToBO_shouldConvertLowerThanConditionDataToLowerThanCondition() {
        LowerThanConditionData data = new LowerThanConditionData();
        data.setId("lt-001");
        data.setVariableName("health");
        data.setValue(10);

        LowerThanCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("lt-001");
        assertThat(result.getVariableName()).isEqualTo("health");
        assertThat(result.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        LowerThanConditionData data = new LowerThanConditionData();
        data.setVariableName("lives");
        data.setValue(3);

        LowerThanCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts LowerThanCondition to LowerThanConditionData")
    void mapToDO_shouldConvertLowerThanConditionToLowerThanConditionData() {
        LowerThanCondition condition = new LowerThanCondition("energy", 5, variableProvider);
        condition.setId("lt-002");

        LowerThanConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("lt-002");
        assertThat(result.getVariableName()).isEqualTo("energy");
        assertThat(result.getValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        LowerThanCondition condition1 = new LowerThanCondition("x", 5, variableProvider);
        condition1.setId("lt-id-001");

        LowerThanCondition condition2 = new LowerThanCondition("x", 5, variableProvider);
        condition2.setId("lt-id-002");

        LowerThanConditionData result1 = mapper.mapToDO(condition1);
        LowerThanConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("lt-id-001");
        assertThat(result2.getId()).isEqualTo("lt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        LowerThanConditionData original = new LowerThanConditionData();
        original.setId("round-trip-lt");
        original.setVariableName("health");
        original.setValue(10);

        LowerThanCondition bo = mapper.mapToBO(original);
        LowerThanConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableName()).isEqualTo(original.getVariableName());
        assertThat(roundTrip.getValue()).isEqualTo(original.getValue());
    }
}
```

- [ ] **Step 2: Run the test — expect failure**

```bash
cd server && mvn test -Dtest="LowerThanConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error for `LowerThanConditionMapper`.

- [ ] **Step 3: Create `LowerThanConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/LowerThanConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.LowerThanConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.LowerThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "LowerThanCondition mapper")
public class LowerThanConditionMapper extends PreConditionMapper<LowerThanConditionData, LowerThanCondition> {

    public LowerThanConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(LowerThanConditionData.class, LowerThanCondition.class, this);
    }

    @Override
    public LowerThanCondition mapToBO(LowerThanConditionData data) {
        LowerThanCondition result = new LowerThanCondition(data.getVariableName(), data.getValue(),
                                                           getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public LowerThanConditionData mapToDO(LowerThanCondition condition) {
        LowerThanConditionData result = new LowerThanConditionData();
        result.setVariableName(condition.getVariableName());
        result.setValue(condition.getValue());
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="LowerThanConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/LowerThanConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/LowerThanConditionMapperTest.java
git commit -m "feat: add LowerThanConditionMapper with tests"
```

---

## Task 6: SameConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/SameConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/SameConditionMapperTest.java`

`SameConditionData` has mutable fields `variableNameOne` and `variableNameTwo`. `SameCondition` takes two variable name strings and a `VariableProvider`.

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/SameConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.server.condition.SameCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SameConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private SameConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new SameConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts SameConditionData to SameCondition")
    void mapToBO_shouldConvertSameConditionDataToSameCondition() {
        SameConditionData data = new SameConditionData();
        data.setId("same-001");
        data.setVariableNameOne("colour");
        data.setVariableNameTwo("targetColour");

        SameCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("same-001");
        assertThat(result.getVariableNameOne()).isEqualTo("colour");
        assertThat(result.getVariableNameTwo()).isEqualTo("targetColour");
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        SameConditionData data = new SameConditionData();
        data.setVariableNameOne("a");
        data.setVariableNameTwo("b");

        SameCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts SameCondition to SameConditionData")
    void mapToDO_shouldConvertSameConditionToSameConditionData() {
        SameCondition condition = new SameCondition("colourA", "colourB", variableProvider);
        condition.setId("same-002");

        SameConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("same-002");
        assertThat(result.getVariableNameOne()).isEqualTo("colourA");
        assertThat(result.getVariableNameTwo()).isEqualTo("colourB");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        SameCondition condition1 = new SameCondition("x", "y", variableProvider);
        condition1.setId("same-id-001");

        SameCondition condition2 = new SameCondition("x", "y", variableProvider);
        condition2.setId("same-id-002");

        SameConditionData result1 = mapper.mapToDO(condition1);
        SameConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("same-id-001");
        assertThat(result2.getId()).isEqualTo("same-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        SameConditionData original = new SameConditionData();
        original.setId("round-trip-same");
        original.setVariableNameOne("colour");
        original.setVariableNameTwo("targetColour");

        SameCondition bo = mapper.mapToBO(original);
        SameConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableNameOne()).isEqualTo(original.getVariableNameOne());
        assertThat(roundTrip.getVariableNameTwo()).isEqualTo(original.getVariableNameTwo());
    }
}
```

- [ ] **Step 2: Run the test — expect failure**

```bash
cd server && mvn test -Dtest="SameConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error for `SameConditionMapper`.

- [ ] **Step 3: Create `SameConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/SameConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.SameCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "SameCondition mapper")
public class SameConditionMapper extends PreConditionMapper<SameConditionData, SameCondition> {

    public SameConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(SameConditionData.class, SameCondition.class, this);
    }

    @Override
    public SameCondition mapToBO(SameConditionData data) {
        SameCondition result = new SameCondition(data.getVariableNameOne(), data.getVariableNameTwo(),
                                                  getMapperSupporter().getVariableProvider());
        result.setId(data.getId());
        return result;
    }

    @Override
    public SameConditionData mapToDO(SameCondition condition) {
        SameConditionData result = new SameConditionData();
        result.setVariableNameOne(condition.getVariableNameOne());
        result.setVariableNameTwo(condition.getVariableNameTwo());
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="SameConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/SameConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/SameConditionMapperTest.java
git commit -m "feat: add SameConditionMapper with tests"
```

---

## Task 7: WornConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/WornConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/WornConditionMapperTest.java`

`WornConditionData.thingId` is a String. `WornCondition` takes `Wearable`. `Item` implements `Wearable`, so `allItems().get(thingId)` returns an `Item` that can be passed directly. For `mapToDO`, `condition.getThing()` returns `Wearable`; cast to `Item` (which it always is in practice) to call `getId()`.

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/WornConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WornConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private Item mockItem;

    private WornConditionMapper mapper;
    private Map<String, Item> allItemsMap;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new WornConditionMapper(mapperSupporter, adventureConfig);

        allItemsMap = new HashMap<>();
        when(adventureConfig.allItems()).thenReturn(allItemsMap);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts WornConditionData to WornCondition")
    void mapToBO_shouldConvertWornConditionDataToWornCondition() {
        WornConditionData data = new WornConditionData();
        data.setId("worn-001");
        data.setThingId("magic-ring");

        when(mockItem.getId()).thenReturn("magic-ring");
        allItemsMap.put("magic-ring", mockItem);

        WornCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("worn-001");
        assertThat(result.getThing()).isEqualTo(mockItem);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves item from AdventureConfig")
    void mapToBO_shouldResolveItemFromAdventureConfig() {
        WornConditionData data = new WornConditionData();
        data.setThingId("golden-crown");

        Item goldenCrown = org.mockito.Mockito.mock(Item.class);
        when(goldenCrown.getId()).thenReturn("golden-crown");
        allItemsMap.put("golden-crown", goldenCrown);

        WornCondition result = mapper.mapToBO(data);

        assertThat(result.getThing()).isEqualTo(goldenCrown);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts WornCondition to WornConditionData")
    void mapToDO_shouldConvertWornConditionToWornConditionData() {
        when(mockItem.getId()).thenReturn("silver-gloves");
        WornCondition condition = new WornCondition(mockItem);
        condition.setId("worn-002");

        WornConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("worn-002");
        assertThat(result.getThingId()).isEqualTo("silver-gloves");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        when(mockItem.getId()).thenReturn("ring-123");

        WornCondition condition1 = new WornCondition(mockItem);
        condition1.setId("worn-id-001");

        WornCondition condition2 = new WornCondition(mockItem);
        condition2.setId("worn-id-002");

        WornConditionData result1 = mapper.mapToDO(condition1);
        WornConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("worn-id-001");
        assertThat(result2.getId()).isEqualTo("worn-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        WornConditionData original = new WornConditionData();
        original.setId("round-trip-worn");
        original.setThingId("crystal-bracelet");

        Item crystalBracelet = org.mockito.Mockito.mock(Item.class);
        when(crystalBracelet.getId()).thenReturn("crystal-bracelet");
        allItemsMap.put("crystal-bracelet", crystalBracelet);

        WornCondition bo = mapper.mapToBO(original);
        WornConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getThingId()).isEqualTo(original.getThingId());
    }
}
```

- [ ] **Step 2: Run the test — expect failure**

```bash
cd server && mvn test -Dtest="WornConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error for `WornConditionMapper`.

- [ ] **Step 3: Create `WornConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/WornConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "WornCondition mapper")
public class WornConditionMapper extends PreConditionMapper<WornConditionData, WornCondition> {

    private final AdventureConfig adventureConfig;

    public WornConditionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(WornConditionData.class, WornCondition.class, this);
    }

    @Override
    public WornCondition mapToBO(WornConditionData data) {
        final Item item = adventureConfig.allItems().get(data.getThingId());
        WornCondition result = new WornCondition(item);
        result.setId(data.getId());
        return result;
    }

    @Override
    public WornConditionData mapToDO(WornCondition condition) {
        WornConditionData result = new WornConditionData();
        result.setThingId(((Item) condition.getThing()).getId());
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="WornConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/WornConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/WornConditionMapperTest.java
git commit -m "feat: add WornConditionMapper with tests"
```

---

## Task 8: PlayerAtConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/PlayerAtConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/PlayerAtConditionMapperTest.java`

`PlayerAtConditionData.locationId` is a String. `PlayerAtCondition` takes `Location` and `GameContext`. Resolve location from `adventureConfig.allLocations()`.

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/PlayerAtConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.condition.PlayerAtCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerAtConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private GameContext gameContext;

    @Mock
    private Location mockLocation;

    private PlayerAtConditionMapper mapper;
    private Map<String, Location> allLocationsMap;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new PlayerAtConditionMapper(mapperSupporter, adventureConfig, gameContext);

        allLocationsMap = new HashMap<>();
        when(adventureConfig.allLocations()).thenReturn(allLocationsMap);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts PlayerAtConditionData to PlayerAtCondition")
    void mapToBO_shouldConvertPlayerAtConditionDataToPlayerAtCondition() {
        PlayerAtConditionData data = new PlayerAtConditionData();
        data.setId("playerAt-001");
        data.setLocationId("forest-clearing");

        when(mockLocation.getId()).thenReturn("forest-clearing");
        allLocationsMap.put("forest-clearing", mockLocation);

        PlayerAtCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("playerAt-001");
        assertThat(result.getLocation()).isEqualTo(mockLocation);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves location from AdventureConfig")
    void mapToBO_shouldResolveLocationFromAdventureConfig() {
        PlayerAtConditionData data = new PlayerAtConditionData();
        data.setLocationId("dark-cave");

        Location darkCave = org.mockito.Mockito.mock(Location.class);
        when(darkCave.getId()).thenReturn("dark-cave");
        allLocationsMap.put("dark-cave", darkCave);

        PlayerAtCondition result = mapper.mapToBO(data);

        assertThat(result.getLocation()).isEqualTo(darkCave);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts PlayerAtCondition to PlayerAtConditionData")
    void mapToDO_shouldConvertPlayerAtConditionToPlayerAtConditionData() {
        when(mockLocation.getId()).thenReturn("town-square");
        PlayerAtCondition condition = new PlayerAtCondition(mockLocation, gameContext);
        condition.setId("playerAt-002");

        PlayerAtConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("playerAt-002");
        assertThat(result.getLocationId()).isEqualTo("town-square");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        when(mockLocation.getId()).thenReturn("loc-123");

        PlayerAtCondition condition1 = new PlayerAtCondition(mockLocation, gameContext);
        condition1.setId("playerAt-id-001");

        PlayerAtCondition condition2 = new PlayerAtCondition(mockLocation, gameContext);
        condition2.setId("playerAt-id-002");

        PlayerAtConditionData result1 = mapper.mapToDO(condition1);
        PlayerAtConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("playerAt-id-001");
        assertThat(result2.getId()).isEqualTo("playerAt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        PlayerAtConditionData original = new PlayerAtConditionData();
        original.setId("round-trip-playerAt");
        original.setLocationId("ancient-ruins");

        Location ancientRuins = org.mockito.Mockito.mock(Location.class);
        when(ancientRuins.getId()).thenReturn("ancient-ruins");
        allLocationsMap.put("ancient-ruins", ancientRuins);

        PlayerAtCondition bo = mapper.mapToBO(original);
        PlayerAtConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getLocationId()).isEqualTo(original.getLocationId());
    }
}
```

- [ ] **Step 2: Run the test — expect failure**

```bash
cd server && mvn test -Dtest="PlayerAtConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error for `PlayerAtConditionMapper`.

- [ ] **Step 3: Create `PlayerAtConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/PlayerAtConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.PlayerAtCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 20, description = "PlayerAtCondition mapper")
public class PlayerAtConditionMapper extends PreConditionMapper<PlayerAtConditionData, PlayerAtCondition> {

    private final AdventureConfig adventureConfig;
    private final GameContext gameContext;

    public PlayerAtConditionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig,
                                   GameContext aGameContext) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        gameContext = aGameContext;
        aMapperSupporter.registerMapper(PlayerAtConditionData.class, PlayerAtCondition.class, this);
    }

    @Override
    public PlayerAtCondition mapToBO(PlayerAtConditionData data) {
        final Location location = adventureConfig.allLocations().get(data.getLocationId());
        PlayerAtCondition result = new PlayerAtCondition(location, gameContext);
        result.setId(data.getId());
        return result;
    }

    @Override
    public PlayerAtConditionData mapToDO(PlayerAtCondition condition) {
        PlayerAtConditionData result = new PlayerAtConditionData();
        result.setLocationId(condition.getLocation().getId());
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="PlayerAtConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/PlayerAtConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/PlayerAtConditionMapperTest.java
git commit -m "feat: add PlayerAtConditionMapper with tests"
```

---

## Task 9: ItemAtConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/ItemAtConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/ItemAtConditionMapperTest.java`

`ItemAtConditionData` has `locationId` and `thingId`. `ItemAtCondition(Item thing, Location location)` — note argument order: item first, then location. No `GameContext` needed. Resolve both from `AdventureConfig`.

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/ItemAtConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.condition.ItemAtCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemAtConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private Item mockItem;

    @Mock
    private Location mockLocation;

    private ItemAtConditionMapper mapper;
    private Map<String, Item> allItemsMap;
    private Map<String, Location> allLocationsMap;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new ItemAtConditionMapper(mapperSupporter, adventureConfig);

        allItemsMap = new HashMap<>();
        allLocationsMap = new HashMap<>();
        when(adventureConfig.allItems()).thenReturn(allItemsMap);
        when(adventureConfig.allLocations()).thenReturn(allLocationsMap);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts ItemAtConditionData to ItemAtCondition")
    void mapToBO_shouldConvertItemAtConditionDataToItemAtCondition() {
        ItemAtConditionData data = new ItemAtConditionData();
        data.setId("itemAt-001");
        data.setThingId("golden-key");
        data.setLocationId("treasure-room");

        when(mockItem.getId()).thenReturn("golden-key");
        when(mockLocation.getId()).thenReturn("treasure-room");
        allItemsMap.put("golden-key", mockItem);
        allLocationsMap.put("treasure-room", mockLocation);

        ItemAtCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("itemAt-001");
        assertThat(result.getThing()).isEqualTo(mockItem);
        assertThat(result.getLocation()).isEqualTo(mockLocation);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves both item and location from AdventureConfig")
    void mapToBO_shouldResolveBothItemAndLocationFromAdventureConfig() {
        ItemAtConditionData data = new ItemAtConditionData();
        data.setThingId("silver-sword");
        data.setLocationId("armoury");

        Item silverSword = org.mockito.Mockito.mock(Item.class);
        Location armoury = org.mockito.Mockito.mock(Location.class);
        allItemsMap.put("silver-sword", silverSword);
        allLocationsMap.put("armoury", armoury);

        ItemAtCondition result = mapper.mapToBO(data);

        assertThat(result.getThing()).isEqualTo(silverSword);
        assertThat(result.getLocation()).isEqualTo(armoury);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts ItemAtCondition to ItemAtConditionData")
    void mapToDO_shouldConvertItemAtConditionToItemAtConditionData() {
        when(mockItem.getId()).thenReturn("old-lamp");
        when(mockLocation.getId()).thenReturn("cave-entrance");
        ItemAtCondition condition = new ItemAtCondition(mockItem, mockLocation);
        condition.setId("itemAt-002");

        ItemAtConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("itemAt-002");
        assertThat(result.getThingId()).isEqualTo("old-lamp");
        assertThat(result.getLocationId()).isEqualTo("cave-entrance");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        when(mockItem.getId()).thenReturn("item-123");
        when(mockLocation.getId()).thenReturn("loc-123");

        ItemAtCondition condition1 = new ItemAtCondition(mockItem, mockLocation);
        condition1.setId("itemAt-id-001");

        ItemAtCondition condition2 = new ItemAtCondition(mockItem, mockLocation);
        condition2.setId("itemAt-id-002");

        ItemAtConditionData result1 = mapper.mapToDO(condition1);
        ItemAtConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("itemAt-id-001");
        assertThat(result2.getId()).isEqualTo("itemAt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        ItemAtConditionData original = new ItemAtConditionData();
        original.setId("round-trip-itemAt");
        original.setThingId("ruby-gem");
        original.setLocationId("hidden-vault");

        Item rubyGem = org.mockito.Mockito.mock(Item.class);
        Location hiddenVault = org.mockito.Mockito.mock(Location.class);
        when(rubyGem.getId()).thenReturn("ruby-gem");
        when(hiddenVault.getId()).thenReturn("hidden-vault");
        allItemsMap.put("ruby-gem", rubyGem);
        allLocationsMap.put("hidden-vault", hiddenVault);

        ItemAtCondition bo = mapper.mapToBO(original);
        ItemAtConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getThingId()).isEqualTo(original.getThingId());
        assertThat(roundTrip.getLocationId()).isEqualTo(original.getLocationId());
    }
}
```

- [ ] **Step 2: Run the test — expect failure**

```bash
cd server && mvn test -Dtest="ItemAtConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error for `ItemAtConditionMapper`.

- [ ] **Step 3: Create `ItemAtConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/ItemAtConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.ItemAtCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@Service
@AutoRegisterMapper(priority = 20, description = "ItemAtCondition mapper")
public class ItemAtConditionMapper extends PreConditionMapper<ItemAtConditionData, ItemAtCondition> {

    private final AdventureConfig adventureConfig;

    public ItemAtConditionMapper(MapperSupporter aMapperSupporter, @Lazy AdventureConfig anAdventureConfig) {
        super(aMapperSupporter);
        adventureConfig = anAdventureConfig;
        aMapperSupporter.registerMapper(ItemAtConditionData.class, ItemAtCondition.class, this);
    }

    @Override
    public ItemAtCondition mapToBO(ItemAtConditionData data) {
        final Item item = adventureConfig.allItems().get(data.getThingId());
        final Location location = adventureConfig.allLocations().get(data.getLocationId());
        ItemAtCondition result = new ItemAtCondition(item, location);
        result.setId(data.getId());
        return result;
    }

    @Override
    public ItemAtConditionData mapToDO(ItemAtCondition condition) {
        ItemAtConditionData result = new ItemAtConditionData();
        result.setThingId(condition.getThing().getId());
        result.setLocationId(condition.getLocation().getId());
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="ItemAtConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/ItemAtConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/ItemAtConditionMapperTest.java
git commit -m "feat: add ItemAtConditionMapper with tests"
```

---

## Task 10: AndConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/AndConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/AndConditionMapperTest.java`

`AndConditionData` now embeds two `PreConditionData` objects (after Task 1). The mapper delegates to sub-mappers looked up by class at runtime — identical pattern to `NotConditionMapper` but with two children. `AndCondition` now exposes `getPreCondition()` and `getAnotherPreCondition()` (added in Task 2).

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/AndConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.condition.AndConditionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.condition.AndCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AndConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private Mapper innerMapper;

    @Mock
    private PreCondition firstConditionBO;

    @Mock
    private PreCondition secondConditionBO;

    @Mock
    private PreConditionData firstConditionData;

    @Mock
    private PreConditionData secondConditionData;

    @Mock
    private GameContext gameContext;

    @Mock
    private Item mockItem;

    private AndConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new AndConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts AndConditionData to AndCondition")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldConvertAndConditionDataToAndCondition() {
        AndConditionData data = new AndConditionData();
        data.setId("and-001");
        data.setPreCondition(firstConditionData);
        data.setAnotherPreCondition(secondConditionData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(firstConditionData)).thenReturn(firstConditionBO);
        when(innerMapper.mapToBO(secondConditionData)).thenReturn(secondConditionBO);

        AndCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("and-001");
        assertThat(result.getPreCondition()).isEqualTo(firstConditionBO);
        assertThat(result.getAnotherPreCondition()).isEqualTo(secondConditionBO);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves nested condition mappers dynamically")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldResolveNestedConditionMappersDynamically() {
        CarriedConditionData carriedData = new CarriedConditionData();
        HereConditionData hereData = new HereConditionData();

        AndConditionData data = new AndConditionData();
        data.setPreCondition(carriedData);
        data.setAnotherPreCondition(hereData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(any())).thenReturn(firstConditionBO);

        mapper.mapToBO(data);

        verify(mapperSupporter).getMapper(CarriedConditionData.class);
        verify(mapperSupporter).getMapper(HereConditionData.class);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts AndCondition to AndConditionData")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldConvertAndConditionToAndConditionData() {
        AndCondition condition = new AndCondition(firstConditionBO, secondConditionBO);
        condition.setId("and-002");

        when(mapperSupporter.getMapper(firstConditionBO.getClass())).thenReturn(innerMapper);
        when(mapperSupporter.getMapper(secondConditionBO.getClass())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(firstConditionBO)).thenReturn(firstConditionData);
        when(innerMapper.mapToDO(secondConditionBO)).thenReturn(secondConditionData);

        AndConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("and-002");
        assertThat(result.getPreCondition()).isEqualTo(firstConditionData);
        assertThat(result.getAnotherPreCondition()).isEqualTo(secondConditionData);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldPreserveIdDuringConversion() {
        AndCondition condition1 = new AndCondition(firstConditionBO, secondConditionBO);
        condition1.setId("and-id-001");

        AndCondition condition2 = new AndCondition(firstConditionBO, secondConditionBO);
        condition2.setId("and-id-002");

        when(mapperSupporter.getMapper(any())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(any())).thenReturn(firstConditionData);

        AndConditionData result1 = mapper.mapToDO(condition1);
        AndConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("and-id-001");
        assertThat(result2.getId()).isEqualTo("and-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves structure")
    @SuppressWarnings("unchecked")
    void roundTripMapping_shouldPreserveInformation() {
        CarriedConditionData carriedData = new CarriedConditionData();
        HereConditionData hereData = new HereConditionData();

        AndConditionData original = new AndConditionData();
        original.setId("round-trip-and");
        original.setPreCondition(carriedData);
        original.setAnotherPreCondition(hereData);

        // mapToBO path
        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(carriedData)).thenReturn(firstConditionBO);
        when(innerMapper.mapToBO(hereData)).thenReturn(secondConditionBO);

        // mapToDO path
        when(mapperSupporter.getMapper(firstConditionBO.getClass())).thenReturn(innerMapper);
        when(mapperSupporter.getMapper(secondConditionBO.getClass())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(firstConditionBO)).thenReturn(carriedData);
        when(innerMapper.mapToDO(secondConditionBO)).thenReturn(hereData);

        AndCondition bo = mapper.mapToBO(original);
        AndConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getPreCondition()).isEqualTo(original.getPreCondition());
        assertThat(roundTrip.getAnotherPreCondition()).isEqualTo(original.getAnotherPreCondition());
    }
}
```

- [ ] **Step 2: Run the test — expect failure**

```bash
cd server && mvn test -Dtest="AndConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error for `AndConditionMapper`.

- [ ] **Step 3: Create `AndConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/AndConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.condition.AndConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.AndCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 10, description = "AndCondition mapper")
public class AndConditionMapper extends PreConditionMapper<AndConditionData, AndCondition> {

    public AndConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(AndConditionData.class, AndCondition.class, this);
    }

    @Override
    public AndCondition mapToBO(AndConditionData data) {
        final PreCondition first = (PreCondition) getMapperSupporter()
                .getMapper(data.getPreCondition().getClass()).mapToBO(data.getPreCondition());
        final PreCondition second = (PreCondition) getMapperSupporter()
                .getMapper(data.getAnotherPreCondition().getClass()).mapToBO(data.getAnotherPreCondition());
        AndCondition result = new AndCondition(first, second);
        result.setId(data.getId());
        return result;
    }

    @Override
    public AndConditionData mapToDO(AndCondition condition) {
        final PreConditionData first = (PreConditionData) getMapperSupporter()
                .getMapper(condition.getPreCondition().getClass()).mapToDO(condition.getPreCondition());
        final PreConditionData second = (PreConditionData) getMapperSupporter()
                .getMapper(condition.getAnotherPreCondition().getClass()).mapToDO(condition.getAnotherPreCondition());
        AndConditionData result = new AndConditionData();
        result.setPreCondition(first);
        result.setAnotherPreCondition(second);
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="AndConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/AndConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/AndConditionMapperTest.java
git commit -m "feat: add AndConditionMapper with tests"
```

---

## Task 11: OrConditionMapper

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/server/mapper/condition/OrConditionMapper.java`
- Create: `server/src/test/java/com/pdg/adventure/server/mapper/condition/OrConditionMapperTest.java`

Identical structure to `AndConditionMapper`. `OrConditionData` now embeds two `PreConditionData` objects (after Task 1). `OrCondition` exposes `getPreCondition()` and `getAnotherPreCondition()` (added in Task 2).

- [ ] **Step 1: Write the failing test**

`server/src/test/java/com/pdg/adventure/server/mapper/condition/OrConditionMapperTest.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.OrConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.condition.OrCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private Mapper innerMapper;

    @Mock
    private PreCondition firstConditionBO;

    @Mock
    private PreCondition secondConditionBO;

    @Mock
    private PreConditionData firstConditionData;

    @Mock
    private PreConditionData secondConditionData;

    private OrConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new OrConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts OrConditionData to OrCondition")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldConvertOrConditionDataToOrCondition() {
        OrConditionData data = new OrConditionData();
        data.setId("or-001");
        data.setPreCondition(firstConditionData);
        data.setAnotherPreCondition(secondConditionData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(firstConditionData)).thenReturn(firstConditionBO);
        when(innerMapper.mapToBO(secondConditionData)).thenReturn(secondConditionBO);

        OrCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("or-001");
        assertThat(result.getPreCondition()).isEqualTo(firstConditionBO);
        assertThat(result.getAnotherPreCondition()).isEqualTo(secondConditionBO);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves nested condition mappers dynamically")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldResolveNestedConditionMappersDynamically() {
        CarriedConditionData carriedData = new CarriedConditionData();
        HereConditionData hereData = new HereConditionData();

        OrConditionData data = new OrConditionData();
        data.setPreCondition(carriedData);
        data.setAnotherPreCondition(hereData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(any())).thenReturn(firstConditionBO);

        mapper.mapToBO(data);

        verify(mapperSupporter).getMapper(CarriedConditionData.class);
        verify(mapperSupporter).getMapper(HereConditionData.class);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts OrCondition to OrConditionData")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldConvertOrConditionToOrConditionData() {
        OrCondition condition = new OrCondition(firstConditionBO, secondConditionBO);
        condition.setId("or-002");

        when(mapperSupporter.getMapper(firstConditionBO.getClass())).thenReturn(innerMapper);
        when(mapperSupporter.getMapper(secondConditionBO.getClass())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(firstConditionBO)).thenReturn(firstConditionData);
        when(innerMapper.mapToDO(secondConditionBO)).thenReturn(secondConditionData);

        OrConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("or-002");
        assertThat(result.getPreCondition()).isEqualTo(firstConditionData);
        assertThat(result.getAnotherPreCondition()).isEqualTo(secondConditionData);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldPreserveIdDuringConversion() {
        OrCondition condition1 = new OrCondition(firstConditionBO, secondConditionBO);
        condition1.setId("or-id-001");

        OrCondition condition2 = new OrCondition(firstConditionBO, secondConditionBO);
        condition2.setId("or-id-002");

        when(mapperSupporter.getMapper(any())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(any())).thenReturn(firstConditionData);

        OrConditionData result1 = mapper.mapToDO(condition1);
        OrConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("or-id-001");
        assertThat(result2.getId()).isEqualTo("or-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves structure")
    @SuppressWarnings("unchecked")
    void roundTripMapping_shouldPreserveInformation() {
        CarriedConditionData carriedData = new CarriedConditionData();
        HereConditionData hereData = new HereConditionData();

        OrConditionData original = new OrConditionData();
        original.setId("round-trip-or");
        original.setPreCondition(carriedData);
        original.setAnotherPreCondition(hereData);

        // mapToBO path
        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(carriedData)).thenReturn(firstConditionBO);
        when(innerMapper.mapToBO(hereData)).thenReturn(secondConditionBO);

        // mapToDO path
        when(mapperSupporter.getMapper(firstConditionBO.getClass())).thenReturn(innerMapper);
        when(mapperSupporter.getMapper(secondConditionBO.getClass())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(firstConditionBO)).thenReturn(carriedData);
        when(innerMapper.mapToDO(secondConditionBO)).thenReturn(hereData);

        OrCondition bo = mapper.mapToBO(original);
        OrConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getPreCondition()).isEqualTo(original.getPreCondition());
        assertThat(roundTrip.getAnotherPreCondition()).isEqualTo(original.getAnotherPreCondition());
    }
}
```

- [ ] **Step 2: Run the test — expect failure**

```bash
cd server && mvn test -Dtest="OrConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: compilation error for `OrConditionMapper`.

- [ ] **Step 3: Create `OrConditionMapper`**

`server/src/main/java/com/pdg/adventure/server/mapper/condition/OrConditionMapper.java`:

```java
package com.pdg.adventure.server.mapper.condition;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.condition.OrConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.condition.OrCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 10, description = "OrCondition mapper")
public class OrConditionMapper extends PreConditionMapper<OrConditionData, OrCondition> {

    public OrConditionMapper(MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        aMapperSupporter.registerMapper(OrConditionData.class, OrCondition.class, this);
    }

    @Override
    public OrCondition mapToBO(OrConditionData data) {
        final PreCondition first = (PreCondition) getMapperSupporter()
                .getMapper(data.getPreCondition().getClass()).mapToBO(data.getPreCondition());
        final PreCondition second = (PreCondition) getMapperSupporter()
                .getMapper(data.getAnotherPreCondition().getClass()).mapToBO(data.getAnotherPreCondition());
        OrCondition result = new OrCondition(first, second);
        result.setId(data.getId());
        return result;
    }

    @Override
    public OrConditionData mapToDO(OrCondition condition) {
        final PreConditionData first = (PreConditionData) getMapperSupporter()
                .getMapper(condition.getPreCondition().getClass()).mapToDO(condition.getPreCondition());
        final PreConditionData second = (PreConditionData) getMapperSupporter()
                .getMapper(condition.getAnotherPreCondition().getClass()).mapToDO(condition.getAnotherPreCondition());
        OrConditionData result = new OrConditionData();
        result.setPreCondition(first);
        result.setAnotherPreCondition(second);
        result.setId(condition.getId());
        return result;
    }
}
```

- [ ] **Step 4: Run the test — expect all 5 to pass**

```bash
cd server && mvn test -Dtest="OrConditionMapperTest" -DfailIfNoTests=false 2>&1 | tail -10
```

Expected: `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: Run the full test suite**

```bash
cd server && mvn test "-Dexclude=**/storage/**Test.java,**/AdventureBuilderTest.java" 2>&1 | tail -30
```

Expected: all existing tests still pass plus the new condition mapper tests.

- [ ] **Step 6: Commit**

```bash
cd server && git add \
  src/main/java/com/pdg/adventure/server/mapper/condition/OrConditionMapper.java \
  src/test/java/com/pdg/adventure/server/mapper/condition/OrConditionMapperTest.java
git commit -m "feat: add OrConditionMapper with tests"
```
