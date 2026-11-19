# Eclipse JNoSQL – Mapping Layer Development Guide

How to maintain and extend `jnosql-mapping`. Read [`common.md`](common.md) (shared rules) and [`communication.md`](communication.md) (the layer below) first; this guide does not repeat them. Annotation reference: [`MAPPING.adoc`](../MAPPING.adoc). Tests: [`TESTING-GUIDELINE.adoc`](../TESTING-GUIDELINE.adoc).

> `MAPPING.adoc` is user documentation and partly predates the code (it lists four mapping modules and describes some annotations as “beyond the spec”). When it disagrees with the source, the source and the Jakarta specifications win; `MappingType` imports `jakarta.nosql.Embeddable`, for example.

---

## 1. Mapping architecture

The Mapping layer turns **user objects into Communication objects and back**, and exposes them through Jakarta NoSQL (`Template`) and Jakarta Data (`@Repository`) programming models.

| Responsibility | Where |
|---|---|
| Jakarta NoSQL entity mapping (`@Entity`, `@Id`, `@Column`, `@Embeddable`, `@Convert`, …) | Read from classes by `jnosql-mapping-reflection` into the metadata API in `jnosql-mapping-api-core`. |
| Entity metadata | `EntityMetadata`, `EntitiesMetadata`, `FieldMetadata`, `ConstructorMetadata`, `InheritanceMetadata`, `ProjectionMetadata` (`mapping.metadata`). |
| Communication ↔ object conversion | `EntityConverter`, `DefaultEntityConverter`, `FieldConverter`, `ParameterConverter`, `ProjectorConverter` (`jnosql-mapping-semistructured`); `KeyValueEntityConverter` (`jnosql-mapping-key-value`). |
| CDI integration | CDI `Extension`s, `Bean`s, producers, qualifiers (`*Extension`, `TemplateBean`, `*ManagerSupplier`, `*TemplateProducer`, `AbstractBean`, `@Database`). |
| Jakarta Data repositories | Repository metadata (`RepositoryMetadata`, `RepositoryMethod`, `RepositoryMethodType`), invocation handlers (`CoreRepositoryInvocationHandler`), operations (`mapping.metadata.repository.spi.*`, `Semistructured*Operation`), return handling (`RepositoryReturn`, `DynamicReturn`). |
| Specialized templates | `KeyValueTemplate`, `DocumentTemplate`, `ColumnTemplate`, `GraphTemplate`, `TimeSeriesTemplate`, `VectorTemplate`. |
| Database-model extensions | The `*Extension` + `TemplateBean` + `Repository*Bean` + `*ManagerSupplier` set in each model module. |

### Boundary with Communication

- Mapping depends on Communication, never the reverse (`common.md` §1).
- Mapping speaks `CommunicationEntity`, `Element`, `SelectQuery`, `DeleteQuery`, `DatabaseManager`, `BucketManager`. It never builds native queries and never references a driver.
- Communication never sees user classes, annotations, `EntityMetadata`, or CDI. Anything that needs a user type or annotation belongs here; anything that needs only names and values belongs in Communication.
- Field-name translation between Java names and stored names happens in Mapping (`EntityMetadata.columnField`, `MapperObserver`, `RepositoryObserverParser` implementing `CommunicationObserverParser`), so the query parser stays unaware of entities.
- Known wrinkle, not a pattern to copy: `jnosql-mapping-graph` contains `org.eclipse.jnosql.communication.graph.GraphDatabaseManager` and `CommunicationEdge`, i.e. Communication-package types inside a Mapping module. New communication contracts go in a Communication module.

---

## 2. Module responsibilities

| Module | Purpose | Key abstractions |
|---|---|---|
| `jnosql-mapping-api-core` | Contracts only: metadata, repository metadata, database qualifier, events. No reflection implementation, no CDI wiring beyond qualifiers. | `EntityMetadata`, `EntitiesMetadata`, `FieldMetadata`, `MappingType`, `ClassScanner`, `ClassConverter`, `ConstructorBuilderSupplier`, `ProjectionMetadata`, `RepositoryMetadata`, `RepositoryMethodType`, `RepositoryInvocationContext` and the `repository.spi` operation interfaces (`FindByOperation`, `QueryOperation`, `SaveOperation`, …), `Database`, `DatabaseType`, `DatabaseQualifier`, `Databases`, `EntityPrePersist`, `EntityPostPersist`, `NoSQLRepository`, exceptions (`IdNotFoundException`, `MetadataException`, `DynamicQueryException`). |
| `jnosql-mapping-reflection` | The reflection implementation of the metadata API, class scanning, and the CDI extension that publishes metadata. | `ReflectionClassConverter`, `EntityMetadataBuilder`, `DefaultEntityMetadata`, `Default*FieldMetadata`, `EmbeddedFieldMetadata`, `AutoApplyConverters`, `ClassGraphClassScanner`/`ReflectionClassScanner`, `ReflectionEntityMetadataExtension`, `ReflectionLifecycleEventHandler`, `repository.ReflectionRepositoryMetadata`, `repository.RepositoryMethodTypeConverter`, `ReflectionConstructorBuilderSupplier`. |
| `jnosql-mapping-core` | Model-independent runtime: configuration, converters lookup, repository engine, return conversion. | `Converters`, `MappingConfigurations`, `MicroProfileSettings`, `AbstractBean` (`core.spi`), `AbstractRepositoryInvocationHandler`, `CoreRepositoryInvocationHandler`, `RepositoryReturn` + `returns.*`, `DynamicReturn`, `DynamicQueryMethodReturn`, `InfrastructureOperatorProvider`, `RepositoryOperationProvider`, `Core*Operation`, `SpecialParameters`, `NoSQLPage`. Legacy: `core.query.*` (`AbstractRepositoryProxy`, `RepositoryType` is `@Deprecated(forRemoval = true)`). |
| `jnosql-mapping-semistructured` | Everything shared by column, document, graph, time series, and vector: template, entity conversion, fluent mapper queries, repository operations. | `SemiStructuredTemplate`, `AbstractSemiStructuredTemplate`, `EntityConverter`, `EntityConverterFactory`, `FieldConverter`, `EventPersistManager`, `MapperSelect`/`MapperDelete`/`MapperUpdate`, `MappingQuery`, `repository.Semistructured*Operation`, `SemistructuredQueryBuilder`, `SemistructuredReturnType`, `SemistructuredRepositoryProducer`, `query.BaseRepositoryBean`/`RepositoryBean`/`CustomRepositoryBean`. The `query` package (other than the beans) is the **deprecated** previous engine (see its `package-info.java`); new work goes to `repository`. |
| `jnosql-mapping-key-value` | Key-value mapping on `BucketManager`. | `KeyValueTemplate`, `AbstractKeyValueTemplate`, `KeyValueEntityConverter`, `KeyValueEventPersistManager`, `KeyValueRepositoryProducer`, `BucketManagerSupplier`, `KeyValueDatabase`. |
| `jnosql-mapping-column` | Wide-column on `DatabaseManager`. | `ColumnTemplate`, `ColumnTemplateProducer`, `ColumnExtension`, `ColumnManagerSupplier`. |
| `jnosql-mapping-document` | Document on `DatabaseManager`. | `DocumentTemplate`, `DocumentTemplateProducer`, `DocumentExtension`, `DocumentManagerSupplier`. |
| `jnosql-mapping-graph` | Graph (vertices as entities, edges). | `GraphTemplate`, `AbstractGraphTemplate`, `Edge`, `EdgeBuilder`, `GraphExtension`, `GraphManagerSupplier`; `GraphDatabaseManager` (see wrinkle above). |
| `jnosql-mapping-timeseries` | Time series. | `TimeSeriesTemplate`, `TimeSeriesQuery`, `DefaultTimeSeriesTemplate`, `TimeSeriesExtension`, `TimeSeriesManagerSupplier`. |
| `jnosql-mapping-vector` | Vector search. | `VectorTemplate`, `VectorManager`, `VectorSearch`, `VectorSelectQuery`, `Vector`/`DenseVector`/`SparseVector`/`MultiVector`, `VectorExtension`, `VectorManagerSupplier`. |

Rules: model modules depend on `semistructured` (or `key-value`) and `core`; they MUST NOT depend on each other. `api-core` MUST NOT depend on `reflection` or `core`.

---

## 3. Entity metadata and conversion

### Pipeline

```
classpath ──ClassScanner──► entity / embeddable / projection / repository classes
          ──ClassConverter► EntityMetadata (cached in ReflectionEntityMetadataExtension → CDIGroupEntityMetadata → EntitiesMetadata)
entity ⇄ EntityConverter ⇄ CommunicationEntity (Elements)  ⇄ DatabaseManager
```

- `EntitiesMetadata.get(Class)` is the single lookup for metadata; do not re-parse annotations at runtime. Metadata is built once per CDI container (`ReflectionEntityMetadataExtension` keeps its maps per extension instance on purpose, per the source comment).
- **Identity.** `EntityMetadata.id()` returns the `@Id` field if any; operations that need an id throw `IdNotFoundException` (`AbstractSemiStructuredTemplate`, `IdNotFoundException.newInstance(type)`).
- **Mapping annotations.** Read only in `jnosql-mapping-reflection` (`FieldMappingBuilder`, `EntityMetadataBuilder`, `ReflectionClassConverter`). Do not read annotations in templates, converters, or repository code; add the information to the metadata API instead.
- **Field kinds.** `MappingType.of(Class)` classifies a type as `COLLECTION`, `MAP`, `ENTITY`, `EMBEDDED` (`@Embeddable` flat), `EMBEDDED_GROUP`, `ARRAY`, or `DEFAULT`. `FieldConverter` is an enum with one strategy per kind (`EMBEDDED`, `ENTITY`, `COLLECTION`, `DEFAULT`, …); a new kind means a new `MappingType` plus a new constant, not a branch in `EntityConverter`.
- **Embeddables.** Flat embeddables are merged into the owning entity's elements; grouped ones become a sub-structure (`FieldConverter`, `EmbeddedFieldMetadata`; tests in `EntityConverterTest`, `DefaultMapFieldMetadataEmbeddedTest`).
- **Inheritance.** `InheritanceMetadata` carries discriminator column and value; `EntityConverter` writes the discriminator and, when reading, selects the subtype from it. Missing or unknown discriminators throw `MappingException` with the column/value in the message (`EntityConverter.mapInheritanceEntity`/`inheritanceToEntity`). Queries on a parent add the inheritance condition (`DynamicSelectQueryBuilder.includeInheritance`/`applyInheritance`). Tests: `EntityConverterInheritanceTest`, `TemplateInheritanceTest`.
- **Constructors and records.** Entities may be built through constructor parameters (`ConstructorMetadata`, `ParameterConverter`, `ReflectionConstructorBuilderSupplier`); keep field and parameter handling symmetrical (`*FieldMetadata` vs `*ParameterMetaData`). Tests: `EntityConverterConstructorTest`, `EntityImmutableTest`.
- **Attribute conversion.** `@Convert` or an auto-applied converter (`AutoApplyConverters`, found by `ClassScanner.autoApplyConverters()`) is resolved by `Converters` (CDI bean if one exists, otherwise a no-arg instance, cached per converter class). `FieldConverter` calls `AttributeConverter.convertToEntityAttribute`, and the template side calls `convertToDatabaseColumn`. Tests: `EntityConverterAutoApplyTest`, `AutoApplyConvertersTest`.
- **UUID.** Mapping has **no UUID-specific code**. A `UUID` field is read through `Value.get(UUID.class)` from Communication (`UUIDValueReader`, which accepts a `UUID` or a canonical string and throws `CommunicationException` for a malformed string; see `communication.md` §6) or through an `AttributeConverter`. Do not add UUID handling to the mapping layer; fix it in the reader/writer or in the converter. Existing tests: `EntityConverterTest` (id stored as string), `EntityConverterAutoApplyTest` (id as `UUID`), `DefaultFieldMetadataTest` (UUID converters). Where a round trip matters, assert entity → `CommunicationEntity` → entity equality for both stored forms.
- **Reflection.** Access goes through `Reflections`, `FieldReader`/`FieldWriter`, `InstanceSupplier`; do not scatter `setAccessible` calls. The scanner (`ClassGraphClassScanner`) scans the whole classpath once; it is a startup cost, so do not add scans.
- **Lifecycle callbacks.** Two mechanisms exist and MUST NOT be confused:
  1. Jakarta NoSQL/JNoSQL template events: `EntityPrePersist` / `EntityPostPersist`, fired by `EventPersistManager` (semistructured) and `KeyValueEventPersistManager` (key-value) around template persistence (`AbstractSemiStructuredTemplate`).
  2. Jakarta Data lifecycle events: `PreInsertEvent`, `PostInsertEvent`, `PreUpdateEvent`, `PreUpsertEvent`, `PreDeleteEvent`, and their post counterparts, fired for **repository** operations by `ReflectionLifecycleEventHandler` (`LifecycleEventHandler`), typed per entity through `LifecycleEventTypeLiteral`.
  Template calls do not fire Jakarta Data events and repository built-ins use the lifecycle handler; check both paths when changing persistence. Tests: `EventPersistManagerTest`, `ReflectionLifecycleEventHandlerTest`, `core/.../events`.

---

## 4. CDI and configuration

- **Discovery.** Each model module ships `beans.xml` and registers one extension in `META-INF/services/jakarta.enterprise.inject.spi.Extension` (`ColumnExtension`, `DocumentExtension`, `GraphExtension`, `KeyValueExtension`, `TimeSeriesExtension`, `VectorExtension`, `ReflectionEntityMetadataExtension`). `jnosql-mapping-semistructured` and `-core` register no extension; they are plain beans discovered by `beans.xml`. The mapping modules have no `module-info.java`, so on the module path they are automatic modules; do not assume JPMS encapsulation here.
- **Producers.** `*ManagerSupplier` classes produce the communication manager with `@Produces @Database(DatabaseType.X)` and close it with a `@Disposes` method (`ColumnManagerSupplier`). The *container* owns the manager; templates and repositories only borrow it.
- **Qualifiers.** `@Database(type, provider)` selects among providers. Build instances with `DatabaseQualifier.ofColumn(provider)` etc.; a blank provider means the default. `Databases.addDatabase` registers only producers whose `@Database` type matches the extension's type. Review caution: `DatabaseQualifier.ofGraph(String)` returns `DEFAULT_KEY_VALUE_PROVIDER` for a blank provider (source line is a likely copy-paste slip) and `DatabaseQualifierTest` has no blank-provider case for graph. When touching qualifiers, add that test for every model and fix any mismatch in a separate commit; this is an example of why per-model duplication needs tests, not of intended behavior.
- **Extension flow.** `ProcessProducer` observers collect `@Database` producers → `AfterBeanDiscovery` adds a `TemplateBean`, `RepositoryBean`, and `CustomRepositoryBean` per (repository type × provider) (`ColumnExtension.onAfterBeanDiscovery`). The default (no-provider) beans are added unless an explicit default producer exists.
- **Beans.** Extend `AbstractBean<T>`; set `getTypes()`, `getQualifiers()`, a unique `getId()`, and create instances through `getInstance(...)`. Keep `create` free of logic beyond assembling collaborators.
- **Configuration.** `MicroProfileSettings.INSTANCE` is the `Settings` adapter over MicroProfile Config. Keys are the `MappingConfigurations` constants (`jnosql.<model>.provider`, `jnosql.<model>.database`, `jnosql.graph.transaction.automatic`). The provider key may name a `DatabaseConfiguration`/`KeyValueConfiguration` class; otherwise the first `ServiceLoader` provider is used. A missing database name raises `MappingException` naming the property. New settings: add a constant to `MappingConfigurations`, document it, and test absence and presence.
- **Java SE vs. Jakarta EE.** On Java SE, CDI is a container the application starts (the tests use Weld, `@EnableAutoWeld`); all discovery goes through `beans.xml` and the extension SPI. In a Jakarta EE/Quarkus-style runtime the container provides these services, and class scanning plus `ServiceLoader` may see a different class loader (`CHANGELOG.adoc` notes a move to a `ServiceDiscovery` mechanism for this). Do not use `CDI.current()` or static singletons for state that must be per application (follow `ReflectionEntityMetadataExtension`, which keeps its maps per extension instance).
- **Lifecycle and ownership.** Beans created by the extension are `@ApplicationScoped` managers; whoever produces a resource disposes it. Templates created by `*TemplateProducer` must not close the manager. Repositories are proxies created by `SemistructuredRepositoryProducer`/`KeyValueRepositoryProducer` with the `CreationalContext` of the bean.

---

## 5. Jakarta Data repositories

### Flow

```
@Repository interface
  ClassScanner.repositories*()           → ReflectionRepositoriesMetadata → RepositoryMetadata (+ RepositoryMethod per method)
  ColumnExtension etc. (per provider)    → RepositoryBean → SemistructuredRepositoryProducer.get(...)
  proxy: CoreRepositoryInvocationHandler (extends AbstractRepositoryInvocationHandler)
    invoke(method) → RepositoryMethodDescriptor (type from RepositoryMethod) → switch on RepositoryMethodType
        → operator (built-in/default/object/custom)  or  RepositoryOperationProvider.<operation>().execute(context)
        → SemistructuredReturnType / DynamicReturn → RepositoryReturn strategies
```

- **Discovery.** `ClassScanner` separates `repositoriesStandard()` (JNoSQL-handled `DataRepository` interfaces) from `customRepositories()`; unsupported repository kinds are filtered in `ClassGraphClassScanner`.
- **Metadata and method resolution.** `RepositoryMethodTypeConverter` (reflection module) decides the `RepositoryMethodType` of each method, in this order: default method → built-in framework repository method (`CrudRepository`, `BasicRepository`, `NoSQLRepository`) → `CursoredPage` return (cursor pagination) → operation annotations (`@Insert`, `@Save`, `@Delete`, `@Update`, `@Query`, `@Find`) → name-prefix patterns (`find…` → `FIND_BY`, `delete…`, `countAll`, `count…`, `exists…`, `findAll`). Resolution is done once, at metadata creation; the invocation handler caches a `RepositoryMethodDescriptor` per `Method`. A new method kind means a new `RepositoryMethodType`, a converter rule, an operation interface in `api-core` `repository.spi`, an implementation per engine, and a `case` in `dispatchRepositoryMethod`; keep these steps together in one change.
- **Derived queries.** `SemistructuredFindByOperation` / `SemistructuredQueryBuilder` use Communication's `SelectMethodProvider`/`DeleteMethodProvider` to parse the method name into a query, wrap field names with the entity observer, then bind arguments with `ParamsBinder`. Mapping adds inheritance conditions and special parameters; it does not parse names itself.
- **`@Query` (JDQL).** `SemistructuredQueryOperation` uses `QueryType` and `DynamicQueryMethodReturn`, validates the return type against the query type, and sends the text through `template.prepare(text, entity)`. This is the **Jakarta Data Query Language subset parsed by the in-repo parser**. Jakarta Query integration is not implemented (no `jakarta.query` dependency, `README.adoc`); do not write code or docs that assume it.
- **`@Find` / parameter-based.** `SemistructuredParameterBasedOperation` builds a query from annotated parameters via `SemiStructuredParameterBasedQuery`. An `UnsatisfiableQueryException` is translated into an *empty result of the declared return type* (`executeEmptyResult`), not an error; preserve that.
- **Parameter conversion.** `RepositoryMetadataUtils`, `ParamValue`, `ParamValueUtils`, `RestrictionConverter`, `ValueConverter`, `ParamsBinder` map method arguments (including `Restriction`, `@Is`, converters) to communication values. Conversion via `AttributeConverter` happens here for repository arguments, using `Converters`.
- **Return types.** One `RepositoryReturn` per shape (`List`, `Set`, `SortedSet`, `Queue`, `Stream`, `Optional`, `Page`, array, instance, `void`), registered in `META-INF/services/…RepositoryReturn` in `jnosql-mapping-core`. `DynamicReturn` selects by `isCompatible`. A new return type is a new class plus a service line, never a new branch in `DynamicReturn`.
- **Pagination and sorting.** `SpecialParameters` extracts `PageRequest`, `Limit`, `Sort`/`Order` from the arguments (`DynamicReturn.findSpecialParameters`/`findPageRequest`); `NoSQLPage` and `MappedCursoredPage` are the page implementations; cursor pagination is executed in `SemistructuredCursorPaginationOperation` over Communication's `selectCursor`. Total counts are supplied lazily (`totalSupplier`).
- **Lifecycle behavior.** Repository insert/update/save/delete go through `SemistructuredRepository` with `LifecycleEventHandler` (see §3).
- **Two engines.** `core.query.*` and `semistructured.query.*` (except the beans) are the deprecated old engine kept for compatibility; they receive fixes only. The key-value module has its own producer (`KeyValueRepositoryProducer`) over `CoreBaseRepositoryOperationProvider`. Do not add features to the deprecated engine, and do not add a third engine.

---

## 6. Specialized mapping models

Reusable architecture first: every semistructured model follows the same recipe. A model module contributes only what differs.

| Model | Template contract | What is model-specific |
|---|---|---|
| Key-Value | `KeyValueTemplate extends Template` (`put`, `get`, `deleteByKey(s)`, TTL) | Not semistructured: its own converter, event manager, queries (`KeyValueQuery`), and repository producer over `BucketManager`. |
| Document | `DocumentTemplate extends SemiStructuredTemplate` | Almost nothing beyond wiring (`DocumentExtension`, `DocumentManagerSupplier`, `DocumentTemplateProducer`); nested structures come from the shared converter. |
| Column | `ColumnTemplate extends SemiStructuredTemplate` | Wiring only, as Document. |
| Graph | `GraphTemplate extends SemiStructuredTemplate` | Edges (`Edge`, `EdgeBuilder`, `edge(...)`, `findEdgeById`, `deleteEdge`) on `GraphDatabaseManager`; `jnosql.graph.transaction.automatic`. |
| Time Series | `TimeSeriesTemplate extends SemiStructuredTemplate` | Fluent `select(Class)` → `TimeSeriesQuery` (`latest`, `earliest`, `since`, `until`, `last`, per `CHANGELOG.adoc`), `TimeSeriesManagerSupplier`. |
| Vector | `VectorTemplate extends SemiStructuredTemplate` | `search(Class)` → `VectorSearch`/`VectorSelectQuery` on `VectorManager`, vector value types (`DenseVector`, `SparseVector`, `MultiVector`). |

Extension points when adding or changing a model:

1. A communication contract (in Communication, not here).
2. `*ManagerSupplier` (producer + disposer) and a `MappingConfigurations` pair.
3. `*TemplateProducer` + `Default*Template` (`@Vetoed` inner implementations are used when the template is produced rather than injected, see `ColumnTemplateProducer`).
4. `*Extension`, `TemplateBean`, `Repository*Bean`, `CustomRepository*Bean`, registered in `META-INF/services`.
5. Model-specific API on the template interface, documented as a JNoSQL feature, not as Jakarta NoSQL (`common.md` §8).

Avoid copying a module to create another: the column, document, graph, time-series, and vector extension/bean classes are near-duplicates today. Prefer pushing shared logic into `jnosql-mapping-semistructured`/`AbstractBean`, and treat the qualifier slip in §4 as the cost of the duplication.

---

## 7. SOLID and semantic design in Mapping

Apply `common.md` §§2–4. Mapping-specific guidance, with the trade-offs.

- **Separate the four concerns.** *Metadata* (what an entity is: `EntityMetadata`, built in `reflection`) → *conversion* (`EntityConverter`/`FieldConverter`) → *query interpretation* (`SemistructuredQueryBuilder`, Communication parsers, `ParamsBinder`) → *execution* (`SemiStructuredTemplate`, `*Operation`). A change in one MUST NOT require reading annotations in another, nor making a template parse a query string.
- **Small responsibilities.** `RepositoryOperationProvider` returns one narrow operation interface per method kind (`FindByOperation`, `CountByOperation`, …) and `InfrastructureOperatorProvider` groups the infrastructure operators; prefer adding an operation to adding a flag to an existing one.
- **Strategy where behavior varies.** `RepositoryReturn` (return shapes), `FieldConverter` (field kinds), `RepositoryOperation` implementations (per engine), `ClassScanner`/`ClassConverter`/`ConstructorBuilderSupplier` (SPI replaceable implementations). Each is a real axis of variation with multiple implementations.
- **Conditional chains to watch.** The central `switch` in `AbstractRepositoryInvocationHandler.dispatchRepositoryMethod` over `RepositoryMethodType` and the name/annotation rules in `RepositoryMethodTypeConverter` are acceptable today because the set is closed and each branch only delegates to a polymorphic operator. They are *not* a place to add model-specific `if`s or business logic. If you find yourself testing `DatabaseType` inside a shared class, move the variation into the model module.
- **Composition over inheritance.** `AbstractSemiStructuredTemplate` and `AbstractGraphTemplate` are template-method base classes with many abstract accessors (`converter()`, `manager()`, `eventManager()`, `entities()`, `converters()`); each model template subclasses it only to supply collaborators. That is a pragmatic trade-off (it keeps CDI proxies and generated subclasses simple) but it is a large base class: do not add new responsibilities to it; add a collaborator and inject it, as `EventPersistManager` and `Converters` already are.
- **Duplicated model-specific logic.** Before copying code into a second model module, check whether it can live in `jnosql-mapping-semistructured` or `AbstractBean` (see §6). Duplicated qualifier factories and extensions are known debt, not a template.
- **Protected no-arg constructors.** Many `@ApplicationScoped` beans (`SemistructuredQueryBuilder`, `SemistructuredReturnType`, `*Operation`) declare a protected no-arg constructor that sets fields to `null` so CDI can proxy them. That is a CDI requirement, not a style to extend to non-bean classes; keep real logic out of that constructor.
- **Stable API contracts.** Interfaces in `api-core` (`EntityMetadata`, `RepositoryMethod`, the `spi` operations, `ClassScanner`) are implemented by other modules and possibly by third parties; changes follow `common.md` §8. Adding an abstract method to `EntityMetadata` breaks `DefaultEntityMetadata` and external implementers, so prefer a `default` method with a correct behavior (as `ClassScanner.autoApplyConverters()` does).
- **Don't present existing choices as ideal.** Legacy packages (`core.query`, `semistructured.query`), the graph-package placement, and near-duplicate model modules exist for history and compatibility; follow the current pattern only for consistency inside a module and prefer the new `repository` engine and `api-core` contracts for new code.

---

## 8. Public APIs and Javadoc

Rules from `common.md` §6 apply. Mapping specifics:

- **Developer-facing:** `Template` extensions (`DocumentTemplate`, `ColumnTemplate`, `KeyValueTemplate`, `GraphTemplate`, `TimeSeriesTemplate`, `VectorTemplate`, `SemiStructuredTemplate`), `Edge`, `EdgeBuilder`, `Vector` types, `TimeSeriesQuery`, `VectorSearch`, `@Database`, `DatabaseType`, `DatabaseQualifier`, `NoSQLRepository`, `EntityPrePersist`/`EntityPostPersist`, `MappingConfigurations`, `MapperSelect`/`MapperUpdate`/`MapperDelete` fluent contracts, and the SPI that providers implement (`EntityMetadata`, `ClassScanner`, `RepositoryReturn`, `ClassConverter`).
- **Not developer-facing despite being public:** `Default*`, `Abstract*Repository*`, `*Utils`, `DynamicReturn`, `Core*Operation`, `Semistructured*Operation`, `*Bean`, `*Extension`, `*Supplier`, `*Producer`. Public for CDI/wiring; keep their Javadoc short and do not add usage examples.
- For templates and fluent APIs, document: the entity requirements (annotations, `@Id`), `NullPointerException` for null arguments, what happens when the entity has no id (`IdNotFoundException`), event behavior, and which operations a provider may not support (propagated `UnsupportedOperationException`).
- Examples must compile against the real API. Use `{@code}` blocks like these, which match existing signatures:

```java
// KeyValueTemplate
User saved = template.put(user, Duration.ofMinutes(10));
Optional<User> found = template.get(saved.id(), User.class);

// A Jakarta NoSQL entity used with a semistructured template
Person person = template.insert(new Person("1", "Ada"));
Optional<Person> loaded = template.find(Person.class, "1");
```

- Verify each example by compiling it (or copying from a passing test) before committing; remove an example rather than keep one you have not checked.

---

## 9. Testing

[`TESTING-GUIDELINE.adoc`](../TESTING-GUIDELINE.adoc) governs naming, structure, and assertions; `common.md` §7 the general approach. Mapping tests use JUnit 5 + AssertJ + Weld JUnit (`@EnableAutoWeld`, `@AddPackages`) where CDI is involved, and Mockito for the Communication boundary.

| Area | Cover | Reference tests |
|---|---|---|
| Entity metadata | Every annotation and field kind, ids, embeddables, inheritance, records/constructors, converters, invalid entities, projections. | `reflection`: `DefaultEntityMetadataTest`, `DefaultEntitiesMetadataTest`, `ReflectionClassConverterTest`, `MappingTypeTest`, `DefaultFieldMetadataTest`, `EmbeddedFieldMetadataTest`, `ConstructorMetadataBuilderTest`, `ProjectionConverterTest` |
| Conversion | Entity ⇄ `CommunicationEntity` in both directions, nested/collection/map fields, inheritance discriminators (missing and unknown), `AttributeConverter` and auto-apply, null fields. | `EntityConverterTest`, `EntityConverterInheritanceTest`, `EntityConverterConstructorTest`, `EntityConverterAutoApplyTest`, `EntityConverterFactoryTest`, `ProjectorConverterTest` |
| UUID round trip | A `UUID` field and id stored as `UUID` and as string; malformed string; with and without a converter. | `EntityConverterTest`, `EntityConverterAutoApplyTest`, `DefaultFieldMetadataTest` (add a round-trip case when touching these paths) |
| Repository method resolution | Each naming pattern and annotation maps to the right `RepositoryMethodType`; default/built-in/object/custom methods; unknown. | `reflection/repository` tests, `RepositoryMethodDescriptorTest`, `RepositoryObserverParserTest` |
| Query operations | Each `Semistructured*Operation` with return types, special parameters, pagination, empty results on `UnsatisfiableQueryException`, inheritance conditions. | `Repository*Test` (`RepositoryFindByTest`, `RepositoryQueryTest`, `RepositoryCursorPaginationTest`, …), `Semistructured*OperationTest`, `SemistructuredQueryBuilderTest`, `DynamicSelectQueryBuilderTest`, `DynamicReturn*Test`, `core/.../returns` |
| CDI integration | Extension discovery, beans per provider/qualifier, producers/disposers, configuration keys and missing-property errors. | `SemistructuredRepositoryProducerWeldTest`, `EntityConverterFactoryTest`, per-module `*ExtensionTest`/`*SupplierTest` where present |
| Lifecycle callbacks | Pre/post for insert, update, upsert, delete on repository paths; `EntityPrePersist`/`EntityPostPersist` on template paths; null entity rejected. | `ReflectionLifecycleEventHandlerTest`, `EventPersistManagerTest`, `core/.../events` |
| Model-specific | Edge operations (graph), fluent time-series and vector queries, key-value TTL and key conversion. | tests in the respective module (`jnosql-mapping-graph`, `-timeseries`, `-vector`, `-key-value`) |
| Regression | One test per fixed bug in the module where it lived; name by behavior. | – |

Rules specific to Mapping:

- Test metadata with real annotated entities in the module's `entities` test packages, not mocks of `EntityMetadata`.
- Test conversion with an in-memory `CommunicationEntity`; do not need a database. Mock `DatabaseManager`/`BucketManager` only to verify delegation.
- Prefer one CDI-container test per wiring concern; keep behavior in plain unit tests (faster, clearer failures).
- When changing a shared class (`EntityConverter`, `FieldConverter`, `SemistructuredQueryBuilder`), run the tests of every model module that depends on it, not only `semistructured`.
- Run PIT on changed conversion, resolution, and return-handling classes (`TESTING-GUIDELINE.adoc` §4).

---

## 10. Definition of Done (Mapping)

- [ ] Boundaries: Mapping depends on Communication only; no driver code or native syntax; no entity/annotation/CDI concepts leaked down; model modules do not depend on each other.
- [ ] Annotations are read only in `jnosql-mapping-reflection` and exposed via the metadata API; no duplicated annotation parsing.
- [ ] Jakarta boundaries respected: Jakarta NoSQL/Data behavior follows the specifications; JNoSQL-specific features are labeled as such; no Jakarta Query assumptions.
- [ ] New behavior varies through an existing strategy/SPI (`RepositoryReturn`, `FieldConverter`, operation interface, `ClassScanner`), registered in `META-INF/services` or the extension; no model-specific branches in shared code; nothing added to the deprecated engine.
- [ ] Both lifecycle paths (template events and Jakarta Data lifecycle events) considered when persistence changes.
- [ ] CDI: beans are proxyable, scoped correctly, qualifiers built through `DatabaseQualifier`, resources disposed by their producer; behavior works under Weld SE.
- [ ] Compatibility: no unintended change to metadata contracts, exception types (`IdNotFoundException`, `MappingException` messages), stored shapes, or configuration keys; `CHANGELOG.adoc` updated; `MAPPING.adoc` updated if user-visible mapping behavior changed.
- [ ] Tests: metadata, conversion (including UUID round trip where touched), repository resolution, query operations, CDI wiring, lifecycle, and model-specific cases as applicable; regression test for fixes; dependent modules re-run.
- [ ] Javadoc on developer-facing APIs with verified examples; internal public classes not advertised.
- [ ] `mvn clean verify` passes for the affected modules (Checkstyle, PMD, RAT, Javadoc).
- [ ] Conventional Commit, `git commit -s`, no automated tool as author or co-author.
