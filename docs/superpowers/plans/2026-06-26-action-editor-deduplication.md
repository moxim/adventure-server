# Action Editor Deduplication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract shared item-collection logic into `ViewSupporter` and unify five single-item action editors under a new abstract base class, eliminating copy-pasted `collectAllItems`, `findItemById`, `buildUI`, `validate`, and `getActionSummary` across 7 editors.

**Architecture:** Two new static methods on `ViewSupporter` replace 7 identical private copies of `collectAllItems`. A new `AbstractSingleItemActionEditor<T>` abstract class implements the full single-item-selector pattern; five concrete editors are reduced to a constructor + two abstract method overrides. `CreateActionEditor` and `MoveItemActionEditor` keep their own `buildUI` but delegate collection to `ViewSupporter`.

**Tech Stack:** Java 21, Vaadin Flow (ComboBox, H4, Span), Lombok (@Data on model classes), JUnit 5, AssertJ, Maven

## Global Constraints

- All tests run via `mvn test` from `/Users/mafw/workroom/projects/adventurebuilder/server/`
- No changes to any model (`*ActionData`) class — model classes are unchanged
- The abstract base makes `buildUI()`, `validate()`, and `getActionSummary()` `final` — subclasses may not override them
- `DestroyActionEditor` currently renders no title or description; the refactor adds both (consistent with all other editors); existing test `initialize_shouldBuildUI` asserts `getChildren().count() > 0` which still passes
- `DropActionEditor` currently renders title and description inline; the refactor separates them into fields — behavior is identical, test still passes
- Package for all editor classes: `com.pdg.adventure.view.command.action`
- Package for `ViewSupporter`: `com.pdg.adventure.view.support`

---

### Task 1: Add `collectAllItems` and `collectAllContainers` to `ViewSupporter`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/support/ViewSupporter.java` (add 2 methods after `getItemLocationPairs`)
- Create: `src/test/java/com/pdg/adventure/view/support/ViewSupporterTest.java`

**Interfaces:**
- Produces:
  - `ViewSupporter.collectAllItems(AdventureData): List<ItemData>`
  - `ViewSupporter.collectAllContainers(AdventureData): List<ItemContainerData>`

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/com/pdg/adventure/view/support/ViewSupporterTest.java`:

```java
package com.pdg.adventure.view.support;

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

class ViewSupporterTest {

    private AdventureData adventureData;
    private ItemData locationItem;
    private ItemData pocketItem;
    private ItemContainerData container;
    private ItemContainerData playerPocket;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        locationItem = new ItemData();
        locationItem.setId("item-loc");

        pocketItem = new ItemData();
        pocketItem.setId("item-pocket");

        LocationData location = new LocationData();
        location.setId("loc-1");

        container = new ItemContainerData("loc-1");
        container.setId("container-1");
        List<ItemData> locItems = new ArrayList<>();
        locItems.add(locationItem);
        container.setItems(locItems);
        location.setItemContainerData(container);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(location.getId(), location);
        adventureData.setLocationData(locations);

        playerPocket = new ItemContainerData("player-pocket");
        playerPocket.setId("pocket-1");
        List<ItemData> pocketItems = new ArrayList<>();
        pocketItems.add(pocketItem);
        playerPocket.setItems(pocketItems);
        adventureData.setPlayerPocket(playerPocket);
    }

    @Test
    void collectAllItems_shouldIncludeItemsFromLocations() {
        List<ItemData> result = ViewSupporter.collectAllItems(adventureData);
        assertThat(result).contains(locationItem);
    }

    @Test
    void collectAllItems_shouldIncludeItemsFromPlayerPocket() {
        List<ItemData> result = ViewSupporter.collectAllItems(adventureData);
        assertThat(result).contains(pocketItem);
    }

    @Test
    void collectAllItems_withNullItemList_shouldNotThrow() {
        container.setItems(null);
        List<ItemData> result = ViewSupporter.collectAllItems(adventureData);
        assertThat(result).doesNotContain(locationItem);
    }

    @Test
    void collectAllContainers_shouldReturnPocketFirst() {
        List<ItemContainerData> result = ViewSupporter.collectAllContainers(adventureData);
        assertThat(result.get(0)).isSameAs(playerPocket);
    }

    @Test
    void collectAllContainers_shouldIncludeLocationContainers() {
        List<ItemContainerData> result = ViewSupporter.collectAllContainers(adventureData);
        assertThat(result).contains(container);
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```
mvn test -Dtest=ViewSupporterTest
```
Expected: FAILURE — `collectAllItems` and `collectAllContainers` do not exist yet.

- [ ] **Step 3: Add the two methods to `ViewSupporter`**

In `src/main/java/com/pdg/adventure/view/support/ViewSupporter.java`, insert after the closing `}` of `getItemLocationPairs` (after line 75):

```java
    public static List<ItemData> collectAllItems(AdventureData adventureData) {
        List<ItemData> allItems = new ArrayList<>();
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                List<ItemData> items = location.getItemContainerData().getItems();
                if (items != null) {
                    allItems.addAll(items);
                }
            }
        }
        if (adventureData.getPlayerPocket() != null) {
            List<ItemData> pocketItems = adventureData.getPlayerPocket().getItems();
            if (pocketItems != null) {
                allItems.addAll(pocketItems);
            }
        }
        return allItems;
    }

    public static List<ItemContainerData> collectAllContainers(AdventureData adventureData) {
        List<ItemContainerData> allContainers = new ArrayList<>();
        if (adventureData.getPlayerPocket() != null) {
            allContainers.add(adventureData.getPlayerPocket());
        }
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                allContainers.add(location.getItemContainerData());
            }
        }
        return allContainers;
    }
```

`ViewSupporter.java` already has `import com.pdg.adventure.model.*;` so `AdventureData`, `ItemData`, `ItemContainerData`, and `LocationData` are covered. No new imports needed.

- [ ] **Step 4: Run tests to confirm they pass**

```
mvn test -Dtest=ViewSupporterTest
```
Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```
git add src/main/java/com/pdg/adventure/view/support/ViewSupporter.java \
        src/test/java/com/pdg/adventure/view/support/ViewSupporterTest.java
git commit -m "refactor: extract collectAllItems/collectAllContainers into ViewSupporter"
```

---

### Task 2: Create `AbstractSingleItemActionEditor<T>` and refactor `WearActionEditor`

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/action/AbstractSingleItemActionEditor.java`
- Modify: `src/main/java/com/pdg/adventure/view/command/action/WearActionEditor.java` (replace with 15-line version)
- Test: `src/test/java/com/pdg/adventure/view/command/action/WearActionEditorTest.java` (existing — no changes)

**Interfaces:**
- Consumes: `ViewSupporter.collectAllItems(AdventureData)` from Task 1
- Produces: `AbstractSingleItemActionEditor<T>` — abstract base with `final buildUI()`, `final validate()`, `final getActionSummary()`; abstract `currentThingId()` / `applyThingId(String)`

- [ ] **Step 1: Confirm existing WearActionEditor tests are green**

```
mvn test -Dtest=WearActionEditorTest
```
Expected: `Tests run: 6, Failures: 0, Errors: 0`

- [ ] **Step 2: Create `AbstractSingleItemActionEditor`**

Create `src/main/java/com/pdg/adventure/view/command/action/AbstractSingleItemActionEditor.java`:

```java
package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.view.support.ViewSupporter;

public abstract class AbstractSingleItemActionEditor<T extends ActionData>
        extends ActionEditorComponent {

    protected final T typedAction;
    protected final AdventureData adventureData;

    private final String title;
    private final String descriptionText;
    private final String itemLabel;
    private final String itemPlaceholder;
    private final String itemErrorMessage;

    private ComboBox<ItemData> itemSelector;

    protected AbstractSingleItemActionEditor(
            T actionData, AdventureData adventureData,
            String title, String descriptionText,
            String itemLabel, String itemPlaceholder, String itemErrorMessage) {
        super(actionData);
        this.typedAction = actionData;
        this.adventureData = adventureData;
        this.title = title;
        this.descriptionText = descriptionText;
        this.itemLabel = itemLabel;
        this.itemPlaceholder = itemPlaceholder;
        this.itemErrorMessage = itemErrorMessage;
    }

    protected abstract String currentThingId();

    protected abstract void applyThingId(String id);

    @Override
    protected final void buildUI() {
        H4 heading = new H4(title);
        Span description = new Span(descriptionText);
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = ViewSupporter.collectAllItems(adventureData);

        itemSelector = new ComboBox<>(itemLabel);
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder(itemPlaceholder);
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (currentThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(currentThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(
                e -> applyThingId(e.getValue() != null ? e.getValue().getId() : null));

        add(heading, description, itemSelector);
    }

    @Override
    public final boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        if (!valid) {
            itemSelector.setErrorMessage(itemErrorMessage);
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }
        return valid;
    }

    @Override
    public final String getActionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }
}
```

- [ ] **Step 3: Replace `WearActionEditor` with the refactored version**

Replace the full content of `src/main/java/com/pdg/adventure/view/command/action/WearActionEditor.java`:

```java
package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.WearActionData;

public class WearActionEditor extends AbstractSingleItemActionEditor<WearActionData> {

    public WearActionEditor(WearActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Wear Action",
              "Select the wearable item the player will wear. Shows all items; ensure you select a wearable one.",
              "Item to Wear", "Select item to wear", "Please select an item to wear");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
```

- [ ] **Step 4: Run WearActionEditor tests**

```
mvn test -Dtest=WearActionEditorTest
```
Expected: `Tests run: 6, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```
git add src/main/java/com/pdg/adventure/view/command/action/AbstractSingleItemActionEditor.java \
        src/main/java/com/pdg/adventure/view/command/action/WearActionEditor.java
git commit -m "refactor: extract AbstractSingleItemActionEditor; migrate WearActionEditor"
```

---

### Task 3: Refactor `TakeActionEditor`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/action/TakeActionEditor.java`
- Test: `src/test/java/com/pdg/adventure/view/command/action/TakeActionEditorTest.java` (existing — no changes)

**Interfaces:**
- Consumes: `AbstractSingleItemActionEditor<T>` from Task 2

- [ ] **Step 1: Confirm existing tests are green**

```
mvn test -Dtest=TakeActionEditorTest
```
Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 2: Replace `TakeActionEditor`**

Replace the full content of `src/main/java/com/pdg/adventure/view/command/action/TakeActionEditor.java`:

```java
package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.TakeActionData;

public class TakeActionEditor extends AbstractSingleItemActionEditor<TakeActionData> {

    public TakeActionEditor(TakeActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Take Action",
              "Select the item the player will pick up.",
              "Item to Take", "Select item to take", "Please select an item to take");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
```

- [ ] **Step 3: Run tests**

```
mvn test -Dtest=TakeActionEditorTest
```
Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 4: Commit**

```
git add src/main/java/com/pdg/adventure/view/command/action/TakeActionEditor.java
git commit -m "refactor: migrate TakeActionEditor to AbstractSingleItemActionEditor"
```

---

### Task 4: Refactor `DropActionEditor`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/action/DropActionEditor.java`
- Test: `src/test/java/com/pdg/adventure/view/command/action/DropActionEditorTest.java` (existing — no changes)

**Interfaces:**
- Consumes: `AbstractSingleItemActionEditor<T>` from Task 2

- [ ] **Step 1: Confirm existing tests are green**

```
mvn test -Dtest=DropActionEditorTest
```
Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 2: Replace `DropActionEditor`**

Replace the full content of `src/main/java/com/pdg/adventure/view/command/action/DropActionEditor.java`:

```java
package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.DropActionData;

public class DropActionEditor extends AbstractSingleItemActionEditor<DropActionData> {

    public DropActionEditor(DropActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Drop Action",
              "Select the item the player will drop.",
              "Item to Drop", "Select item to drop", "Please select an item to drop");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
```

- [ ] **Step 3: Run tests**

```
mvn test -Dtest=DropActionEditorTest
```
Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 4: Commit**

```
git add src/main/java/com/pdg/adventure/view/command/action/DropActionEditor.java
git commit -m "refactor: migrate DropActionEditor to AbstractSingleItemActionEditor"
```

---

### Task 5: Refactor `DestroyActionEditor`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/action/DestroyActionEditor.java`
- Test: `src/test/java/com/pdg/adventure/view/command/action/DestroyActionEditorTest.java` (existing — no changes)

**Interfaces:**
- Consumes: `AbstractSingleItemActionEditor<T>` from Task 2

**Note:** The current `DestroyActionEditor.buildUI()` calls `add(itemSelector)` with no title or description. After refactoring the base class adds a title (`"Destroy Action"`) and description. The existing test asserts `getChildren().count() > 0`, which remains true (now 3 children instead of 1). No test changes needed.

- [ ] **Step 1: Confirm existing tests are green**

```
mvn test -Dtest=DestroyActionEditorTest
```
Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 2: Replace `DestroyActionEditor`**

Replace the full content of `src/main/java/com/pdg/adventure/view/command/action/DestroyActionEditor.java`:

```java
package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.DestroyActionData;

public class DestroyActionEditor extends AbstractSingleItemActionEditor<DestroyActionData> {

    public DestroyActionEditor(DestroyActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Destroy Action",
              "Select the item to remove permanently from the adventure.",
              "Item to Destroy", "Select item to destroy", "Please select an item to destroy");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
```

- [ ] **Step 3: Run tests**

```
mvn test -Dtest=DestroyActionEditorTest
```
Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 4: Commit**

```
git add src/main/java/com/pdg/adventure/view/command/action/DestroyActionEditor.java
git commit -m "refactor: migrate DestroyActionEditor to AbstractSingleItemActionEditor"
```

---

### Task 6: Refactor `RemoveActionEditor`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/action/RemoveActionEditor.java`
- Test: `src/test/java/com/pdg/adventure/view/command/action/RemoveActionEditorTest.java` (existing — no changes)

**Interfaces:**
- Consumes: `AbstractSingleItemActionEditor<T>` from Task 2

- [ ] **Step 1: Confirm existing tests are green**

```
mvn test -Dtest=RemoveActionEditorTest
```
Expected: `Tests run: 6, Failures: 0, Errors: 0` (6 tests)

- [ ] **Step 2: Replace `RemoveActionEditor`**

Replace the full content of `src/main/java/com/pdg/adventure/view/command/action/RemoveActionEditor.java`:

```java
package com.pdg.adventure.view.command.action;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.RemoveActionData;

public class RemoveActionEditor extends AbstractSingleItemActionEditor<RemoveActionData> {

    public RemoveActionEditor(RemoveActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Remove Action",
              "Remove (un-wear) a wearable item from the player. Shows all items; ensure you select a wearable one.",
              "Item to Remove", "Select item to remove", "Please select an item to remove");
    }

    @Override
    protected String currentThingId() { return typedAction.getThingId(); }

    @Override
    protected void applyThingId(String id) { typedAction.setThingId(id); }
}
```

- [ ] **Step 3: Run tests**

```
mvn test -Dtest=RemoveActionEditorTest
```
Expected: `Tests run: 6, Failures: 0, Errors: 0`

- [ ] **Step 4: Commit**

```
git add src/main/java/com/pdg/adventure/view/command/action/RemoveActionEditor.java
git commit -m "refactor: migrate RemoveActionEditor to AbstractSingleItemActionEditor"
```

---

### Task 7: Refactor `CreateActionEditor` to use `ViewSupporter`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/action/CreateActionEditor.java`
- Test: `src/test/java/com/pdg/adventure/view/command/action/CreateActionEditorTest.java` (existing — no changes)

**Interfaces:**
- Consumes: `ViewSupporter.collectAllItems(AdventureData)` from Task 1

- [ ] **Step 1: Confirm existing tests are green**

```
mvn test -Dtest=CreateActionEditorTest
```
Expected: `Tests run: 8, Failures: 0, Errors: 0`

- [ ] **Step 2: Replace `CreateActionEditor`**

Replace the full content of `src/main/java/com/pdg/adventure/view/command/action/CreateActionEditor.java`:

```java
package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class CreateActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final CreateActionData createActionData;
    private ComboBox<ItemData> itemSelector;
    private ComboBox<LocationData> containerSelector;

    public CreateActionEditor(CreateActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.createActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Create Action");
        Span description = new Span("Create an item at a specific location");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = ViewSupporter.collectAllItems(adventureData);

        itemSelector = new ComboBox<>("Item to Create");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to create");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (createActionData.getThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(createActionData.getThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                createActionData.setThingId(e.getValue().getId());
            } else {
                createActionData.setThingId(null);
            }
        });

        List<LocationData> allLocations = new ArrayList<>(adventureData.getLocationData().values());

        containerSelector = new ComboBox<>("Container / Location");
        containerSelector.setItems(allLocations);
        containerSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        containerSelector.setPlaceholder("Select container / location");
        containerSelector.setWidthFull();
        containerSelector.setRequired(true);

        if (createActionData.getContainerProviderId() != null) {
            allLocations.stream()
                        .filter(loc -> loc.getId().equals(createActionData.getContainerProviderId()))
                        .findFirst()
                        .ifPresent(containerSelector::setValue);
        }

        containerSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                createActionData.setContainerProviderId(e.getValue().getId());
            } else {
                createActionData.setContainerProviderId(null);
            }
        });

        add(title, description, itemSelector, containerSelector);
    }

    @Override
    public boolean validate() {
        boolean itemValid = itemSelector.getValue() != null;
        boolean containerValid = containerSelector.getValue() != null;

        if (!itemValid) {
            itemSelector.setErrorMessage("Please select an item to create");
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }

        if (!containerValid) {
            containerSelector.setErrorMessage("Please select a container / location");
            containerSelector.setInvalid(true);
        } else {
            containerSelector.setInvalid(false);
        }

        return itemValid && containerValid;
    }

    @Override
    public String getActionSummary() {
        String item = (itemSelector != null && itemSelector.getValue() != null)
                ? ViewSupporter.formatDescription(itemSelector.getValue()) : "?";
        String container = (containerSelector != null && containerSelector.getValue() != null)
                ? ViewSupporter.formatDescription(containerSelector.getValue()) : "?";
        return item + " @ " + container;
    }
}
```

- [ ] **Step 3: Run tests**

```
mvn test -Dtest=CreateActionEditorTest
```
Expected: `Tests run: 8, Failures: 0, Errors: 0`

- [ ] **Step 4: Commit**

```
git add src/main/java/com/pdg/adventure/view/command/action/CreateActionEditor.java
git commit -m "refactor: replace private helpers in CreateActionEditor with ViewSupporter calls"
```

---

### Task 8: Refactor `MoveItemActionEditor` to use `ViewSupporter`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/action/MoveItemActionEditor.java`
- Test: `src/test/java/com/pdg/adventure/view/command/action/MoveItemActionEditorTest.java` (existing — no changes)

**Interfaces:**
- Consumes: `ViewSupporter.collectAllItems(AdventureData)` and `ViewSupporter.collectAllContainers(AdventureData)` from Task 1

- [ ] **Step 1: Confirm existing tests are green**

```
mvn test -Dtest=MoveItemActionEditorTest
```
Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 2: Replace `MoveItemActionEditor`**

Replace the full content of `src/main/java/com/pdg/adventure/view/command/action/MoveItemActionEditor.java`:

```java
package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class MoveItemActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final MoveItemActionData moveActionData;
    private ComboBox<ItemData> itemSelector;
    private ComboBox<ItemContainerData> destinationSelector;

    public MoveItemActionEditor(MoveItemActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.moveActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Move Item Action");
        Span description = new Span("Move an item to a different container");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = ViewSupporter.collectAllItems(adventureData);

        itemSelector = new ComboBox<>("Item to Move");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to move");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (moveActionData.getThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(moveActionData.getThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                moveActionData.setThingId(e.getValue().getId());
            } else {
                moveActionData.setThingId(null);
            }
        });

        List<ItemContainerData> allContainers = ViewSupporter.collectAllContainers(adventureData);

        destinationSelector = new ComboBox<>("Destination Container");
        destinationSelector.setItems(allContainers);
        destinationSelector.setItemLabelGenerator(this::formatContainerLabel);
        destinationSelector.setPlaceholder("Select destination container");
        destinationSelector.setWidthFull();
        destinationSelector.setRequired(true);

        if (moveActionData.getDestinationId() != null) {
            allContainers.stream()
                         .filter(c -> c.getId().equals(moveActionData.getDestinationId()))
                         .findFirst()
                         .ifPresent(destinationSelector::setValue);
        }

        destinationSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                moveActionData.setDestinationId(e.getValue().getId());
            } else {
                moveActionData.setDestinationId(null);
            }
        });

        add(title, description, itemSelector, destinationSelector);
    }

    @Override
    public boolean validate() {
        boolean itemValid = itemSelector.getValue() != null;
        boolean destinationValid = destinationSelector.getValue() != null;

        if (!itemValid) {
            itemSelector.setErrorMessage("Please select an item to move");
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }

        if (!destinationValid) {
            destinationSelector.setErrorMessage("Please select a destination container");
            destinationSelector.setInvalid(true);
        } else {
            destinationSelector.setInvalid(false);
        }

        return itemValid && destinationValid;
    }

    @Override
    public String getActionSummary() {
        String item = (itemSelector != null && itemSelector.getValue() != null)
                ? ViewSupporter.formatDescription(itemSelector.getValue()) : "?";
        String dest = (destinationSelector != null && destinationSelector.getValue() != null)
                ? formatContainerLabel(destinationSelector.getValue()) : "?";
        return item + " @ " + dest;
    }

    private String formatContainerLabel(ItemContainerData container) {
        if (container == null) return "";
        if (adventureData.getPlayerPocket() != null &&
            container.getId().equals(adventureData.getPlayerPocket().getId())) {
            return "your pocket";
        }
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null &&
                location.getItemContainerData().getId().equals(container.getId())) {
                return ViewSupporter.formatDescription(location);
            }
        }
        return ViewSupporter.formatDescription(container);
    }
}
```

- [ ] **Step 3: Run tests**

```
mvn test -Dtest=MoveItemActionEditorTest
```
Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 4: Run all tests to confirm no regressions**

```
mvn test
```
Expected: BUILD SUCCESS, same test count as before (717).

- [ ] **Step 5: Commit**

```
git add src/main/java/com/pdg/adventure/view/command/action/MoveItemActionEditor.java
git commit -m "refactor: replace private helpers in MoveItemActionEditor with ViewSupporter calls"
```
