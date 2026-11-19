# Eclipse JNoSQL – Common Development Guidelines

Shared engineering rules for contributors and AI coding agents working on this repository (`jnosql-communication` and `jnosql-mapping`).

Read first: [`README.adoc`](../README.adoc), [`CONTRIBUTING.adoc`](../CONTRIBUTING.adoc), [`TESTING-GUIDELINE.adoc`](../TESTING-GUIDELINE.adoc) (authoritative for tests), [`MAPPING.adoc`](../MAPPING.adoc), [`CHANGELOG.adoc`](../CHANGELOG.adoc).

Terms: **MUST** is mandatory; **SHOULD** is the default unless there is a documented reason.

---

## 1. Architectural overview

```
Application / Jakarta Data repositories / Template
        │  (CDI, MicroProfile Config)
        ▼
jnosql-mapping      entity <-> communication model, metadata, repositories, templates
        │
        ▼
jnosql-communication   database-neutral model, values, queries, SPI
        │
        ▼
jnosql-databases (separate repository)   driver per database
```

| Concern | Where | Responsibility |
|---|---|---|
| Communication layer | `jnosql-communication-core`, `-semistructured`, `-key-value`, `-query` | Database-neutral model and contracts. `Value`, `ValueReader`/`ValueWriter`, `Settings`, `CommunicationException`; semistructured model (`CommunicationEntity`, `Element`, `DatabaseManager`, `DatabaseManagerFactory`, `SelectQuery`, `DeleteQuery`, `UpdateQuery`); key-value configuration (`KeyValueConfiguration`). It knows nothing about entities or annotations. |
| Query parsing | `jnosql-communication-query` | Own ANTLR-based parser for the JDQL subset (`SelectProvider`, `DeleteProvider`, `UpdateProvider`, `org.eclipse.jnosql.communication.query.method`). |
| Mapping layer | `jnosql-mapping-api-core`, `-core`, `-reflection`, `-semistructured`, `-column`, `-document`, `-graph`, `-key-value`, `-timeseries`, `-vector` | Object mapping. Metadata (`EntityMetadata`, `FieldMetadata`, `RepositoryMetadata` in `org.eclipse.jnosql.mapping.metadata`), reflection-based discovery (`jnosql-mapping-reflection`), entity ↔ `CommunicationEntity` conversion (`EntityConverter`, `FieldConverter`), templates (`AbstractSemiStructuredTemplate`, `SemiStructuredTemplate`), events (`EntityPrePersist`, `EntityPostPersist`), qualifiers (`@Database`, `DatabaseType`). |
| Jakarta NoSQL | `jakarta.nosql` (`Template`, `@Entity`, `@Id`, `@Column`, `@Convert`, `AttributeConverter`, …) | Standardized annotations and the common `Template`. JNoSQL implements them in the mapping layer. |
| Jakarta Data | `jakarta.data` | Standardized `@Repository`, `Page`, `Sort`, `Restriction`, etc. Implemented by the repository engine: `jnosql-mapping-core` (`CoreRepositoryInvocationHandler`, `RepositoryReturn` implementations, `operations`) and `jnosql-mapping-semistructured` (`SemiStructuredRepositoryProxy`, `CustomRepositoryHandler`, `DynamicQuery`). |
| Jakarta Query | not a dependency | **Not integrated.** There is no dependency on the Jakarta Query API. The current implementation is the in-repo JDQL parser (`jnosql-communication-query`). Do not add Jakarta Query types, claims, or docs until that changes (see README, “Querying with Jakarta Query”). |
| CDI | `jakarta.enterprise` | Wiring only: beans and extensions (`AbstractBean`, `RepositoryBean`, `CustomRepositoryBean`, `Converters`, `*ManagerSupplier`, `spi` packages) and qualifier resolution. |
| MicroProfile Config | `MicroProfileSettings` (`mapping.core.config`) | Source of configuration, translated into the communication-level `Settings`. |
| Database drivers | **separate repository** `eclipse-jnosql/jnosql-databases` | Implement `DatabaseConfiguration`/`DatabaseManager`/`DatabaseManagerFactory` (or key-value equivalents) and are discovered through SPI. Do not add driver code or driver-specific behavior here. |

### Boundary rules

1. Dependencies point downward only: mapping → communication. Communication MUST NOT import `org.eclipse.jnosql.mapping`, `jakarta.nosql` mapping annotations, or CDI.
2. Mapping code MUST NOT depend on a concrete driver. It talks to `DatabaseManager`, `BucketManager`, or other communication contracts.
3. CDI/MicroProfile Config belong at the edges (`spi`, `configuration`, `*Supplier` classes). Core conversion and query logic SHOULD be usable and testable without a container.
4. Database-model modules (`column`, `document`, `graph`, `key-value`, `timeseries`, `vector`) adapt the shared semistructured engine; model-specific behavior stays in its module, shared behavior moves down to `mapping-semistructured` or `mapping-core`.
5. Specialized APIs (graph, time series, vector templates, `@Database`, …) are JNoSQL features, not standardized APIs. Keep them clearly separated from `jakarta.nosql`/`jakarta.data` behavior.
6. Discovery of providers goes through `ServiceDiscovery` / `ServiceLoader` and `module-info.java` `provides … with` declarations. A new reader, writer, or provider MUST be registered in both `module-info.java` and `META-INF/services` when the module does so for its siblings.

---

## 2. Semantic code

**Why:** this framework is extended by third-party driver authors and used by application developers through stable contracts. Code that states intent is cheaper to review, safer to change, and harder to misuse. Surprising names, mixed return conventions, and silent nulls become bugs in every driver.

### Rules

- **Name by intent, not mechanics.** Use domain terms: `EntityConverter`, `RepositoryReturn`, `DatabaseManagerFactory`, `UnsatisfiableQueryException`. Avoid `Helper`, `Manager`, `Util`, `Processor`, `Handler` unless the class really has that role (`CustomRepositoryHandler` handles custom repository methods). `*Utils` classes (e.g. `ParamValueUtils`) are for stateless, private-in-spirit helpers, not a place for unrelated logic.
- **One reason to change per class.** If a class name needs “And”, or its Javadoc needs a list of unrelated duties, split it.
- **Small methods.** A method SHOULD fit on a screen and do one thing at one level of abstraction. Extract a well-named private method rather than commenting a block. Nested `if`/loops beyond two levels are a signal to extract.
- **Consistent exceptions.**
  - Invalid arguments: `NullPointerException` via `Objects.requireNonNull(x, "x is required")` (the repository convention, e.g. `Converters.get`).
  - Failures in the communication layer: `CommunicationException` (unchecked) with a message carrying the offending value and the original cause, as in `UUIDValueReader`.
  - Mapping failures: the module’s existing exception (`MetadataException`, `IdNotFoundException`, `DynamicQueryException`, `UnsatisfiableQueryException`, …). Extend an existing one before creating a new type.
  - Never swallow exceptions; never catch `Exception`/`Throwable` to return a default.
- **Consistent return values.** Return `Optional<T>` for “maybe absent” single results (`FieldParameterMetadata.converter()`), empty collections instead of `null` for collections, `boolean` for predicates. Do not mix “returns `null`” and “returns `Optional`” for the same kind of lookup.
- **Predictable null behavior.** Public methods MUST either reject `null` (documented `@throws NullPointerException`) or define what it means (documented `@return … or null`). Never leave it undefined. Existing documented nullable returns (e.g. `Value.get(Class)` returns `null` for a null value) MUST be preserved.
- **Immutable value objects.** Use records or final classes with final fields for data carriers that are compared by value (`Settings`/`DefaultSettings`, `DefaultValue`, `DefaultFieldValue`, query parameter holders). Implement `equals`/`hashCode`/`toString` consistently, and copy mutable inputs defensively. Do not make classes immutable when they intentionally accumulate state (builders, `CommunicationEntity`).
- **Visibility.** Fields are `private` (Checkstyle `VisibilityModifier`); `protected` is tolerated, not encouraged. Utility classes have private constructors (`HideUtilityClassConstructor`).
- **No dead flexibility.** No unused parameters, flags that alter the meaning of a method (`save(entity, true)`), or `Object`-typed APIs where a type parameter works.

```java
// Prefer: intent and null contract visible in the signature
public <X, Y> AttributeConverter<X, Y> get(FieldParameterMetadata metadata) {
    Objects.requireNonNull(metadata, "The metadata is required");
    return getInstance(metadata);
}
```

---

## 3. SOLID principles

Apply SOLID to solve a present problem. Do **not** create an interface, factory, or hierarchy that has a single implementation and no foreseeable second one, or only to “satisfy” a principle.

### Single Responsibility

Split by reason to change. `AbstractSemiStructuredTemplate` orchestrates persistence; conversion lives in `EntityConverter`, field/value conversion in `FieldConverter`, event dispatch in `EventPersistManager`, query parsing in the `communication.semistructured` `*Parser` classes. A change in event handling should not touch the converter.

*Smell:* a class that mixes CDI lookup, reflection, and query building. Move each concern to its own collaborator.

### Open/Closed

Add behavior by adding an implementation, not by editing a central conditional. `RepositoryReturn` has one implementation per return shape (`ListRepositoryReturn`, `OptionalRepositoryReturn`, `PageRepositoryReturn`, `StreamRepositoryReturn`, …); supporting a new return type means adding a class that answers `isCompatible`. Likewise `ValueReader`/`ValueWriter` are extended via SPI without touching `Value`.

Do not make something extensible “just in case”. Extension points are public contracts (see §6): add them only for a demonstrated need.

### Liskov Substitution

Any implementation MUST honor the contract of the type it implements: same preconditions (not stricter), same postconditions (not weaker), same exception types. Examples: every `ValueReader.read` implementation returns the requested type or fails consistently with `CommunicationException`; `DatabaseManagerFactory.apply(null)` throws `NullPointerException` for all implementations; `DatabaseManagerFactory.close()` is safe to call even when the driver holds no resources.

*Smell:* `instanceof` checks on an abstraction’s implementations, or overrides that throw `UnsupportedOperationException` for an inherited method. Redesign the hierarchy or use composition.

### Interface Segregation

Keep contracts narrow. `ValueReader` only reads (`read` + `test`); `ValueWriter` only writes. `RepositoryReturn` has three methods that always go together. If implementers routinely leave methods empty or throw, split the interface. Do not add a method to a public interface for one caller’s convenience; prefer a `default` method only when it has a correct universal implementation (and note the compatibility rules in §8).

### Dependency Inversion

High-level code depends on abstractions it owns. The mapping layer depends on `DatabaseManager`, `EntityConverter`, `EntitiesMetadata`, `Converters`, not on a driver or reflection implementation. Inject collaborators through constructors or CDI `@Inject` rather than calling `CDI.current()` or `new` on a concrete type deep in logic. Prefer constructor injection for new code so classes remain unit-testable without a container.

---

## 4. Design patterns

Use a pattern when it removes real duplication or a real branching problem. A guard clause is not a Strategy problem.

| Pattern | Use when | Existing example |
|---|---|---|
| **Strategy** | Behavior genuinely varies independently of the caller, and variants grow over time. | `RepositoryReturn` implementations; `ValueReader`/`ValueWriter` selected by `test(Class)`; `InfrastructureOperatorProvider`/`RepositoryOperationProvider` operators. |
| **Factory** | Creating an object requires configuration, selection, or hides the concrete type. | `EntityConverterFactory`, `DatabaseManagerFactory`, `CustomRepositoryHandlerBuilder`, `SettingsBuilder`. |
| **Adapter** | Wrapping an external or incompatible API behind your contract. | `MicroProfileSettings` adapts MicroProfile Config to `Settings`; drivers in `jnosql-databases` adapt a vendor client to `DatabaseManager`. |
| **Decorator** | Adding behavior around an existing contract. | `ValueReaderDecorator`, `ValueWriterDecorator`, `TypeReferenceReaderDecorator`. |
| **Template method** | A fixed algorithm with a few overridable steps. | `AbstractBean`, `AbstractRepositoryInvocationHandler`. Prefer composition for new designs. |

### Rules

- **Composition over inheritance.** Inherit only for a true is-a relationship with shared invariants. Prefer delegating to a collaborator over extending `Abstract*` classes; every abstract base class increases coupling for every subclass.
- **Polymorphism instead of growing conditional chains.** When an `if/else if` or `switch` selects *independently varying behavior* by type, kind, or capability, and more cases are plausible, replace it with a polymorphic contract (as `RepositoryReturn` does for return types) or a lookup of strategies.
- **Keep simple conditionals.** Guard clauses, null/argument checks, a two-way branch, or a `switch` over a closed enum that is converted to one value are fine. Do not wrap them in strategies.
- **Avoid God classes.** Large `Abstract*` classes (e.g. `AbstractSemiStructuredTemplate`) are a warning, not a model: new responsibilities go to a collaborator, not into them.
- **No speculative frameworks.** Do not build plugin registries, generic pipelines, or configuration DSLs for one use case. Introduce an abstraction on the second real use.
- **Avoid excessive abstraction.** If a reader must open four files to understand one operation, flatten it.

---

## 5. Public API design

**What is “public API”.** A type is supported API only if it is part of an exported package intended for developers or providers, documented as such, and stable. A `public` modifier alone does not make a class supported API: many classes are public only because Java requires it across packages or for CDI/reflection (`Default*` implementations, `*Utils`, `Abstract*` support classes). Treat these as internal; do not advertise them in docs.

### Rules

1. **Minimal public surface.** Start with package-private/private. Make a type or member public only for a consumer who needs it. Do not export new packages in `module-info.java` casually; `exports` is a compatibility promise.
2. **Separate API from implementation.** Contracts are interfaces/abstract contracts (`ValueReader`, `EntityConverterFactory`, `RepositoryReturn`, `EntityMetadata`); implementations are `Default*` or in dedicated packages (`jnosql-mapping-reflection` implements the metadata API from `jnosql-mapping-api-core`). Callers MUST depend on the contract. Do not leak implementation types in signatures, return types, or exception messages.
3. **Do not expose internals** (collections that can be mutated, caches, CDI contexts, reflection `Field`/`Method` objects) unless that is the contract. Return unmodifiable copies or views.
4. **Semantic consistency.** The same concept uses the same name, argument order, null behavior, and exception across modules. When adding to `column`/`document`/`key-value`/`graph`/`vector`/`timeseries`, mirror the sibling API unless the database model truly differs.
5. **Exception contracts.** Document and keep stable: which unchecked exception, when. Use `NullPointerException` for null arguments, `IllegalArgumentException` for invalid values, `CommunicationException` for communication failures, and module-specific exceptions for mapping errors. Changing the thrown type is a breaking change.
6. **Generic types and type safety.** Use type parameters instead of `Object`/raw types (`<T> T read(Class<T> type, Object value)`, `AttributeConverter<X, Y>`). Confine unchecked casts to one private spot with a minimal `@SuppressWarnings("unchecked")` and a justification (as `UUIDValueReader` does). Never leak raw types in public signatures.
7. **UUID and converter round-trip.** A value written to the database and read back MUST produce an equal value. Specifically:
   - `UUIDValueReader` accepts `UUID` (returned as-is) and `CharSequence` in canonical UUID format; malformed text throws `CommunicationException` with the offending value; other types return `null`. Preserve this when touching it.
   - Writers and readers MUST be inverse for supported types. When changing a `ValueWriter` or `ValueReader`, or a user-facing `AttributeConverter` path (`Converters`, `FieldConverter`), add a test that converts entity → `CommunicationEntity` → entity and compares with the original, including `null`, empty, and boundary values.
   - Do not change the stored representation of an existing type (for example UUID as `String` vs. native) without a changelog entry and migration note (see `MIGRATION.adoc`).
8. **Deprecation.** Deprecate before removal (`@Deprecated(forRemoval = true)` plus Javadoc `@deprecated` naming the replacement, as `RepositoryType` does). Record removals in `CHANGELOG.adoc`.

---

## 6. Javadoc standards

Audience: Java developers using or extending the framework.

### What requires Javadoc

- **MUST:** every developer-facing public interface, class, enum, record, and annotation, and every public/protected method and constructor on them.
- **SHOULD:** short `{@code}` / `<pre>{@code …}</pre>` examples for important operations (creating a template, running a query, implementing an SPI).
- **NOT required:** private and package-private members, tests, generated code (`jnosql-communication-query` grammar). A short comment is fine where logic is non-obvious, but do not restate the code.
- **Internal public classes** (public only for technical reasons) SHOULD state it, e.g. “Internal implementation; not part of the supported API”, and MUST NOT carry examples that invite use.

### Content rules

- First sentence: a summary of what the type or method does, in third person, ending with a period. No “This class …” filler.
- `@param` for every parameter, `@param <T>` for every type parameter, `@return` for every non-void method, `@throws` for each exception that is part of the contract (null arguments included). Describe constraints and null behavior, not the type.
- Document **actual** behavior and limitations: thread safety, caching, what happens on missing/duplicate data, and database-provider differences.
- Keep docs in sync with code. A behavior change without a Javadoc change is incomplete.
- Use `{@link}` for types and members, `{@code}` for literals; `<p>` for paragraphs; escape `<`, `>`, `&` or wrap in `{@code}`; no self-closing `<p/>`, no unterminated tags. Use `@see` sparingly.
- Include `@since` only if the module already uses it for that type.

### Example (from `Value`, `UUIDValueReader`)

```java
/**
 * Converts the wrapped value to the given type.
 *
 * <pre>{@code
 * UUID id = value.get(UUID.class);
 * }</pre>
 *
 * @param type the target type
 * @param <T>  the target type
 * @return the converted instance, or {@code null} if the value is {@code null}
 * @throws NullPointerException   when {@code type} is {@code null}
 * @throws CommunicationException when the value cannot be converted
 */
<T> T get(Class<T> type);
```

Validate with `mvn javadoc:javadoc`; Javadoc errors in touched modules MUST be fixed.

---

## 7. Testing

[`TESTING-GUIDELINE.adoc`](../TESTING-GUIDELINE.adoc) is authoritative (JUnit 5, AssertJ, `should…` names, `@DisplayName`, `@Nested` `WhenThe<Operation>`, soft assertions). Do not duplicate it; this section only adds architectural expectations.

- **Unit tests** per module, no container, no database. Test conversion, parsing, metadata, and return strategies in isolation. Prefer real small objects over mocks.
- **Integration tests** for CDI wiring, SPI discovery, and repository proxies run in the CDI test setup that the module already uses; keep them few and focused on wiring. Database-backed tests belong in `jnosql-databases`.
- **Cover three classes of case for every behavior:** positive, negative (invalid input, exceptions, with the exact exception type and message), and boundary (null, empty, single element, limits, UUID malformed, `Page`/`Limit` edges).
- **Jakarta compatibility.** Behavior defined by Jakarta NoSQL or Jakarta Data MUST follow the specification, even where JNoSQL could do more. Check the specification and its TCK expectations when changing standardized behavior. Never change a Jakarta-defined result to suit a JNoSQL feature.
- **Regression.** Every bug fix MUST add a test that fails before the fix and passes after. Name it after the behavior, not the issue number.
- **Mocks (Mockito).** Mock *collaborator contracts* at an architectural boundary (`DatabaseManager`, `EntitiesMetadata`). Do not mock value objects or the class under test, and do not verify internal calls when observable behavior can be asserted (guideline §3.4).
- **Mutation testing.** PIT is configured in the root POM (`pitest-maven`, `equals`/`hashCode`/`toString` excluded). Run it per `TESTING-GUIDELINE.adoc` §4 (`mvn clean test-compile -P pitest`) for classes you change, and add tests for surviving mutants that represent real behavior.
- Coverage is collected by JaCoCo; improve *meaningful* coverage, never write tests only to move a number.

---

## 8. Compatibility

- **Contracts are the product.** Public signatures, thrown exceptions, null handling, ordering, defaults, stored representations, and SPI registration are all part of the contract, not only method signatures.
- **Do not change existing behavior silently.** If a fix alters observable behavior, call it out in the PR, add a test, and add a `CHANGELOG.adoc` entry (`Fixed`/`Changed`/`Removed`) under `[Unreleased]`.
- **Safe changes:** adding a new class, a new method on a class not meant for extension, a new `ValueReader`/`RepositoryReturn` implementation, new Javadoc.
- **Risky changes:** adding abstract methods to an interface (breaks implementers; use a `default` method with a correct behavior), changing a return type, narrowing parameters, throwing new/different exceptions, changing `equals`/`hashCode`, changing the stored shape of a value, reordering SPI selection, and altering `module-info.java` `exports`/`opens`/`provides`.
- **Remove or rename only through deprecation** (§5.8). Migration-impacting changes go into `MIGRATION.adoc`.
- **Standardized vs. framework features.**
  - Standardized: `jakarta.nosql` (`Template`, entity annotations, `AttributeConverter`) and `jakarta.data` (repositories, `Page`, `Sort`, `Restriction`, …). Behavior is dictated by the specification and the version declared in the root POM (`jakarta.nosql.version`, `jakarta.data.api.version`; currently milestone versions).
  - JNoSQL-specific: `@Database`/`DatabaseType` qualifiers, graph / time-series / vector templates, semistructured template extras, `jnosql-communication` contracts, and the JDQL parser. These may evolve, but still follow the rules above.
  - Do not present a JNoSQL extension as a Jakarta standard in code, Javadoc, or docs, and do not describe unimplemented capabilities (including Jakarta Query integration) as available.
- Be careful with the Java baseline (`maven.compiler` in the root POM, Java 21 per `CHANGELOG.adoc`) and avoid newer language/API features than the build allows.

---

## 9. Definition of Done

- [ ] **Architecture:** dependencies point downward; communication has no mapping/CDI/Jakarta mapping imports; no driver code; CDI/Config only at the edges.
- [ ] **API compatibility:** no unintended signature, exception, null, or stored-value change; new public types are intentional and minimal; deprecations follow §5.8; `CHANGELOG.adoc` (and `MIGRATION.adoc` when needed) updated.
- [ ] **Semantic design:** intent-revealing names, single responsibility, small methods, consistent exceptions/returns, explicit null behavior, no needless abstraction or growing type-based conditionals.
- [ ] **Tests:** positive, negative, and boundary cases; regression test for each fix; converter/UUID round-trips covered when touched; follows `TESTING-GUIDELINE.adoc`; PIT checked for changed logic.
- [ ] **Javadoc:** all developer-facing public types and members documented with accurate `@param`/`@return`/`@throws`, examples for key operations, valid Javadoc syntax.
- [ ] **Build:** `mvn clean verify` passes for affected modules (compile, tests, Checkstyle, PMD, Apache RAT license headers, Javadoc); new files carry the license header.
- [ ] **Git:** Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, …), every commit signed off with `git commit -s` (DCO), no automated tool listed as author or co-author, one logical change per commit.
