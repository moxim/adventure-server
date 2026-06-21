# Condition List Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a functional condition list editor embedded in `PreconditionActionEditor` that lets users add, edit, remove, and negate leaf preconditions on a command using an inline-expand (accordion) layout.

**Architecture:** Parallel hierarchy to the existing action editor pattern: `ConditionEditorComponent` (abstract base) → 9 concrete leaf editors → `ConditionEditorFactory` (switch) + `ConditionSelector` (combobox) + `ConditionRow` (Details accordion row with negate checkbox) + `ConditionListEditor` (container). `PreconditionActionEditor` is refactored to replace its Grid stub with `ConditionListEditor`.

**Tech Stack:** Java 25, Vaadin 24 Flow (Details, ComboBox, Checkbox, Button, TextField), Lombok, JUnit 5, AssertJ

---

## File Structure

**New — `src/main/java/com/pdg/adventure/view/command/condition/`:**
- `ConditionEditorComponent.java` — abstract base (mirrors `ActionEditorComponent`)
- `ConditionEditorSupport.java` — package-private static helpers (item/location list collection)
- `ConditionEditorFactory.java` — static switch expression creating the right editor
- `ConditionSelector.java` — ComboBox + Add button for picking condition type
- `ConditionRow.java` — extends `Details`; summary = type name, content = negate + remove + editor
- `ConditionListEditor.java` — VerticalLayout holding rows + selector
- `CarriedConditionEditor.java` — ComboBox<ItemData>
- `HereConditionEditor.java` — ComboBox<ItemData>
- `WornConditionEditor.java` — ComboBox<ItemData> (wearable only)
- `PlayerAtConditionEditor.java` — ComboBox<LocationData>
- `ItemAtConditionEditor.java` — ComboBox<ItemData> + ComboBox<LocationData>
- `EqualsConditionEditor.java` — TextField (variable) + TextField (value)
- `GreaterThanConditionEditor.java` — TextField (variable) + TextField (number value)
- `LowerThanConditionEditor.java` — TextField (variable) + TextField (number value)
- `SameConditionEditor.java` — TextField (variable 1) + TextField (variable 2)

**New — `src/test/java/com/pdg/adventure/view/command/condition/`:**
- `ConditionEditorFactoryTest.java`
- `ConditionRowTest.java`
- `ConditionListEditorTest.java`
- `CarriedConditionEditorTest.java`
- `HereConditionEditorTest.java`
- `WornConditionEditorTest.java`
- `PlayerAtConditionEditorTest.java`
- `ItemAtConditionEditorTest.java`
- `EqualsConditionEditorTest.java`
- `GreaterThanConditionEditorTest.java`
- `LowerThanConditionEditorTest.java`
- `SameConditionEditorTest.java`

**Modified:**
- `src/main/java/com/pdg/adventure/model/condition/EqualsConditionData.java` — remove `final` from fields, add `@AllArgsConstructor`
- `src/main/java/com/pdg/adventure/view/command/PreconditionActionEditor.java` — replace Grid stub with `ConditionListEditor`

---

### Task 1: EqualsConditionData — make fields mutable

`EqualsConditionData` has `final` fields so Lombok generates no setters. The editor needs to mutate them via setters. Adding `@AllArgsConstructor` preserves the existing 2-arg constructor used by `EqualsConditionMapper` and its tests.

**Files:**
- Modify: `src/main/java/com/pdg/adventure/model/condition/EqualsConditionData.java`

- [ ] **Step 1: Open the file and note the current state**

```java
// Current:
public class EqualsConditionData extends PreConditionData {
    private final String variableName;
    private final String value;
}
```

- [ ] **Step 2: Apply the change**

```java
package com.pdg.adventure.model.condition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class EqualsConditionData extends PreConditionData {
    private String variableName;
    private String value;
}
```

- [ ] **Step 3: Run existing mapper tests to verify nothing broke**

```bash
cd server && mvn test -pl . -Dtest="EqualsConditionMapperTest" -q
```

Expected: `BUILD SUCCESS`, 5 tests pass.

- [ ] **Step 4: Run full test suite**

```bash
cd server && mvn test -q
```

Expected: `BUILD SUCCESS`, all existing tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/model/condition/EqualsConditionData.java
git commit -m "refactor: make EqualsConditionData fields mutable for editor support"
```

---

### Task 2: ConditionEditorComponent + ConditionEditorSupport

Foundation classes. No separate test file — the abstract base is tested indirectly through the concrete editor tests. The support class is tested through editor tests.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/ConditionEditorComponent.java`
- Create: `src/main/java/com/pdg/adventure/view/command/condition/ConditionEditorSupport.java`

- [ ] **Step 1: Create `ConditionEditorComponent`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.pdg.adventure.model.condition.PreConditionData;

public abstract class ConditionEditorComponent extends VerticalLayout {
    protected final PreConditionData conditionData;

    protected ConditionEditorComponent(PreConditionData conditionData) {
        this.conditionData = conditionData;
        setPadding(true);
        setSpacing(true);
    }

    public final void initialize() {
        buildUI();
    }

    protected abstract void buildUI();

    public PreConditionData getConditionData() {
        return conditionData;
    }

    public abstract boolean validate();

    public abstract String getConditionSummary();
}
```

- [ ] **Step 2: Create `ConditionEditorSupport`**

```java
package com.pdg.adventure.view.command.condition;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;

class ConditionEditorSupport {
    private ConditionEditorSupport() {}

    static List<ItemData> allItems(AdventureData data) {
        List<ItemData> items = new ArrayList<>();
        data.getLocationData().values().forEach(loc -> {
            ItemContainerData container = loc.getItemContainerData();
            if (container != null && container.getItems() != null) {
                items.addAll(container.getItems());
            }
        });
        if (data.getPlayerPocket() != null && data.getPlayerPocket().getItems() != null) {
            items.addAll(data.getPlayerPocket().getItems());
        }
        return items;
    }

    static List<ItemData> wearableItems(AdventureData data) {
        return allItems(data).stream().filter(ItemData::isWearable).toList();
    }

    static List<LocationData> allLocations(AdventureData data) {
        return new ArrayList<>(data.getLocationData().values());
    }
}
```

- [ ] **Step 3: Verify compilation**

```bash
cd server && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/
git commit -m "feat: add ConditionEditorComponent abstract base and ConditionEditorSupport"
```

---

### Task 3: CarriedConditionEditor + HereConditionEditor

Both editors show a single item picker. `CarriedConditionData.itemId` and `HereConditionData.thingId` are the fields being bound.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/CarriedConditionEditor.java`
- Create: `src/main/java/com/pdg/adventure/view/command/condition/HereConditionEditor.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/CarriedConditionEditorTest.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/HereConditionEditorTest.java`

- [ ] **Step 1: Write `CarriedConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.CarriedConditionData;

class CarriedConditionEditorTest {

    private AdventureData adventureData;
    private ItemData item1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Golden Sword");
        item1.setDescriptionData(desc);

        LocationData loc = new LocationData();
        loc.setId("loc-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        List<ItemData> items = new ArrayList<>();
        items.add(item1);
        container.setItems(items);
        loc.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc.getId(), loc);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNoItemSelected_returnsFalse() {
        CarriedConditionData data = new CarriedConditionData();
        CarriedConditionEditor editor = new CarriedConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withItemPreSelected_returnsTrue() {
        CarriedConditionData data = new CarriedConditionData();
        data.setItemId(item1.getId());
        CarriedConditionEditor editor = new CarriedConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        CarriedConditionData data = new CarriedConditionData();
        CarriedConditionEditor editor = new CarriedConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        CarriedConditionEditor editor = new CarriedConditionEditor(new CarriedConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withNoSelection_returnsNone() {
        CarriedConditionEditor editor = new CarriedConditionEditor(new CarriedConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }
}
```

- [ ] **Step 2: Run test to confirm compilation failure**

```bash
cd server && mvn test -Dtest="CarriedConditionEditorTest" -q 2>&1 | tail -5
```

Expected: compilation error — `CarriedConditionEditor` does not exist.

- [ ] **Step 3: Implement `CarriedConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class CarriedConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final CarriedConditionData carriedData;
    private ComboBox<ItemData> itemSelector;

    public CarriedConditionEditor(CarriedConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.carriedData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> items = ConditionEditorSupport.allItems(adventureData);
        itemSelector = new ComboBox<>("Item");
        itemSelector.setItems(items);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (carriedData.getItemId() != null) {
            items.stream().filter(i -> i.getId().equals(carriedData.getItemId()))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        itemSelector.addValueChangeListener(e ->
            carriedData.setItemId(e.getValue() != null ? e.getValue().getId() : null));

        add(itemSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        itemSelector.setInvalid(!valid);
        if (!valid) itemSelector.setErrorMessage("Please select an item");
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }
}
```

- [ ] **Step 4: Write `HereConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.HereConditionData;

class HereConditionEditorTest {

    private AdventureData adventureData;
    private ItemData item1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        item1 = new ItemData();
        item1.setId("item-here-1");
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Torch");
        item1.setDescriptionData(desc);
        LocationData loc = new LocationData();
        loc.setId("loc-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        List<ItemData> items = new ArrayList<>();
        items.add(item1);
        container.setItems(items);
        loc.setItemContainerData(container);
        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc.getId(), loc);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNoItemSelected_returnsFalse() {
        HereConditionEditor editor = new HereConditionEditor(new HereConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withItemPreSelected_returnsTrue() {
        HereConditionData data = new HereConditionData();
        data.setThingId(item1.getId());
        HereConditionEditor editor = new HereConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        HereConditionData data = new HereConditionData();
        HereConditionEditor editor = new HereConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        HereConditionEditor editor = new HereConditionEditor(new HereConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
```

- [ ] **Step 5: Implement `HereConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class HereConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final HereConditionData hereData;
    private ComboBox<ItemData> itemSelector;

    public HereConditionEditor(HereConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.hereData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> items = ConditionEditorSupport.allItems(adventureData);
        itemSelector = new ComboBox<>("Item");
        itemSelector.setItems(items);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (hereData.getThingId() != null) {
            items.stream().filter(i -> i.getId().equals(hereData.getThingId()))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        itemSelector.addValueChangeListener(e ->
            hereData.setThingId(e.getValue() != null ? e.getValue().getId() : null));

        add(itemSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        itemSelector.setInvalid(!valid);
        if (!valid) itemSelector.setErrorMessage("Please select an item");
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }
}
```

- [ ] **Step 6: Run tests**

```bash
cd server && mvn test -Dtest="CarriedConditionEditorTest,HereConditionEditorTest" -q
```

Expected: `BUILD SUCCESS`, all tests pass.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/ \
        src/test/java/com/pdg/adventure/view/command/condition/
git commit -m "feat: add CarriedConditionEditor and HereConditionEditor"
```

---

### Task 4: WornConditionEditor + PlayerAtConditionEditor

`WornConditionEditor` filters items by `isWearable`. `PlayerAtConditionEditor` shows a location picker.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/WornConditionEditor.java`
- Create: `src/main/java/com/pdg/adventure/view/command/condition/PlayerAtConditionEditor.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/WornConditionEditorTest.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/PlayerAtConditionEditorTest.java`

- [ ] **Step 1: Write `WornConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.WornConditionData;

class WornConditionEditorTest {

    private AdventureData adventureData;
    private ItemData wearableItem;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        wearableItem = new ItemData();
        wearableItem.setId("cloak-1");
        wearableItem.setWearable(true);
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Magic Cloak");
        wearableItem.setDescriptionData(desc);

        ItemData nonWearable = new ItemData();
        nonWearable.setId("rock-1");
        nonWearable.setWearable(false);
        DescriptionData desc2 = new DescriptionData();
        desc2.setShortDescription("Rock");
        nonWearable.setDescriptionData(desc2);

        LocationData loc = new LocationData();
        loc.setId("loc-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        List<ItemData> items = new ArrayList<>();
        items.add(wearableItem);
        items.add(nonWearable);
        container.setItems(items);
        loc.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc.getId(), loc);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNoItemSelected_returnsFalse() {
        WornConditionEditor editor = new WornConditionEditor(new WornConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withWearableItemPreSelected_returnsTrue() {
        WornConditionData data = new WornConditionData();
        data.setThingId(wearableItem.getId());
        WornConditionEditor editor = new WornConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        WornConditionData data = new WornConditionData();
        WornConditionEditor editor = new WornConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        WornConditionEditor editor = new WornConditionEditor(new WornConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
```

- [ ] **Step 2: Implement `WornConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class WornConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final WornConditionData wornData;
    private ComboBox<ItemData> itemSelector;

    public WornConditionEditor(WornConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.wornData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> items = ConditionEditorSupport.wearableItems(adventureData);
        itemSelector = new ComboBox<>("Wearable Item");
        itemSelector.setItems(items);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select wearable item");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (wornData.getThingId() != null) {
            items.stream().filter(i -> i.getId().equals(wornData.getThingId()))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        itemSelector.addValueChangeListener(e ->
            wornData.setThingId(e.getValue() != null ? e.getValue().getId() : null));

        add(itemSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        itemSelector.setInvalid(!valid);
        if (!valid) itemSelector.setErrorMessage("Please select a wearable item");
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }
}
```

- [ ] **Step 3: Write `PlayerAtConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;

class PlayerAtConditionEditorTest {

    private AdventureData adventureData;
    private LocationData loc1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        loc1 = new LocationData();
        loc1.setId("loc-1");
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Dark Cave");
        loc1.setDescriptionData(desc);
        loc1.setItemContainerData(new ItemContainerData("loc-1"));

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc1.getId(), loc1);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNoLocationSelected_returnsFalse() {
        PlayerAtConditionEditor editor = new PlayerAtConditionEditor(new PlayerAtConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withLocationPreSelected_returnsTrue() {
        PlayerAtConditionData data = new PlayerAtConditionData();
        data.setLocationId(loc1.getId());
        PlayerAtConditionEditor editor = new PlayerAtConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        PlayerAtConditionData data = new PlayerAtConditionData();
        PlayerAtConditionEditor editor = new PlayerAtConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        PlayerAtConditionEditor editor = new PlayerAtConditionEditor(new PlayerAtConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
```

- [ ] **Step 4: Implement `PlayerAtConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class PlayerAtConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final PlayerAtConditionData playerAtData;
    private ComboBox<LocationData> locationSelector;

    public PlayerAtConditionEditor(PlayerAtConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.playerAtData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<LocationData> locations = ConditionEditorSupport.allLocations(adventureData);
        locationSelector = new ComboBox<>("Location");
        locationSelector.setItems(locations);
        locationSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        locationSelector.setPlaceholder("Select location");
        locationSelector.setWidthFull();
        locationSelector.setRequired(true);

        if (playerAtData.getLocationId() != null) {
            locations.stream().filter(l -> l.getId().equals(playerAtData.getLocationId()))
                     .findFirst().ifPresent(locationSelector::setValue);
        }
        locationSelector.addValueChangeListener(e ->
            playerAtData.setLocationId(e.getValue() != null ? e.getValue().getId() : null));

        add(locationSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = locationSelector.getValue() != null;
        locationSelector.setInvalid(!valid);
        if (!valid) locationSelector.setErrorMessage("Please select a location");
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (locationSelector == null || locationSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(locationSelector.getValue());
    }
}
```

- [ ] **Step 5: Run tests**

```bash
cd server && mvn test -Dtest="WornConditionEditorTest,PlayerAtConditionEditorTest" -q
```

Expected: `BUILD SUCCESS`, all tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/ \
        src/test/java/com/pdg/adventure/view/command/condition/
git commit -m "feat: add WornConditionEditor and PlayerAtConditionEditor"
```

---

### Task 5: ItemAtConditionEditor

Two pickers: an item and a location. Both must be set for validation to pass.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/ItemAtConditionEditor.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/ItemAtConditionEditorTest.java`

- [ ] **Step 1: Write `ItemAtConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.ItemAtConditionData;

class ItemAtConditionEditorTest {

    private AdventureData adventureData;
    private ItemData item1;
    private LocationData loc1;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        item1 = new ItemData();
        item1.setId("item-1");
        DescriptionData iDesc = new DescriptionData();
        iDesc.setShortDescription("Key");
        item1.setDescriptionData(iDesc);

        loc1 = new LocationData();
        loc1.setId("loc-1");
        DescriptionData lDesc = new DescriptionData();
        lDesc.setShortDescription("Dungeon");
        loc1.setDescriptionData(lDesc);

        ItemContainerData container = new ItemContainerData("loc-1");
        List<ItemData> items = new ArrayList<>();
        items.add(item1);
        container.setItems(items);
        loc1.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(loc1.getId(), loc1);
        adventureData.setLocationData(locations);
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void validate_withNothingSelected_returnsFalse() {
        ItemAtConditionEditor editor = new ItemAtConditionEditor(new ItemAtConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withBothPreSelected_returnsTrue() {
        ItemAtConditionData data = new ItemAtConditionData();
        data.setThingId(item1.getId());
        data.setLocationId(loc1.getId());
        ItemAtConditionEditor editor = new ItemAtConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        ItemAtConditionData data = new ItemAtConditionData();
        ItemAtConditionEditor editor = new ItemAtConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        ItemAtConditionEditor editor = new ItemAtConditionEditor(new ItemAtConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
```

- [ ] **Step 2: Run test to confirm compilation failure**

```bash
cd server && mvn test -Dtest="ItemAtConditionEditorTest" -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement `ItemAtConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class ItemAtConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final ItemAtConditionData itemAtData;
    private ComboBox<ItemData> itemSelector;
    private ComboBox<LocationData> locationSelector;

    public ItemAtConditionEditor(ItemAtConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.itemAtData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> items = ConditionEditorSupport.allItems(adventureData);
        itemSelector = new ComboBox<>("Item");
        itemSelector.setItems(items);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        List<LocationData> locations = ConditionEditorSupport.allLocations(adventureData);
        locationSelector = new ComboBox<>("Location");
        locationSelector.setItems(locations);
        locationSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        locationSelector.setPlaceholder("Select location");
        locationSelector.setWidthFull();
        locationSelector.setRequired(true);

        if (itemAtData.getThingId() != null) {
            items.stream().filter(i -> i.getId().equals(itemAtData.getThingId()))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        if (itemAtData.getLocationId() != null) {
            locations.stream().filter(l -> l.getId().equals(itemAtData.getLocationId()))
                     .findFirst().ifPresent(locationSelector::setValue);
        }

        itemSelector.addValueChangeListener(e ->
            itemAtData.setThingId(e.getValue() != null ? e.getValue().getId() : null));
        locationSelector.addValueChangeListener(e ->
            itemAtData.setLocationId(e.getValue() != null ? e.getValue().getId() : null));

        add(itemSelector, locationSelector);
    }

    @Override
    public boolean validate() {
        boolean itemValid = itemSelector.getValue() != null;
        boolean locValid = locationSelector.getValue() != null;
        itemSelector.setInvalid(!itemValid);
        locationSelector.setInvalid(!locValid);
        if (!itemValid) itemSelector.setErrorMessage("Please select an item");
        if (!locValid) locationSelector.setErrorMessage("Please select a location");
        return itemValid && locValid;
    }

    @Override
    public String getConditionSummary() {
        String item = (itemSelector != null && itemSelector.getValue() != null)
                ? ViewSupporter.formatDescription(itemSelector.getValue()) : "?";
        String loc = (locationSelector != null && locationSelector.getValue() != null)
                ? ViewSupporter.formatDescription(locationSelector.getValue()) : "?";
        return item + " @ " + loc;
    }
}
```

- [ ] **Step 4: Run tests**

```bash
cd server && mvn test -Dtest="ItemAtConditionEditorTest" -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/ItemAtConditionEditor.java \
        src/test/java/com/pdg/adventure/view/command/condition/ItemAtConditionEditorTest.java
git commit -m "feat: add ItemAtConditionEditor"
```

---

### Task 6: EqualsConditionEditor, GreaterThanConditionEditor, LowerThanConditionEditor

Variable/value text-field editors. `GreaterThan` and `LowerThan` additionally validate that the value is a parseable number and store it as `Double`.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/EqualsConditionEditor.java`
- Create: `src/main/java/com/pdg/adventure/view/command/condition/GreaterThanConditionEditor.java`
- Create: `src/main/java/com/pdg/adventure/view/command/condition/LowerThanConditionEditor.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/EqualsConditionEditorTest.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/GreaterThanConditionEditorTest.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/LowerThanConditionEditorTest.java`

- [ ] **Step 1: Write `EqualsConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.EqualsConditionData;

class EqualsConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        EqualsConditionEditor editor = new EqualsConditionEditor(new EqualsConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withBothFieldsPreSet_returnsTrue() {
        EqualsConditionData data = new EqualsConditionData("score", "100");
        EqualsConditionEditor editor = new EqualsConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        EqualsConditionData data = new EqualsConditionData();
        EqualsConditionEditor editor = new EqualsConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        EqualsConditionEditor editor = new EqualsConditionEditor(new EqualsConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withPreSetValues_returnsFormattedString() {
        EqualsConditionData data = new EqualsConditionData("lives", "3");
        EqualsConditionEditor editor = new EqualsConditionEditor(data);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("lives = 3");
    }
}
```

- [ ] **Step 2: Implement `EqualsConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.condition.EqualsConditionData;

public class EqualsConditionEditor extends ConditionEditorComponent {
    private final EqualsConditionData equalsData;
    private TextField variableNameField;
    private TextField valueField;

    public EqualsConditionEditor(EqualsConditionData conditionData) {
        super(conditionData);
        this.equalsData = conditionData;
    }

    @Override
    protected void buildUI() {
        variableNameField = new TextField("Variable Name");
        variableNameField.setWidthFull();
        variableNameField.setRequired(true);

        valueField = new TextField("Value");
        valueField.setWidthFull();
        valueField.setRequired(true);

        if (equalsData.getVariableName() != null) variableNameField.setValue(equalsData.getVariableName());
        if (equalsData.getValue() != null) valueField.setValue(equalsData.getValue());

        variableNameField.addValueChangeListener(e -> equalsData.setVariableName(e.getValue()));
        valueField.addValueChangeListener(e -> equalsData.setValue(e.getValue()));

        add(variableNameField, valueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.getValue() != null && !variableNameField.getValue().trim().isEmpty();
        boolean valValid = valueField.getValue() != null && !valueField.getValue().trim().isEmpty();
        variableNameField.setInvalid(!nameValid);
        valueField.setInvalid(!valValid);
        if (!nameValid) variableNameField.setErrorMessage("Please enter a variable name");
        if (!valValid) valueField.setErrorMessage("Please enter a value");
        return nameValid && valValid;
    }

    @Override
    public String getConditionSummary() {
        String var = (variableNameField != null && !variableNameField.getValue().isEmpty())
                ? variableNameField.getValue() : (equalsData.getVariableName() != null ? equalsData.getVariableName() : "");
        String val = (valueField != null && !valueField.getValue().isEmpty())
                ? valueField.getValue() : (equalsData.getValue() != null ? equalsData.getValue() : "");
        if (var.isEmpty()) return "(none)";
        return var + " = " + val;
    }
}
```

- [ ] **Step 3: Write `GreaterThanConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.GreaterThanConditionData;

class GreaterThanConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(new GreaterThanConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withInvalidNumberValue_returnsFalse() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setVariableName("score");
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(data);
        editor.initialize();
        // variableName set but value not set
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withPreSetValues_returnsTrue() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setVariableName("score");
        data.setValue(50);
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        GreaterThanConditionEditor editor = new GreaterThanConditionEditor(new GreaterThanConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
```

- [ ] **Step 4: Implement `GreaterThanConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.condition.GreaterThanConditionData;

public class GreaterThanConditionEditor extends ConditionEditorComponent {
    private final GreaterThanConditionData condData;
    private TextField variableNameField;
    private TextField valueField;

    public GreaterThanConditionEditor(GreaterThanConditionData conditionData) {
        super(conditionData);
        this.condData = conditionData;
    }

    @Override
    protected void buildUI() {
        variableNameField = new TextField("Variable Name");
        variableNameField.setWidthFull();
        variableNameField.setRequired(true);

        valueField = new TextField("Value (number)");
        valueField.setWidthFull();
        valueField.setRequired(true);

        if (condData.getVariableName() != null) variableNameField.setValue(condData.getVariableName());
        if (condData.getValue() != null) valueField.setValue(condData.getValue().toString());

        variableNameField.addValueChangeListener(e -> condData.setVariableName(e.getValue()));
        valueField.addValueChangeListener(e -> {
            try { condData.setValue(Double.parseDouble(e.getValue())); }
            catch (NumberFormatException ex) { condData.setValue(null); }
        });

        add(variableNameField, valueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.getValue() != null && !variableNameField.getValue().trim().isEmpty();
        boolean valValid = false;
        if (valueField.getValue() != null && !valueField.getValue().trim().isEmpty()) {
            try { Double.parseDouble(valueField.getValue()); valValid = true; }
            catch (NumberFormatException ignored) {}
        }
        variableNameField.setInvalid(!nameValid);
        valueField.setInvalid(!valValid);
        if (!nameValid) variableNameField.setErrorMessage("Please enter a variable name");
        if (!valValid) valueField.setErrorMessage("Please enter a valid number");
        return nameValid && valValid;
    }

    @Override
    public String getConditionSummary() {
        String var = (variableNameField != null && !variableNameField.getValue().isEmpty())
                ? variableNameField.getValue() : "";
        String val = (valueField != null && !valueField.getValue().isEmpty())
                ? valueField.getValue() : "";
        if (var.isEmpty()) return "(none)";
        return var + " > " + val;
    }
}
```

- [ ] **Step 5: Write `LowerThanConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.LowerThanConditionData;

class LowerThanConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        LowerThanConditionEditor editor = new LowerThanConditionEditor(new LowerThanConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withPreSetValues_returnsTrue() {
        LowerThanConditionData data = new LowerThanConditionData();
        data.setVariableName("lives");
        data.setValue(3);
        LowerThanConditionEditor editor = new LowerThanConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        LowerThanConditionData data = new LowerThanConditionData();
        LowerThanConditionEditor editor = new LowerThanConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        LowerThanConditionEditor editor = new LowerThanConditionEditor(new LowerThanConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
```

- [ ] **Step 6: Implement `LowerThanConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.condition.LowerThanConditionData;

public class LowerThanConditionEditor extends ConditionEditorComponent {
    private final LowerThanConditionData condData;
    private TextField variableNameField;
    private TextField valueField;

    public LowerThanConditionEditor(LowerThanConditionData conditionData) {
        super(conditionData);
        this.condData = conditionData;
    }

    @Override
    protected void buildUI() {
        variableNameField = new TextField("Variable Name");
        variableNameField.setWidthFull();
        variableNameField.setRequired(true);

        valueField = new TextField("Value (number)");
        valueField.setWidthFull();
        valueField.setRequired(true);

        if (condData.getVariableName() != null) variableNameField.setValue(condData.getVariableName());
        if (condData.getValue() != null) valueField.setValue(condData.getValue().toString());

        variableNameField.addValueChangeListener(e -> condData.setVariableName(e.getValue()));
        valueField.addValueChangeListener(e -> {
            try { condData.setValue(Double.parseDouble(e.getValue())); }
            catch (NumberFormatException ex) { condData.setValue(null); }
        });

        add(variableNameField, valueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.getValue() != null && !variableNameField.getValue().trim().isEmpty();
        boolean valValid = false;
        if (valueField.getValue() != null && !valueField.getValue().trim().isEmpty()) {
            try { Double.parseDouble(valueField.getValue()); valValid = true; }
            catch (NumberFormatException ignored) {}
        }
        variableNameField.setInvalid(!nameValid);
        valueField.setInvalid(!valValid);
        if (!nameValid) variableNameField.setErrorMessage("Please enter a variable name");
        if (!valValid) valueField.setErrorMessage("Please enter a valid number");
        return nameValid && valValid;
    }

    @Override
    public String getConditionSummary() {
        String var = (variableNameField != null && !variableNameField.getValue().isEmpty())
                ? variableNameField.getValue() : "";
        String val = (valueField != null && !valueField.getValue().isEmpty())
                ? valueField.getValue() : "";
        if (var.isEmpty()) return "(none)";
        return var + " < " + val;
    }
}
```

- [ ] **Step 7: Run all three tests**

```bash
cd server && mvn test -Dtest="EqualsConditionEditorTest,GreaterThanConditionEditorTest,LowerThanConditionEditorTest" -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/ \
        src/test/java/com/pdg/adventure/view/command/condition/
git commit -m "feat: add EqualsConditionEditor, GreaterThanConditionEditor, LowerThanConditionEditor"
```

---

### Task 7: SameConditionEditor

Two variable name TextFields.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/SameConditionEditor.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/SameConditionEditorTest.java`

- [ ] **Step 1: Write `SameConditionEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.SameConditionData;

class SameConditionEditorTest {

    @Test
    void validate_withEmptyFields_returnsFalse() {
        SameConditionEditor editor = new SameConditionEditor(new SameConditionData());
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withBothVariablesPreSet_returnsTrue() {
        SameConditionData data = new SameConditionData();
        data.setVariableNameOne("score");
        data.setVariableNameTwo("highScore");
        SameConditionEditor editor = new SameConditionEditor(data);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        SameConditionData data = new SameConditionData();
        SameConditionEditor editor = new SameConditionEditor(data);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        SameConditionEditor editor = new SameConditionEditor(new SameConditionData());
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withBothSet_returnsFormattedString() {
        SameConditionData data = new SameConditionData();
        data.setVariableNameOne("a");
        data.setVariableNameTwo("b");
        SameConditionEditor editor = new SameConditionEditor(data);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("a = b");
    }
}
```

- [ ] **Step 2: Implement `SameConditionEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.TextField;

import com.pdg.adventure.model.condition.SameConditionData;

public class SameConditionEditor extends ConditionEditorComponent {
    private final SameConditionData sameData;
    private TextField varOneField;
    private TextField varTwoField;

    public SameConditionEditor(SameConditionData conditionData) {
        super(conditionData);
        this.sameData = conditionData;
    }

    @Override
    protected void buildUI() {
        varOneField = new TextField("Variable 1");
        varOneField.setWidthFull();
        varOneField.setRequired(true);

        varTwoField = new TextField("Variable 2");
        varTwoField.setWidthFull();
        varTwoField.setRequired(true);

        if (sameData.getVariableNameOne() != null) varOneField.setValue(sameData.getVariableNameOne());
        if (sameData.getVariableNameTwo() != null) varTwoField.setValue(sameData.getVariableNameTwo());

        varOneField.addValueChangeListener(e -> sameData.setVariableNameOne(e.getValue()));
        varTwoField.addValueChangeListener(e -> sameData.setVariableNameTwo(e.getValue()));

        add(varOneField, varTwoField);
    }

    @Override
    public boolean validate() {
        boolean oneValid = varOneField.getValue() != null && !varOneField.getValue().trim().isEmpty();
        boolean twoValid = varTwoField.getValue() != null && !varTwoField.getValue().trim().isEmpty();
        varOneField.setInvalid(!oneValid);
        varTwoField.setInvalid(!twoValid);
        if (!oneValid) varOneField.setErrorMessage("Please enter variable 1 name");
        if (!twoValid) varTwoField.setErrorMessage("Please enter variable 2 name");
        return oneValid && twoValid;
    }

    @Override
    public String getConditionSummary() {
        String one = (varOneField != null && !varOneField.getValue().isEmpty()) ? varOneField.getValue() : "";
        String two = (varTwoField != null && !varTwoField.getValue().isEmpty()) ? varTwoField.getValue() : "";
        if (one.isEmpty()) return "(none)";
        return one + " = " + two;
    }
}
```

- [ ] **Step 3: Run test**

```bash
cd server && mvn test -Dtest="SameConditionEditorTest" -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/SameConditionEditor.java \
        src/test/java/com/pdg/adventure/view/command/condition/SameConditionEditorTest.java
git commit -m "feat: add SameConditionEditor"
```

---

### Task 8: ConditionEditorFactory

Switch expression on `PreConditionData` subtype; delegates to the 9 editors created in Tasks 3–7.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/ConditionEditorFactory.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/ConditionEditorFactoryTest.java`

- [ ] **Step 1: Write `ConditionEditorFactoryTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.condition.*;

class ConditionEditorFactoryTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        adventureData.setLocationData(new HashMap<>());
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void createEditor_withCarriedConditionData_returnsCarriedEditor() {
        ConditionEditorComponent editor = ConditionEditorFactory.createEditor(new CarriedConditionData(), adventureData);
        assertThat(editor).isInstanceOf(CarriedConditionEditor.class);
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void createEditor_withHereConditionData_returnsHereEditor() {
        assertThat(ConditionEditorFactory.createEditor(new HereConditionData(), adventureData))
                .isInstanceOf(HereConditionEditor.class);
    }

    @Test
    void createEditor_withWornConditionData_returnsWornEditor() {
        assertThat(ConditionEditorFactory.createEditor(new WornConditionData(), adventureData))
                .isInstanceOf(WornConditionEditor.class);
    }

    @Test
    void createEditor_withPlayerAtConditionData_returnsPlayerAtEditor() {
        assertThat(ConditionEditorFactory.createEditor(new PlayerAtConditionData(), adventureData))
                .isInstanceOf(PlayerAtConditionEditor.class);
    }

    @Test
    void createEditor_withItemAtConditionData_returnsItemAtEditor() {
        assertThat(ConditionEditorFactory.createEditor(new ItemAtConditionData(), adventureData))
                .isInstanceOf(ItemAtConditionEditor.class);
    }

    @Test
    void createEditor_withEqualsConditionData_returnsEqualsEditor() {
        assertThat(ConditionEditorFactory.createEditor(new EqualsConditionData(), adventureData))
                .isInstanceOf(EqualsConditionEditor.class);
    }

    @Test
    void createEditor_withGreaterThanConditionData_returnsGreaterThanEditor() {
        assertThat(ConditionEditorFactory.createEditor(new GreaterThanConditionData(), adventureData))
                .isInstanceOf(GreaterThanConditionEditor.class);
    }

    @Test
    void createEditor_withLowerThanConditionData_returnsLowerThanEditor() {
        assertThat(ConditionEditorFactory.createEditor(new LowerThanConditionData(), adventureData))
                .isInstanceOf(LowerThanConditionEditor.class);
    }

    @Test
    void createEditor_withSameConditionData_returnsSameEditor() {
        assertThat(ConditionEditorFactory.createEditor(new SameConditionData(), adventureData))
                .isInstanceOf(SameConditionEditor.class);
    }

    @Test
    void createEditor_withUnknownType_throwsUnsupportedOperationException() {
        PreConditionData unknown = new PreConditionData() {};
        assertThatThrownBy(() -> ConditionEditorFactory.createEditor(unknown, adventureData))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("No editor available for condition type");
    }

    @Test
    void createEditor_returnsInitializedEditor() {
        ConditionEditorComponent editor = ConditionEditorFactory.createEditor(new CarriedConditionData(), adventureData);
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
```

- [ ] **Step 2: Run test to confirm compilation failure**

```bash
cd server && mvn test -Dtest="ConditionEditorFactoryTest" -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement `ConditionEditorFactory`**

```java
package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.*;

public class ConditionEditorFactory {

    public static ConditionEditorComponent createEditor(PreConditionData data, AdventureData adventureData) {
        ConditionEditorComponent editor = switch (data) {
            case CarriedConditionData d -> new CarriedConditionEditor(d, adventureData);
            case HereConditionData d -> new HereConditionEditor(d, adventureData);
            case WornConditionData d -> new WornConditionEditor(d, adventureData);
            case PlayerAtConditionData d -> new PlayerAtConditionEditor(d, adventureData);
            case ItemAtConditionData d -> new ItemAtConditionEditor(d, adventureData);
            case EqualsConditionData d -> new EqualsConditionEditor(d);
            case GreaterThanConditionData d -> new GreaterThanConditionEditor(d);
            case LowerThanConditionData d -> new LowerThanConditionEditor(d);
            case SameConditionData d -> new SameConditionEditor(d);
            default -> throw new UnsupportedOperationException(
                    "No editor available for condition type: " + data.getClass().getSimpleName());
        };
        editor.initialize();
        return editor;
    }
}
```

- [ ] **Step 4: Run tests**

```bash
cd server && mvn test -Dtest="ConditionEditorFactoryTest" -q
```

Expected: `BUILD SUCCESS`, all 11 tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/ConditionEditorFactory.java \
        src/test/java/com/pdg/adventure/view/command/condition/ConditionEditorFactoryTest.java
git commit -m "feat: add ConditionEditorFactory"
```

---

### Task 9: ConditionSelector

ComboBox listing the 9 leaf types + Add button. On click, creates a fresh data object and notifies a listener.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/ConditionSelector.java`

No dedicated test: the selector is a thin wrapper over `ComboBox` + `Button`, tested indirectly via `ConditionListEditorTest`.

- [ ] **Step 1: Implement `ConditionSelector`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import java.util.List;
import java.util.function.Supplier;

import com.pdg.adventure.model.condition.*;

public class ConditionSelector extends HorizontalLayout {

    @Setter
    private ConditionSelectedListener conditionSelectedListener;

    private final ComboBox<ConditionTypeDescriptor> typeSelector;

    public ConditionSelector() {
        typeSelector = new ComboBox<>("Add Condition");
        typeSelector.setItems(availableTypes());
        typeSelector.setItemLabelGenerator(ConditionTypeDescriptor::displayName);
        typeSelector.setPlaceholder("Choose condition type…");
        typeSelector.setWidthFull();

        Button addButton = new Button("Add");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setEnabled(false);

        typeSelector.addValueChangeListener(e -> addButton.setEnabled(e.getValue() != null));

        addButton.addClickListener(_ -> {
            ConditionTypeDescriptor selected = typeSelector.getValue();
            if (selected != null && conditionSelectedListener != null) {
                conditionSelectedListener.onConditionSelected(selected.createData());
                typeSelector.clear();
                addButton.setEnabled(false);
            }
        });

        setWidthFull();
        setAlignItems(Alignment.END);
        add(typeSelector, addButton);
        expand(typeSelector);
    }

    private List<ConditionTypeDescriptor> availableTypes() {
        return List.of(
            new ConditionTypeDescriptor("Carried (item in inventory)", CarriedConditionData::new),
            new ConditionTypeDescriptor("Here (item at current location)", HereConditionData::new),
            new ConditionTypeDescriptor("Worn (item being worn)", WornConditionData::new),
            new ConditionTypeDescriptor("Player At (location)", PlayerAtConditionData::new),
            new ConditionTypeDescriptor("Item At (item + location)", ItemAtConditionData::new),
            new ConditionTypeDescriptor("Equals (variable = value)", EqualsConditionData::new),
            new ConditionTypeDescriptor("Greater Than (variable > value)", GreaterThanConditionData::new),
            new ConditionTypeDescriptor("Lower Than (variable < value)", LowerThanConditionData::new),
            new ConditionTypeDescriptor("Same (variable = variable)", SameConditionData::new)
        );
    }

    @FunctionalInterface
    public interface ConditionSelectedListener {
        void onConditionSelected(PreConditionData data);
    }

    private record ConditionTypeDescriptor(String displayName, Supplier<PreConditionData> factory) {
        PreConditionData createData() { return factory.get(); }
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd server && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/ConditionSelector.java
git commit -m "feat: add ConditionSelector"
```

---

### Task 10: ConditionRow

Extends `Details`. Summary text = condition type name. Content = negate checkbox + remove button + editor form. `toConditionData()` wraps in `NotConditionData` when negate is checked.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/ConditionRow.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/ConditionRowTest.java`

- [ ] **Step 1: Write `ConditionRowTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

class ConditionRowTest {

    private ConditionEditorComponent editor;

    @BeforeEach
    void setUp() {
        com.pdg.adventure.model.condition.EqualsConditionData eqData = new com.pdg.adventure.model.condition.EqualsConditionData();
        editor = new EqualsConditionEditor(eqData);
        editor.initialize();
    }

    @Test
    void toConditionData_withNegateUnchecked_returnsLeafData() {
        ConditionRow row = new ConditionRow(editor, false);
        PreConditionData result = row.toConditionData();
        assertThat(result).isSameAs(editor.getConditionData());
        assertThat(result).isNotInstanceOf(NotConditionData.class);
    }

    @Test
    void toConditionData_withNegateChecked_returnsNotConditionDataWrappingLeaf() {
        ConditionRow row = new ConditionRow(editor, true);
        PreConditionData result = row.toConditionData();
        assertThat(result).isInstanceOf(NotConditionData.class);
        assertThat(((NotConditionData) result).getPreCondition()).isSameAs(editor.getConditionData());
    }

    @Test
    void constructor_buildsUI() {
        ConditionRow row = new ConditionRow(editor, false);
        assertThat(row.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void setOnRemove_acceptsCallback() {
        ConditionRow row = new ConditionRow(editor, false);
        row.setOnRemove(() -> {});
        assertThat(row).isNotNull();
    }
}
```

- [ ] **Step 2: Run test to confirm compilation failure**

```bash
cd server && mvn test -Dtest="ConditionRowTest" -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement `ConditionRow`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

public class ConditionRow extends Details {
    private final ConditionEditorComponent editor;
    private final Checkbox negateCheckbox;
    @Setter
    private Runnable onRemove;

    public ConditionRow(ConditionEditorComponent editor, boolean negate) {
        this.editor = editor;

        String typeName = editor.getConditionData().getPreconditionName()
                               .replace("ConditionData", "");
        setSummaryText(typeName);

        negateCheckbox = new Checkbox("Negate", negate);

        Button removeButton = new Button("Remove");
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(_ -> { if (onRemove != null) onRemove.run(); });

        HorizontalLayout controls = new HorizontalLayout(negateCheckbox, removeButton);
        controls.setAlignItems(Alignment.CENTER);

        add(controls, editor);
        setWidthFull();
    }

    public PreConditionData toConditionData() {
        if (negateCheckbox.getValue()) {
            NotConditionData not = new NotConditionData();
            not.setPreCondition(editor.getConditionData());
            return not;
        }
        return editor.getConditionData();
    }

    public boolean validate() {
        return editor.validate();
    }
}
```

- [ ] **Step 4: Run tests**

```bash
cd server && mvn test -Dtest="ConditionRowTest" -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/ConditionRow.java \
        src/test/java/com/pdg/adventure/view/command/condition/ConditionRowTest.java
git commit -m "feat: add ConditionRow accordion component"
```

---

### Task 11: ConditionListEditor

Top-level container. Holds `ConditionRow` instances + `ConditionSelector`. Exposes `setConditions` and `getConditions` for load/save.

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/condition/ConditionListEditor.java`
- Create: `src/test/java/com/pdg/adventure/view/command/condition/ConditionListEditorTest.java`

- [ ] **Step 1: Write `ConditionListEditorTest`**

```java
package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

class ConditionListEditorTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        adventureData.setLocationData(new HashMap<>());
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void constructor_buildsUI() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void setConditions_withNullList_doesNotThrow() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(null);
        assertThat(editor.getConditions()).isEmpty();
    }

    @Test
    void setConditions_withEmptyList_producesNoRows() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(new ArrayList<>());
        assertThat(editor.getConditions()).isEmpty();
    }

    @Test
    void setConditions_withLeafCondition_producesOneRow() {
        CarriedConditionData carried = new CarriedConditionData();
        carried.setItemId("item-1");

        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(carried));

        List<PreConditionData> result = editor.getConditions();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(CarriedConditionData.class);
    }

    @Test
    void setConditions_withNotConditionData_producesNegatedRow() {
        CarriedConditionData leaf = new CarriedConditionData();
        leaf.setItemId("item-1");
        NotConditionData not = new NotConditionData();
        not.setPreCondition(leaf);

        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(not));

        List<PreConditionData> result = editor.getConditions();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(NotConditionData.class);
        assertThat(((NotConditionData) result.getFirst()).getPreCondition()).isInstanceOf(CarriedConditionData.class);
    }

    @Test
    void getConditions_afterSetConditions_roundTripsLeafData() {
        CarriedConditionData carried = new CarriedConditionData();
        carried.setId("round-trip-id");
        carried.setItemId("sword");

        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(carried));

        List<PreConditionData> result = editor.getConditions();
        assertThat(result).hasSize(1);
        CarriedConditionData retrieved = (CarriedConditionData) result.getFirst();
        assertThat(retrieved.getId()).isEqualTo("round-trip-id");
        assertThat(retrieved.getItemId()).isEqualTo("sword");
    }
}
```

- [ ] **Step 2: Run test to confirm compilation failure**

```bash
cd server && mvn test -Dtest="ConditionListEditorTest" -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement `ConditionListEditor`**

```java
package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.stream.Collectors;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

public class ConditionListEditor extends VerticalLayout {
    private final AdventureData adventureData;
    private final VerticalLayout rowsLayout;

    public ConditionListEditor(AdventureData adventureData) {
        this.adventureData = adventureData;

        rowsLayout = new VerticalLayout();
        rowsLayout.setPadding(false);
        rowsLayout.setSpacing(true);

        ConditionSelector selector = new ConditionSelector();
        selector.setConditionSelectedListener(data -> addRow(data, false));

        setPadding(false);
        add(rowsLayout, selector);
    }

    public void setConditions(List<PreConditionData> conditions) {
        rowsLayout.removeAll();
        if (conditions == null) return;
        for (PreConditionData data : conditions) {
            boolean negate = data instanceof NotConditionData;
            PreConditionData leaf = negate ? ((NotConditionData) data).getPreCondition() : data;
            addRow(leaf, negate);
        }
    }

    public List<PreConditionData> getConditions() {
        return rowsLayout.getChildren()
                .filter(c -> c instanceof ConditionRow)
                .map(c -> (ConditionRow) c)
                .map(ConditionRow::toConditionData)
                .collect(Collectors.toList());
    }

    private void addRow(PreConditionData data, boolean negate) {
        ConditionEditorComponent editor = ConditionEditorFactory.createEditor(data, adventureData);
        ConditionRow row = new ConditionRow(editor, negate);
        row.setOnRemove(() -> rowsLayout.remove(row));
        rowsLayout.add(row);
    }
}
```

- [ ] **Step 4: Run tests**

```bash
cd server && mvn test -Dtest="ConditionListEditorTest" -q
```

Expected: `BUILD SUCCESS`, all tests pass.

- [ ] **Step 5: Run full suite**

```bash
cd server && mvn test -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/condition/ConditionListEditor.java \
        src/test/java/com/pdg/adventure/view/command/condition/ConditionListEditorTest.java
git commit -m "feat: add ConditionListEditor"
```

---

### Task 12: PreconditionActionEditor refactor

Replace the `Grid<PreConditionData>` stub and empty `addPrecondition()` method with `ConditionListEditor`. Split the single `Details` panel into two separate sections (preconditions and actions). Add `AdventureData` parameter to the constructor and a `setCommand(CommandData)` method.

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/PreconditionActionEditor.java`

- [ ] **Step 1: Read the current file**

Current `PreconditionActionEditor.java` (40 lines):
```java
public PreconditionActionEditor() {
    preconditionGrid = GridFactory.createGrid(PreConditionData.class, List.of(
            new GridFactory.ColumnConfig<>(p -> p.getId(), "ID", false)
    ));
    actionGrid = GridFactory.createGrid(ActionData.class, List.of(
            new GridFactory.ColumnConfig<>(a -> a.getId(), "ID", false)
    ));
    Button addPrecondition = new Button("Add Precondition", _ -> addPrecondition());
    Button addAction = new Button("Add Action", _ -> addAction());
    Details details = new Details("Preconditions & Actions", new VerticalLayout(
            addPrecondition, preconditionGrid, addAction, actionGrid));
    add(details);
}
```

- [ ] **Step 2: Replace with the new implementation**

```java
package com.pdg.adventure.view.command;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.view.command.condition.ConditionListEditor;
import com.pdg.adventure.view.component.GridFactory;

public class PreconditionActionEditor extends VerticalLayout {
    private final ConditionListEditor conditionListEditor;
    private final Grid<ActionData> actionGrid;

    public PreconditionActionEditor(AdventureData adventureData) {
        conditionListEditor = new ConditionListEditor(adventureData);

        actionGrid = GridFactory.createGrid(ActionData.class, List.of(
                new GridFactory.ColumnConfig<>(a -> a.getId(), "ID", false)
        ));
        Button addAction = new Button("Add Action", _ -> addAction());

        Details preconditionsSection = new Details("Preconditions", conditionListEditor);
        Details actionsSection = new Details("Actions", new VerticalLayout(addAction, actionGrid));

        add(preconditionsSection, actionsSection);
    }

    public void setCommand(CommandData commandData) {
        conditionListEditor.setConditions(commandData.getPreConditions());
    }

    private void addAction() {
        // Implement based on ActionData structure
    }
}
```

- [ ] **Step 3: Verify compilation**

```bash
cd server && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Run full test suite**

```bash
cd server && mvn test -q
```

Expected: `BUILD SUCCESS`, all tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/PreconditionActionEditor.java
git commit -m "feat: replace PreconditionActionEditor Grid stub with ConditionListEditor"
```
