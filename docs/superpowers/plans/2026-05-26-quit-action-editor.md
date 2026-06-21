# QuitAction Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow adventure authors to attach a `QuitAction` to any verb|adjective|noun command via the `CommandEditorView`, so the game terminates when that command is executed.

**Architecture:** Follow the established action pipeline exactly — `QuitActionData` (model) → `QuitActionMapper` (server mapper) → `QuitActionEditor` (Vaadin UI) — then register both the factory case and selector entry. The hardcoded global "quit" interceptor in `CommandFactory` is left untouched.

**Tech Stack:** Java 25, Spring Boot, Vaadin Flow, Lombok, JUnit 5 + AssertJ, Mockito

---

## File Map

| Status | File | Purpose |
|---|---|---|
| Create | `src/main/java/com/pdg/adventure/model/action/QuitActionData.java` | No-field data model |
| Create | `src/main/java/com/pdg/adventure/server/mapper/action/QuitActionMapper.java` | Data ↔ action mapper |
| Create | `src/main/java/com/pdg/adventure/view/command/action/QuitActionEditor.java` | No-parameter editor UI |
| Create | `src/test/java/com/pdg/adventure/server/mapper/action/QuitActionMapperTest.java` | Mapper unit tests |
| Create | `src/test/java/com/pdg/adventure/view/command/action/QuitActionEditorTest.java` | Editor unit tests |
| Modify | `src/main/java/com/pdg/adventure/view/command/action/ActionEditorFactory.java` | Add switch case |
| Modify | `src/main/java/com/pdg/adventure/view/command/action/ActionSelector.java` | Add selector entry |
| Modify | `src/test/java/com/pdg/adventure/view/command/action/ActionEditorFactoryTest.java` | Add factory test |

---

## Task 1: QuitActionData model

**Files:**
- Create: `src/main/java/com/pdg/adventure/model/action/QuitActionData.java`

- [ ] **Step 1: Create the data class**

```java
package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class QuitActionData extends ActionData {
}
```

- [ ] **Step 2: Verify compilation**

Run from `server/`:
```bash
mvn compile -q
```
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/pdg/adventure/model/action/QuitActionData.java
git commit -m "Add QuitActionData model"
```

---

## Task 2: QuitActionMapper with tests

**Files:**
- Create: `src/main/java/com/pdg/adventure/server/mapper/action/QuitActionMapper.java`
- Create: `src/test/java/com/pdg/adventure/server/mapper/action/QuitActionMapperTest.java`

- [ ] **Step 1: Write the failing tests**

```java
package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.server.action.QuitAction;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class QuitActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;
    @Mock private MessagesHolder messagesHolder;

    @InjectMocks private QuitActionMapper mapper;

    @Test
    void mapToBO_returnsQuitAction() {
        QuitActionData data = new QuitActionData();

        QuitAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(QuitAction.class);
    }

    @Test
    void mapToDO_returnsQuitActionData() {
        QuitAction action = new QuitAction(messagesHolder);

        QuitActionData data = mapper.mapToDO(action);

        assertThat(data).isNotNull().isInstanceOf(QuitActionData.class);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
mvn test -pl . -Dtest=QuitActionMapperTest -q 2>&1 | tail -5
```
Expected: compilation error (QuitActionMapper not found).

- [ ] **Step 3: Implement the mapper**

```java
package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.server.action.QuitAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Quit action mapper")
public class QuitActionMapper extends ActionMapper<QuitActionData, QuitAction> {

    private final MessagesHolder messagesHolder;

    public QuitActionMapper(MapperSupporter aMapperSupporter, MessagesHolder aMessagesHolder) {
        super(aMapperSupporter);
        messagesHolder = aMessagesHolder;
        aMapperSupporter.registerMapper(QuitActionData.class, QuitAction.class, this);
    }

    @Override
    public QuitAction mapToBO(QuitActionData data) {
        return new QuitAction(messagesHolder);
    }

    @Override
    public QuitActionData mapToDO(QuitAction action) {
        return new QuitActionData();
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
mvn test -Dtest=QuitActionMapperTest -q 2>&1 | tail -5
```
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/mapper/action/QuitActionMapper.java \
        src/test/java/com/pdg/adventure/server/mapper/action/QuitActionMapperTest.java
git commit -m "Add QuitActionMapper with tests"
```

---

## Task 3: QuitActionEditor with tests

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/action/QuitActionEditor.java`
- Create: `src/test/java/com/pdg/adventure/view/command/action/QuitActionEditorTest.java`

- [ ] **Step 1: Write the failing tests**

```java
package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.QuitActionData;

class QuitActionEditorTest {

    @Test
    void constructor_shouldSetActionData() {
        QuitActionData actionData = new QuitActionData();

        QuitActionEditor editor = new QuitActionEditor(actionData);

        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void validate_shouldAlwaysReturnTrue() {
        QuitActionData actionData = new QuitActionData();
        QuitActionEditor editor = new QuitActionEditor(actionData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void initialize_shouldBuildUIChildren() {
        QuitActionData actionData = new QuitActionData();
        QuitActionEditor editor = new QuitActionEditor(actionData);

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
mvn test -Dtest=QuitActionEditorTest -q 2>&1 | tail -5
```
Expected: compilation error (QuitActionEditor not found).

- [ ] **Step 3: Implement the editor**

```java
package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.QuitActionData;

public class QuitActionEditor extends ActionEditorComponent {

    public QuitActionEditor(QuitActionData actionData) {
        super(actionData);
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Quit Action");

        Span description = new Span("Terminate the game when this command is executed.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ This action takes no parameters. Precede it with a Message action to show a farewell message.");

        add(title, description, info);
    }

    @Override
    public boolean validate() {
        return true;
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
mvn test -Dtest=QuitActionEditorTest -q 2>&1 | tail -5
```
Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/action/QuitActionEditor.java \
        src/test/java/com/pdg/adventure/view/command/action/QuitActionEditorTest.java
git commit -m "Add QuitActionEditor with tests"
```

---

## Task 4: Wire-up and factory test

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/action/ActionEditorFactory.java`
- Modify: `src/main/java/com/pdg/adventure/view/command/action/ActionSelector.java`
- Modify: `src/test/java/com/pdg/adventure/view/command/action/ActionEditorFactoryTest.java`

- [ ] **Step 1: Add the factory test**

In `ActionEditorFactoryTest.java`, add the import and a new `@Test` method alongside the existing ones:

Import to add (with the other action data imports):
```java
import com.pdg.adventure.model.action.QuitActionData;
```

Test method to add:
```java
@Test
void createEditor_withQuitActionData_shouldReturnQuitActionEditor() {
    ActionEditorComponent editor = ActionEditorFactory.createEditor(new QuitActionData(), adventureData);
    assertThat(editor).isNotNull().isInstanceOf(QuitActionEditor.class);
}
```

- [ ] **Step 2: Run factory test to verify it fails**

```bash
mvn test -Dtest=ActionEditorFactoryTest -q 2>&1 | tail -5
```
Expected: `UnsupportedOperationException: No editor available for action type: QuitActionData`

- [ ] **Step 3: Wire up ActionEditorFactory**

Add the import (with the other action data imports):
```java
import com.pdg.adventure.model.action.QuitActionData;
```

Add the switch case after the `WearActionData` case:
```java
case QuitActionData quitActionData -> new QuitActionEditor(quitActionData);
```

- [ ] **Step 4: Wire up ActionSelector**

Add the import (with the other action data imports):
```java
import com.pdg.adventure.model.action.QuitActionData;
```

Add the descriptor entry at the end of `getAvailableActionTypes()`, before `return types;`:
```java
types.add(new ActionTypeDescriptor("Quit", "Terminate the game", QuitActionData::new));
```

Note: `QuitActionData::new` works here because `@Data` with no fields generates a no-arg constructor via `@RequiredArgsConstructor`. No lambda wrapper needed (unlike `SetVariableActionData`).

- [ ] **Step 5: Run the full test suite**

```bash
mvn test -q 2>&1 | tail -5
```
Expected: all tests pass, count increases by 6 over the previous baseline of 534.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/action/ActionEditorFactory.java \
        src/main/java/com/pdg/adventure/view/command/action/ActionSelector.java \
        src/test/java/com/pdg/adventure/view/command/action/ActionEditorFactoryTest.java
git commit -m "Wire up QuitActionEditor in factory and selector"
```
