# Independent Review

## Findings

### P1: Apply role hierarchy to URL authorization

**Location:** `src/main/java/com/pdg/adventure/config/SecurityConfig.java:64-66`

`hasAnyRole` checks only the supplied roles. Consequently, an ADMIN-only account is denied access to `/author/**` and `/player/**`, and an AUTHOR-only account is denied access to `/player/**`, before Vaadin can apply its view access rules. Include inherited roles in these matchers, or configure the request authorization managers with the hierarchy. This violates the required ADMIN > AUTHOR > PLAYER access model documented in `AGENTS-architecture.md:95-106`.

### P2: Do not return null from MessageService

**Location:** `src/main/java/com/pdg/adventure/server/storage/service/MessageService.java:131-134`

When the requested message does not exist, this query returns `null`, forcing every caller to add a null check and contradicting the service contract requiring `Optional<T>` rather than null. Return `Optional<String>` (or handle absence at this boundary) so a missing message cannot become a downstream NPE. See `AGENTS-conventions.md:81-84`.

### P2: Replace reflective editor construction with registered factories

**Location:** `src/main/java/com/pdg/adventure/view/command/action/ActionEditorRegistry.java:39-55`

The classpath scan plus reflection accepts only constructors with the exact concrete data type and optional `AdventureData`. Any editor needing a service, collaborator, or supertype constructor therefore fails only at runtime and cannot use Spring injection. The duplicated action and condition registries add substantial machinery merely to replace a typed registration map; register editor factories or beans directly instead.

### P3: Remove unused constructor dependencies

**Location:** `src/main/java/com/pdg/adventure/server/storage/service/AdventureService.java:37-38`

`MapperSupporter` and `MessageService` are injected but never stored or used, so every `AdventureService` construction unnecessarily creates and couples to unrelated services. Remove these parameters to keep the service dependency graph accurate.

### P3: Rename the misspelled repository

**Location:** `src/main/java/com/pdg/adventure/server/storage/repository/VocabularyReporitory.java:8-9`

`VocabularyReporitory` does not meet the required `*Repository` suffix, which makes repository discovery and code search inconsistent with every other persistence repository. Rename it and update its references. See `AGENTS-conventions.md:5-12`.

### P3: Remove the prohibited dead test fixture

**Location:** `src/test/java/com/pdg/adventure/server/BaseDTO.java:1-3`

`BaseDTO` is an unused proof-of-concept fixture. Project guidance explicitly identifies this file (and `BaseRecord`) as dead code that must be removed when encountered. See `AGENTS-conventions.md:94-96`.

### P3: Remove references to the deleted mapper annotation

**Location:** `src/main/java/com/pdg/adventure/server/annotation/README.md:135-139`

This documentation tells contributors to use `@RegisterMapper` as a fallback even though that annotation has been deleted and is prohibited. Following it produces uncompilable or nonconforming mapper code; document the supported `@AutoRegisterMapper` path only. See `AGENTS-conventions.md:64-66`.

### P4: Simplify mapper auto-registration

**Location:** `src/main/java/com/pdg/adventure/server/annotation/AutoMapperRegistrationProcessor.java:28-154`

Custom mapper registration combines a bean post-processor, delayed singleton initialization, generic-type reflection, a manual fallback resolver, priority sorting, and a pending queue solely to populate one map. The priority ordering has no observable effect because `MapperSupporter` stores one mapper per class in a `HashMap`, so mapper lookup has no ordered behavior. Registration failures are only logged and startup continues, deferring configuration failures to later null or cast failures. Prefer an explicit, typed Spring registry that fails during application startup and is safe to refactor with IDE tooling.

### P4: Replace the generic reflective Mongo cascade-save listener

**Location:** `src/main/java/com/pdg/adventure/server/storage/mongo/CascadeSaveMongoEventListener.java:23-211`

The listener recursively reflects over every field, tracks nested event depth with `ThreadLocal`, manually triggers UUID lifecycle events, and issues nested `MongoTemplate.save()` calls. It duplicates traversal and UUID-assignment logic across several methods. The UUID traversal does not use the cycle-tracking set, so a cyclic embedded object graph can recurse indefinitely. Persistence side effects are hidden from repository and service call sites; explicit aggregate saves, or narrowly scoped persistence methods, would be easier to reason about and test.

### P4: Separate mapper registration from mutable game state

**Location:** `src/main/java/com/pdg/adventure/server/support/MapperSupporter.java:23-160`

`MapperSupporter` is both a mapper registry and a global holder of mutable game state, vocabulary, messages, variables, items, locations, and containers. This service-locator design drives the `@Lazy` state beans in `AdventureConfig` and makes dependencies implicit. Separate the mapper registry from adventure-run state to make object ownership and required dependencies explicit. This is a longer-term architectural refactor rather than an immediate correctness issue.
