# Eclipse JNoSQL – Communication Layer Development Guide

How to maintain and extend `jnosql-communication`. Read [`common.md`](common.md) first; it defines the shared rules (boundaries, semantic code, SOLID, API design, Javadoc, Definition of Done). This guide only adds what is specific to Communication. Testing rules live in [`TESTING-GUIDELINE.adoc`](../TESTING-GUIDELINE.adoc); contribution rules in [`CONTRIBUTING.adoc`](../CONTRIBUTING.adoc).

---

## 1. Purpose and responsibilities

The Communication layer is the **database-neutral contract between Eclipse JNoSQL and database-native drivers**. Drivers (in the separate `jnosql-databases` repository) implement these contracts; the mapping layer consumes them.

It owns:

- a value model and conversion SPI (`Value`, `ValueReader`, `ValueWriter`);
- configuration (`Settings`, `DatabaseConfiguration`, `KeyValueConfiguration`);
- the operation contracts (`DatabaseManager`, `BucketManager`) and their factories;
- the neutral query model (`SelectQuery`, `DeleteQuery`, `UpdateQuery`, `CriteriaCondition`);
- parsing of JDQL and method-name queries into a neutral representation.

It does **not** own:

| Not here | Why / where |
|---|---|
| Object mapping (entities, annotations, `AttributeConverter`, metadata) | `jnosql-mapping-*`. Communication deals with `CommunicationEntity`, `Element`, `Value`, never with user entity classes. |
| CDI-managed repositories, Jakarta Data repository proxies, `Template` | `jnosql-mapping-*`. Communication has no CDI dependency and no `jakarta.nosql` import. |
| Vendor drivers and native query syntax | `jnosql-databases`. No driver, client library, or database-specific syntax is added here. |

`jakarta.data` types (`Sort`, `PageRequest`, `CursoredPage`) are used as neutral value types in query and paging contracts (see `module-info.java` of the semistructured, key-value, and query modules). Do not add other specification dependencies without a design discussion.

---

## 2. Module architecture

Dependencies: `semistructured` and `key-value` → `query` → `core`.

| Module | Package | Responsibility | Key sources |
|---|---|---|---|
| `jnosql-communication-core` | `org.eclipse.jnosql.communication` | Value model, conversion SPI, settings, shared exceptions, `Condition`, `Params`. | `Value`, `DefaultValue`, `ValueReader`, `ValueReaderDecorator`, `ValueWriter`, `ValueWriterDecorator`, `TypeReference`, `TypeReferenceReader`, `Settings`, `SettingsBuilder`, `Condition`, `Params`, `ParamValue`, `CommunicationException`, `QueryException`, `ValueUtil` |
| | `…communication.reader` / `.writer` | Built-in readers and writers registered via `module-info.java` `provides` and `META-INF/services`. | `IntegerReader`, `UUIDValueReader`, `EnumReader`, `LocalDateReader`, `TemporalValueWriter`, `EnumValueWriter`, … |
| | `…communication.util` | Provider discovery. | `ServiceDiscovery` |
| `jnosql-communication-query` | `…communication.query` | Neutral parsed-query model: `Query`, `SelectQuery`, `DeleteQuery`, `UpdateQuery`, `Where`, `QueryCondition`, `QueryValue<T>`, `ValueType`, `QueryErrorListener`. | |
| | `…communication.query.data` | ANTLR-based JDQL parsing (`antlr4/…/JDQL.g4`). | `SelectProvider`, `DeleteProvider`, `UpdateProvider`, `SelectParser`, `DeleteParser`, `UpdateParser` |
| | `…communication.query.method` | Query-by-method-name parsing (`antlr4/…/Method.g4`). | `SelectMethodProvider`, `DeleteMethodProvider`, `SelectMethodQueryParser`, `QueryTokenizer` |
| `jnosql-communication-semistructured` | `…communication.semistructured` | Contract for column, document, and graph-like databases: data model, query model, builders, query-to-condition translation, paging. | `DatabaseManager`, `DatabaseManagerFactory`, `DatabaseConfiguration`, `CommunicationEntity`, `Element`, `Elements`, `CriteriaCondition`, `SelectQuery`, `DeleteQuery`, `UpdateQuery`, `QueryParser`, `Conditions`, `Values`, `CommunicationPreparedStatement`, `CursorExecutor` |
| `jnosql-communication-key-value` | `…communication.keyvalue` | Contract for key-value databases. | `BucketManager`, `BucketManagerFactory`, `KeyValueConfiguration`, `KeyValueEntity` |

Rules:

- `core` MUST NOT depend on any other Communication module.
- `query` MUST NOT depend on `semistructured` or `key-value`. It produces a representation; translation to executable queries is the job of the consuming module (`Conditions`, `QueryParser`).
- `semistructured` and `key-value` MUST NOT depend on each other.
- Generated ANTLR code and the `query` packages are excluded from PMD/Checkstyle (see root `pom.xml`); hand-written code in those modules is still reviewed to the same standard.
- Public packages are exported in `module-info.java`; adding an `exports`/`opens`/`provides`/`uses` line is a compatibility decision (see `common.md` §8).

---

## 3. Communication contracts

Only interfaces and classes that exist today are described.

### Key-value: `BucketManager`, `BucketManagerFactory`, `KeyValueConfiguration`

- `BucketManager` is a small CRUD contract: `put` (single, with `Duration` TTL, iterables), `get` (returns `Optional<Value>` / `Iterable<Value>`), `delete`, `name()`, `close()`. Values are returned as `Value`, so conversion is delegated to `ValueReader`.
- `BucketManagerFactory extends Function<String, BucketManager>, AutoCloseable` creates managers by bucket name and also exposes `getList`, `getSet`, `getQueue`, `getMap`.
- `KeyValueConfiguration extends Function<Settings, BucketManagerFactory>` is the entry point; static `getConfiguration()` / `getConfiguration(Class)` look up providers through `ServiceLoader` on every call (deliberate, per the comment in the source, for application servers) and throw `CommunicationException` when none is found.
- Keep it small. Database-specific capabilities (queries, scans, transactions) are not added to `BucketManager`; they belong in a driver-specific interface in `jnosql-databases`.

### Semistructured: `DatabaseManager`, `DatabaseManagerFactory`, `DatabaseConfiguration`

- `DatabaseManager` is the operation contract: `insert`, `update`, `delete`, `select`, plus `count`, `exists`, `singleResult`, `query`, `prepare`, `selectCursor`, `defaultIdFieldName` (from `IdFieldNameSupplier`).
- Abstract methods are the minimum a driver MUST implement. `default` methods give a correct, portable behavior built on those minimums (e.g. `count` and `exists` derive from `select`; `update(UpdateQuery)` selects then updates in memory). A driver MAY override for efficiency but MUST keep the semantics.
- Optional capabilities are explicit: `deleteAndCount` is a `default` that throws `UnsupportedOperationException`, and TTL overloads document the same. Never add a silent no-op fallback for an unsupported capability.
- `DatabaseManagerFactory extends Function<String, DatabaseManager>, AutoCloseable` is a factory by database name; `apply(null)` throws `NullPointerException`, `close()` is safe even when nothing needs releasing.
- `DatabaseConfiguration extends Function<Settings, DatabaseManagerFactory>` is the SPI (`uses` in `module-info.java`) with the same lookup contract as `KeyValueConfiguration`.

### Query objects and conditions

- `SelectQuery`, `DeleteQuery`, `UpdateQuery` are **data**: name, columns, optional `CriteriaCondition`, sorts, skip/limit, update sets. They are built by fluent builders (`SelectQuery.select(...)`, `SelectQuery.builder()`, `DeleteQuery.delete(...)`) and executed by a `DatabaseManager`.
- `CriteriaCondition` is a final, immutable value (an `Element` plus a `Condition`). `and`, `or`, `negate` return new instances; `CriteriaCondition.readOnly(condition)` returns a copy on which `and`, `or`, and `negate` throw `IllegalStateException`.
- `Condition` (core) is the closed vocabulary of operators: `EQUALS`, `GREATER_THAN`, `GREATER_EQUALS_THAN`, `LESSER_THAN`, `LESSER_EQUALS_THAN`, `IN`, `LIKE`, `AND`, `OR`, `NOT`, `BETWEEN`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`, `IGNORE_CASE`.

### Reader/Writer conversion APIs

- `ValueReader extends Predicate<Class<?>>`: `test(type)` declares support, `read(type, value)` converts. `ValueReaderDecorator` (a composite) tries readers discovered through `ServiceDiscovery`.
- `ValueWriter<T, S> extends Predicate<Class<?>>`: converts a Java value to a database-friendly representation (`TemporalValueWriter` → `String`, `EnumValueWriter` → `name()`). `ValueWriterDecorator` is the composite.
- `TypeReferenceReader` handles generic targets (`List`, `Set`, `Map`, `Optional`, `Queue`, `Stream`, `NavigableSet`) through `TypeSupplier`/`TypeReference`.

### Provider and extension contracts

The extension points are exactly: `DatabaseConfiguration`, `KeyValueConfiguration`, `ValueReader`, `ValueWriter`, `TypeReferenceReader`. All are discovered via `ServiceLoader` and declared in `module-info.java` (`uses`/`provides`) and, for core, `META-INF/services`. Do not add a new extension point without a demonstrated second implementer.

---

## 4. Query architecture

### Pipeline

```
JDQL string / method name
   │ query module: antlr grammar + *Parser  (QueryErrorListener -> QueryException on syntax errors)
   ▼
query.SelectQuery / DeleteQuery / UpdateQuery  (QueryCondition, QueryValue<T>, ValueType, Where)
   │ semistructured: QueryParser + Conditions + Values (+ Params for bind variables)
   ▼
semistructured.SelectQuery / DeleteQuery / UpdateQuery  (CriteriaCondition, Element)
   │ DatabaseManager (driver)
   ▼
database-native query
```

- **Representation.** Parsed queries use `QueryCondition` (`name`, `Condition`, `QueryValue<?>`); `QueryValue<T>` is typed by `ValueType` (`NUMBER`, `STRING`, `PARAMETER`, `ARRAY`, `FUNCTION`, `ENUM`, `BOOLEAN`, `NULL`, `PATH`, `CONDITION`). Executable queries use `CriteriaCondition`/`Element`.
- **Construction.** Fluent step interfaces in `SelectQuery`/`DeleteQuery` (`SelectFrom`, `SelectNameCondition`, `SelectWhere`, …) only expose operations valid at that step, so invalid sequences do not compile. Keep it that way: add a step interface rather than a runtime flag.
- **Parsing.** `SelectProvider`/`DeleteProvider`/`UpdateProvider` and the method providers are enum singletons that cache parsed results by query (and entity). Syntax errors surface as `QueryException`; a missing entity is an `IllegalArgumentException`. A provider creates a fresh parser per cache miss, so parser instances hold no shared state.
- **Validation.** Validate at the boundary where the input enters: `Objects.requireNonNull` on builder inputs; `IllegalArgumentException` for structurally invalid values (`CriteriaCondition.in` requires an iterable, `between` requires an iterable of two elements); `IllegalStateException` for composing a read-only condition.
- **Conditions and operators.** `Conditions.getCondition` translates `QueryCondition` to `CriteriaCondition` with an exhaustive `switch` over `Condition` and **no `default` branch**. Adding a constant to `Condition` therefore breaks compilation until it is handled here. Preserve that property; do not add a `default` that swallows new operators.
- **Conversion boundaries.** Parameter values stay as `Params`/`ParamValue` until bound; literal values are converted in `Values.get`. Field-name rewriting goes through `CommunicationObserverParser.fireConditionField`, so mapping-layer name translation does not leak into the parser.
- **Supported vs. unsupported.** The neutral model defines the *maximum* vocabulary; it does not promise every database supports every operator or feature. A driver that cannot execute a query MUST throw `UnsupportedOperationException` (documented on `DatabaseManager.select`/`delete`) rather than ignore a condition or return partial results. Capabilities that are optional get a `default` method that throws (`deleteAndCount`), not a silent fallback.
- **Type safety.** Use `QueryValue<T>`, `TypeReference`, `Class<T>`-based `Value.get`, and `Sort<?>` instead of `Object` plumbing. The unavoidable `ConditionQueryValue.class.cast(...)` in `Conditions` is the pattern: one narrowing step guarded by the `Condition` it follows.

### Database independence

- The neutral model MUST NOT contain native syntax (no SQL, CQL, AQL, Cypher, Mongo operator names, index hints). Operators are `Condition` constants and values are `Value`s.
- `query` grammars follow the JDQL (Jakarta Data Query Language) subset, which is the implemented query language. Do not claim Jakarta Query support here (see `common.md` §1).
- A feature available in one database goes into the driver or a dedicated mapping module (graph, vector, time series), not into `DatabaseManager`. Add to the shared contract only when at least two database models need it and a portable default exists.
- Translation to native queries is the driver's job; Communication gives it a complete, immutable, inspectable query object (`columns()`, `condition()`, `sorts()`, `skip()`, `limit()`, `isCount()`).

---

## 5. Extensibility and SOLID in Communication

- **Separate construction from execution.** Query objects are built by builders/parsers and carry no execution logic. `getResult`/`delete(manager)` on builder interfaces are convenience entry points that only delegate to a `DatabaseManager`; do not add database interaction inside query classes or conditions.
- **Small contracts.** `ValueReader`/`ValueWriter` have one conversion method plus `test`. `IdFieldNameSupplier` is a one-method interface split from `DatabaseManager` for exactly that reason. Add a new narrow interface in preference to growing `DatabaseManager`.
- **Composition.** Readers/writers are composed (`ValueReaderDecorator`, `ValueWriterDecorator`, `TypeReferenceReaderDecorator`); `CriteriaCondition` composes conditions with `and`/`or`/`negate`. Prefer composing existing parts to subclassing.
- **Immutability.** `CriteriaCondition` (final, private constructors), `DefaultValue` (a record), `Condition` (enum), settings (`DefaultSettings`) are immutable. New value-like types SHOULD be too. Builders and `CommunicationEntity` are intentionally mutable; do not share them across threads.
- **Explicit unsupported behavior.** Throw `UnsupportedOperationException` with a message naming the missing capability (`ValueReaderDecorator` for unsupported target types, `DatabaseManager.deleteAndCount`, TTL). Never return `null`, an empty result, or ignore input to “be compatible”.
- **Strategy for independently varying behavior.** Readers, writers, and `CursorExecutor` (`OFF_SET`, `CURSOR_NEXT`, `CURSOR_PREVIOUS`) are strategies: each variant owns its algorithm. A new conversion = a new `ValueReader` registered via SPI, not a new `if` in `DefaultValue`.
- **Avoid large conditional chains, but keep closed switches.** The `switch` in `Conditions` is acceptable: it is exhaustive over a closed enum, performs one uniform mapping, and the compiler enforces completeness. Do not extend it with database-specific cases. If translation needs to vary per database, vary it in the driver, not with `if (database == …)` here.
- **No database-specific branches.** Any code that names a vendor or checks a driver class in this repository is a boundary violation.
- **Do not abstract speculatively.** `DefaultSelectQuery`, `DefaultDeleteQuery`, etc. are implementation details behind the interfaces; do not make them public API or add interfaces for them.

---

## 6. Conversion

### Responsibilities

- **`ValueReader`** converts a stored value to a requested Java type. **`ValueWriter`** converts a Java value to the representation sent to the database. **`Value`** is the facade (`get()`, `get(Class)`, `get(TypeSupplier)`, `isNull()`, `isInstanceOf`).
- Selection is by `test(Class)`; first matching provider wins (`ValueReaderDecorator`, `ValueWriterDecorator`). Make `test` precise (`Integer.class.equals(type) || int.class.equals(type)`) so providers do not shadow each other.

### Documented behaviors to preserve (do not strengthen or invent guarantees)

| Aspect | Current behavior (source) |
|---|---|
| Already-correct type | `ValueReaderDecorator.read` returns the value unchanged when `type.isInstance(value)`. |
| Unsupported target | `ValueReaderDecorator.read` throws `UnsupportedOperationException("The type … is not supported yet")`; `DefaultValue.get(TypeSupplier)` throws `UnsupportedOperationException` for unsupported generic types. |
| Null | `NullValue.get(Class)` and `get(TypeSupplier)` return `null`; `isNull()` is `true`. A `ValueReader` receives a non-null value from `Value`; a reader that is called with `null` directly is not guaranteed to be null-safe (e.g. `IntegerReader` calls `toString()`), so keep that in mind and test it when adding readers. |
| Numeric | `IntegerReader` accepts any `Number` (via `intValue()`, so narrowing may truncate) or parses `toString()` (`NumberFormatException` on bad text). `NumberReader` returns a `Number` as-is, otherwise parses to `Double`. Do not assume range checks exist. |
| Temporal | `LocalDateReader` accepts `LocalDate`, `Calendar`, `Date`, `Number` (epoch millis) interpreted in `ZoneId.systemDefault()`, or ISO text via `LocalDate.parse`. `DateReader` accepts `Date`, `Number`, or text via `new Date(String)`. `TemporalValueWriter` writes any `Temporal` as `toString()`. System-default time zone is part of the current behavior; do not change it silently. |
| Enum | `EnumReader` accepts an instance, an ordinal (`Number`) or a name; unknown ordinal/name throws `IllegalArgumentException`. `EnumValueWriter` writes `name()`. |
| UUID | `UUIDValueReader` returns a `UUID` as-is, parses a `CharSequence` in canonical format (malformed → `CommunicationException` with the offending value and cause), and returns `null` for other types. |
| Optional | `OptionalReader` wraps with `Optional.ofNullable`; `OptionalValueWriter` is the symmetric writer. |

### Symmetry and compatibility

- A writer/reader pair SHOULD round-trip (`write` then `read` returns an equal value). That is the intent for enum (`EnumValueWriter` writes `name()`, `EnumReader` reads it back). There is no UUID-specific writer: `ValueUtil` passes values without a matching writer through unchanged, and `UUIDValueReader` reads both a `UUID` and its string form. Round-trip is **not** claimed for lossy cases (narrowing numerics, `Temporal` → `String` for types the reader does not parse, system-zone-dependent dates). Where a pair is intended to round-trip, add a test; where it is not, document the loss in Javadoc.
- Changing what a writer emits changes stored data: treat it as a breaking change (see `common.md` §5.7, §8).

### Error handling

- Wrap failures that the caller can act on in `CommunicationException` with the offending value and cause (`UUIDValueReader`). Use `IllegalArgumentException` for invalid enum/value arguments as `EnumReader` does. Do not catch broad exceptions or return a default to hide a bad value.
- Be consistent *within* a family: do not mix `NumberFormatException` leaking from one reader with `CommunicationException` in a sibling without a documented reason; if you change a reader's exception type, treat it as a compatibility change.

### Adding a reader or writer

1. Implement `ValueReader`/`ValueWriter` as a small `final` class in `communication.reader`/`communication.writer`.
2. Register it in `module-info.java` (`provides`) and `src/main/resources/META-INF/services/…`.
3. Test: supported inputs, already-typed input, null (where reachable), malformed input, boundary values, and the round trip when intended.

---

## 7. Public API documentation

The Javadoc rules in `common.md` §6 apply. Communication specifics:

- **API vs. implementation.** `DatabaseManager`, `BucketManager`, `*Factory`, `*Configuration`, `Value`, `ValueReader`, `ValueWriter`, `Settings`, `Condition`, `CriteriaCondition`, `SelectQuery`/`DeleteQuery`/`UpdateQuery`, `CommunicationEntity`, `Element` are developer/driver-facing. `Default*` classes, `*Parser` internals, `CursorExecutor`, and generated ANTLR classes are not; keep them package-private where possible and do not document them as API.
- **Contract text matters more than examples of internals.** For every `DatabaseManager`/`BucketManager` method state: null handling, which `UnsupportedOperationException` conditions apply, whether the result is lazy (`Stream` MUST be closed or consumed by the caller if the driver says so), and what an absent result is (`Optional`, empty `Stream`).
- **Examples** for core entry points, kept short and compilable:

```java
// Build and run a query
SelectQuery query = SelectQuery.select("name", "age")
        .from("person")
        .where("age").gte(18)
        .and("name").like("A%")
        .orderBy("name").asc()
        .limit(10)
        .build();
Stream<CommunicationEntity> result = manager.select(query);

// Compose conditions
CriteriaCondition adult = CriteriaCondition.gte(Element.of("age", 18));
CriteriaCondition named = CriteriaCondition.eq(Element.of("name", "Ada"));
CriteriaCondition either = adult.or(named);

// Convert a stored value
UUID id = value.get(UUID.class);
List<String> tags = value.get(new TypeReference<List<String>>() {});
```

- Verify every example against the current builder signatures before committing; if unsure, use `{@code}` snippets that exist in the tests.
- Document in `@throws` the contract exceptions (`NullPointerException`, `IllegalArgumentException`, `UnsupportedOperationException`, `CommunicationException`) exactly as the code throws them.
- SPI interfaces (`ValueReader`, `ValueWriter`, `DatabaseConfiguration`) MUST document what implementers must guarantee (precise `test`, thread-safety expectation, exception type), since implementers are third parties.

---

## 8. Testing

Follow [`TESTING-GUIDELINE.adoc`](../TESTING-GUIDELINE.adoc) (naming, `@Nested`, `@DisplayName`, AssertJ, soft assertions) and the unit/integration split in `common.md` §7. Communication tests are plain JUnit tests with no container. Use these existing tests as templates.

| Area | What to test | Reference tests |
|---|---|---|
| Query construction | Every builder step and operator; combinations; skip/limit/sort; `null` names; immutability of the built object. | `DefaultSelectQueryBuilderTest`, `DefaultDeleteQueryBuilderTest`, `DefaultElementQueryBuilderTest`, `DefaultUpdateQueryTest` |
| Conditions | Each `Condition` constant; `and`/`or`/`negate` semantics, double negation, flattening, `readOnly` violation (`IllegalStateException`), invalid `in`/`between` input. | `CriteriaConditionTest`, `ConditionsTest`, `DefaultElementConditionTest`, `ConditionTest` (core) |
| Parsing | Valid and invalid JDQL/method names, parameters, entity absent, caching, every operator and `ValueType`. | `SelectJakartaDataQuery*Test`, `DeleteJakartaDataQuery*Test`, `UpdateJakartaDataQuery*Test`, `SelectMethodProviderTest`, `SelectMethodQueryProviderTest`, `DeleteByMethodQueryProviderTest`, and in `semistructured` `SelectQueryParserTest`, `DeleteQueryParserTest`, `UpdateQueryParserTest` |
| Converters | Supported types, already-typed input, text input, malformed input, boundary numbers/dates, enum by name/ordinal/unknown, UUID string/`UUID`/invalid, round trip where intended. | `UUIDValueReaderTest`, `IntegerReaderTest`, `LocalDateReaderTest`, `EnumReaderTest`, `TemporalValueWriterTest`, `ValueReaderDecoratorTest` |
| Invalid operations | `UnsupportedOperationException` for optional capabilities and unsupported types; `NullPointerException` for required arguments; the exact message when it is part of the contract. | `DatabaseManagerTest`, `DefaultValueTest`, `NullValueTest` |
| Null and boundary | `null` arguments, empty collections, single-element `in`, `between` with wrong arity, zero/negative skip/limit, empty `Settings`. | `SettingsBuilderTest`, `DefaultSettingsTest`, `ParamsTest`, `ElementsTest` |
| Contract behavior | The `default` methods of `DatabaseManager`/`BucketManager` using a minimal in-test implementation, so any driver inherits verified behavior. | `DatabaseMock`, `DatabaseManagerTest`, `MockKeyValueConfiguration`, `KeyValueConfigurationTest`, `DatabaseConfigurationTest` |

Additional rules:

- Test through the public contract; mock only the `DatabaseManager`/`BucketManager` boundary when verifying that builders delegate.
- A new `Condition` constant, `ValueType`, reader, or writer is not done until it has tests for the parser, the translation (`Conditions`), and the conversion path it touches.
- Run PIT (`TESTING-GUIDELINE.adoc` §4) on the changed condition/conversion classes and kill surviving mutants that represent real behavior.
- Each bug fix gets a regression test in the module where the defect lives.

---

## 9. Definition of Done (Communication)

- [ ] Change stays inside Communication's role: no entity/annotation/CDI/`jakarta.nosql` dependency, no driver code, no vendor syntax or `if (vendor)` branches.
- [ ] Module dependency direction preserved (`semistructured`/`key-value` → `query` → `core`); `module-info.java`, `META-INF/services` updated consistently for any new SPI implementation or package.
- [ ] Existing contracts unchanged: signatures, exception types, null behavior, `Condition` vocabulary, and written/stored representation of values; any intended change is deprecated/documented in `CHANGELOG.adoc`.
- [ ] Optional or unsupported behavior is explicit (`UnsupportedOperationException` with a clear message); nothing is silently ignored.
- [ ] Query objects remain immutable data; no execution logic added to them; `Conditions` switch stays exhaustive without `default`.
- [ ] Readers/writers are small, precise in `test`, registered, and round-trip (or document why not) with tests for null, malformed, and boundary inputs.
- [ ] Public/driver-facing types have accurate Javadoc (`@param`, `@return`, `@throws`, examples for key operations); internal types are not exposed.
- [ ] Tests follow `TESTING-GUIDELINE.adoc`; positive, negative, boundary, and contract cases covered; PIT checked for changed logic.
- [ ] `mvn clean verify` passes for the affected Communication modules (tests, Checkstyle, PMD, RAT license headers, Javadoc).
- [ ] Conventional Commit message and `git commit -s` (DCO); no automated tool as author or co-author.
