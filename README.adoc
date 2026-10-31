= Eclipse JNoSQL
:toc: auto

Eclipse JNoSQL is an open-source Java framework for NoSQL persistence. It implements https://jakarta.ee/specifications/nosql/[Jakarta NoSQL] and https://jakarta.ee/specifications/data/[Jakarta Data], and its query architecture is aligned with https://github.com/jakartaee/query[Jakarta Query]. It provides object mapping, templates, repositories, and fluent query APIs for six NoSQL database models: Key-Value, Document, Wide-Column, Graph, Time Series, and Vector.

The framework integrates with Jakarta CDI and MicroProfile Config and runs on Java SE and compatible Jakarta EE environments. It offers common persistence abstractions and also gives access to the capabilities of each database.

NOTE: Jakarta NoSQL standardizes the mapping annotations and the common `Template` API. The specialized APIs for each database model, including Graph, Time Series and Vector, are Eclipse JNoSQL capabilities beyond the standard. Jakarta Query integration is an architectural direction and is not yet a complete, published feature (see <<Querying with Jakarta Query>>).

== Why Eclipse JNoSQL?

* *Less boilerplate:* annotation-based mapping and ready-to-use persistence operations.
* *Familiar concepts across databases:* the same mapping, `Template` and repository programming model for different NoSQL databases.
* *Jakarta EE standards:* Jakarta NoSQL, Jakarta Data, CDI, and MicroProfile Config.
* *A choice of access styles:* template-based programmatic access or declarative repositories.
* *Database-specific features:* specialized templates expose what is particular to each model and provider.
* *Polyglot persistence:* several databases and models can be used in one application, selected through CDI qualifiers.
* *Externalized configuration:* settings are supplied through MicroProfile Config.

A common API does not make databases interchangeable. Databases differ in query operations, indexing, consistency guarantees, transactions, and data models, so the features you rely on must be supported by the selected provider.

== Jakarta Standards and Architecture

Three complementary Jakarta specifications are involved. They complement each other rather than compete:

[cols="1,3"]
|===
|Specification|Responsibility

|https://jakarta.ee/specifications/nosql/[Jakarta NoSQL]
|Object mapping annotations and the common `Template` persistence API.

|https://jakarta.ee/specifications/data/[Jakarta Data]
|Repository-based persistence: `@Repository` interfaces, query methods, and declarative queries.

|https://github.com/jakartaee/query[Jakarta Query]
|A common object-oriented query language shared across Jakarta persistence specifications.
|===

The query constructs that can be used depend on the Eclipse JNoSQL version and on the database provider.

== Persistence with Jakarta NoSQL

=== Object Mapping

Jakarta NoSQL defines its own mapping annotations in the `jakarta.nosql` package, such as `@Entity`, `@Id`, `@Column`, `@Embeddable`, `@Convert`, `@MappedSuperclass`, `@Inheritance`, `@DiscriminatorColumn` and `@DiscriminatorValue`. Some names will look familiar to Jakarta Persistence developers, but they are not inherited from it. See the https://www.jnosql.org/docs/annotations.html[mapping annotations guide] for details.

The examples below use this entity:

[source,java]
----
@Entity
public class Product {

    @Id
    private String id;

    @Column
    private String name;

    @Column
    private BigDecimal price;

    public Product() {
    }

    public Product(String id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // accessors omitted
}
----

=== Template API

`Template` is the common Jakarta NoSQL persistence abstraction. CDI manages the injection of the configured persistence components:

[source,java]
----
@Inject
Template template;

Product keyboard = new Product("p-1", "Keyboard", new BigDecimal("49.90"));

template.insert(keyboard);
Optional<Product> found = template.find(Product.class, "p-1");
List<Product> products = template.select(Product.class)
        .where("name").eq("Keyboard")
        .result();
template.delete(Product.class, "p-1");
----

`Template` provides a common abstraction, but it does not erase the differences between database models and providers.

=== Specialized Templates

Eclipse JNoSQL provides a specialized template for each database model. `KeyValueTemplate` and `SemiStructuredTemplate` extend `Template`; all the other specializations extend `SemiStructuredTemplate`.

[cols="2,3,3"]
|===
|Template|Main capability|Availability

|`KeyValueTemplate`
|Key-oriented operations
|Released

|`DocumentTemplate`
|Document filtering and queries
|Released

|`ColumnTemplate`
|Wide-Column operations
|Released

|`GraphTemplate`
|Graph relationships and operations, such as edges
|Released

|`TimeSeriesTemplate`
|Time-oriented queries
|Released

|`VectorTemplate`
|Vector similarity operations
|Development branch only
|===

== Repositories with Jakarta Data

Jakarta Data repositories provide a declarative persistence interface. The repository identifier type must match the entity `@Id` type:

[source,java]
----
@Repository
public interface ProductRepository extends BasicRepository<Product, String> {

    List<Product> findByName(String name);
}

@Inject
ProductRepository repository;

repository.save(new Product("p-1", "Keyboard", new BigDecimal("49.90")));
Optional<Product> found = repository.findById("p-1");
List<Product> keyboards = repository.findByName("Keyboard");
----

Use a `Template` for direct programmatic persistence operations and a repository for a declarative interface. Neither replaces the other: repositories do not remove the need for specialized templates when you need model-specific operations.

== Querying with Jakarta Query

https://github.com/jakartaee/query[Jakarta Query] defines a common object-oriented query language for Jakarta persistence APIs, such as Jakarta Data repositories. It has two levels:

* *Jakarta Query Core:* common capabilities intended for broad compatibility across persistence technologies.
* *Jakarta Query Extended:* additional language capabilities for technologies that support them.

NoSQL databases do not share the same query capabilities, so a provider may support only part of the language, and Eclipse JNoSQL respects those limitations.

IMPORTANT: Jakarta Query integration is not a complete feature in a published release. The current implementation uses its own query parser, which supports the Jakarta Data Query Language (JDQL) subset, and it does not depend on the Jakarta Query API. This README therefore has no Jakarta Query examples. See the https://github.com/jakartaee/query[Jakarta Query project] and the link:CHANGELOG.adoc[changelog] for updates.

== Supported NoSQL Database Models

Each model has a mapping module and a specialized template. Whether a model can be used with a given database depends on the available provider integrations, which are maintained in the https://github.com/eclipse-jnosql/jnosql-databases[JNoSQL databases project].

[cols="2,4,3,2"]
|===
|Model|Description and typical use|Template|Guide

|Key-Value
|Values stored and retrieved by key. Typical use: caches, sessions, shopping carts.
|`KeyValueTemplate`
|https://www.jnosql.org/docs/key-value.html[Key-Value]

|Document
|Self-describing, JSON-like documents. Typical use: catalogs and content.
|`DocumentTemplate`
|https://www.jnosql.org/docs/document.html[Document]

|Wide-Column
|Rows with flexible sets of columns grouped in column families. Typical use: large-scale, write-heavy data.
|`ColumnTemplate`
|https://www.jnosql.org/docs/column.html[Wide-Column]

|Graph
|Vertices and edges. Typical use: relationships, recommendations. An Eclipse JNoSQL capability, not standardized by Jakarta NoSQL; Apache TinkerPop is one integration option.
|`GraphTemplate`
|https://www.jnosql.org/docs/graph.html[Graph]

|Time Series
|Time-ordered records. Typical use: metrics and sensor data. An Eclipse JNoSQL capability, not standardized by Jakarta NoSQL.
|`TimeSeriesTemplate`
|https://www.jnosql.org/docs/time-series.html[Time Series]

|Vector
|Vector similarity search over dense, sparse, and multi-vectors. Typical use: semantic search and recommendations. An Eclipse JNoSQL capability, available only on the development branch.
|`VectorTemplate`
|https://www.jnosql.org/docs/vector.html[Vector]
|===

The mapping modules are published under the `org.eclipse.jnosql.mapping` group, for example `jnosql-mapping-document`, `jnosql-mapping-column`, `jnosql-mapping-key-value`, `jnosql-mapping-graph` and `jnosql-mapping-timeseries`. `jnosql-mapping-vector` exists only on the development branch (`2.0.0-SNAPSHOT`).

=== Provider Selection and Configuration

Database settings are read through MicroProfile Config, using properties such as `jnosql.<model>.database` and `jnosql.<model>.provider`. The provider property is necessary when more than one driver is on the classpath. The property prefixes are `jnosql.keyvalue`, `jnosql.document`, `jnosql.column`, `jnosql.graph` and `jnosql.timeseries`.

To use several databases of the same model, qualify the injection point with `@Database`. The provider name must match a manager producer that carries the same qualifier (see <<Programmatic Configuration>>):

[source,java]
----
@Inject
@Database(value = DatabaseType.DOCUMENT, provider = "databaseA")
DocumentTemplate templateA;
----

Key-Value databases additionally support injecting `List`, `Set`, `Queue` and `Map` instances backed by a bucket with `@Inject @KeyValueDatabase("bucket")`.

=== Programmatic Configuration

MicroProfile Config is the conventional way to configure a database, and most applications need nothing else. Sometimes more control over the creation of the database manager is needed: custom connection settings, provider-specific initialization, settings assembled in code, or integration with application-managed connection infrastructure. For these cases, Eclipse JNoSQL exposes configuration and factory abstractions, so an application can create the database manager itself and expose it through CDI.

The three mechanisms solve different problems and can be combined: MicroProfile Config externalizes settings, CDI qualifiers select among configured integrations, and programmatic configuration customizes how a manager is built. The example below is for the Key-Value model; other models use their own manager types, providers and settings (for example `DatabaseManager` for the semi-structured models).

[source,java]
----
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.interceptor.Interceptor;
import java.util.function.Supplier;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.communication.keyvalue.BucketManagerFactory;
import org.eclipse.jnosql.communication.keyvalue.KeyValueConfiguration;

@Alternative
@Priority(Interceptor.Priority.APPLICATION)
@ApplicationScoped
public class CustomBucketManagerProducer implements Supplier<BucketManager> {

    @Override
    @Produces
    @ApplicationScoped
    public BucketManager get() {
        // illustrative: keys and values are specific to each provider
        Settings settings = Settings.builder()
                .put("<provider-setting-key>", "<value from your secure configuration>")
                .build();

        // the first KeyValueConfiguration found on the classpath; use
        // getConfiguration(ProviderConfiguration.class) to pick a specific provider
        KeyValueConfiguration configuration = KeyValueConfiguration.getConfiguration();
        BucketManagerFactory factory = configuration.apply(settings);
        return factory.apply("database");
    }

    void close(@Disposes BucketManager manager) {
        manager.close();
    }
}
----

This example is illustrative rather than executable, because the settings keys and the provider depend on the database integration on the classpath. `Supplier<BucketManager>` follows the convention of JNoSQL's default producers; CDI only requires the `@Produces` method. Do not hard-code credentials; read them from a secure source.

* `@Produces` exposes the returned `BucketManager` as a CDI bean.
* `@Alternative` marks the bean as an alternative, which is not used unless it is enabled.
* `@Priority` enables the alternative for the whole application. Eclipse JNoSQL also provides a default, unqualified `BucketManager` producer that reads MicroProfile Config; an enabled alternative takes precedence over it, which avoids ambiguous resolution. A producer without the alternative annotations would be ambiguous with the default one, unless it uses a qualifier such as `@Database`.
* `@ApplicationScoped` defines the lifecycle of the bean and of the manager.

The disposer pairs with the producer declared in the same bean class and releases the manager when CDI destroys it. It does not conflict with the framework: Eclipse JNoSQL's own disposer belongs to its default producer class, which is replaced when the alternative is enabled. Because the custom producer owns the manager, it is also responsible for closing it. The producer and the produced bean's lifecycle are separate concerns: `@ApplicationScoped` on the class scopes the producer bean, while `@ApplicationScoped` on the method scopes the produced manager.

When the application starts, it creates the settings, the provider creates the manager factory, and the factory creates the manager. CDI then makes that manager available to the Key-Value persistence integration, such as `KeyValueTemplate` and the `@KeyValueDatabase` collections. The Key-Value default producer is unqualified, so the custom producer is unqualified as well.

The Wide-Column example follows the same mechanism with a different manager type and a model-specific qualifier. It is illustrative for the same reasons:

[source,java]
----
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.interceptor.Interceptor;
import java.util.function.Supplier;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

@Alternative
@Priority(Interceptor.Priority.APPLICATION)
@ApplicationScoped
public class CustomColumnManagerProducer implements Supplier<DatabaseManager> {

    @Override
    @Produces
    @Database(DatabaseType.COLUMN)
    @ApplicationScoped
    public DatabaseManager get() {
        Settings settings = Settings.builder()
                .put("<provider-setting-key>", "<value from your secure configuration>")
                .build();

        DatabaseConfiguration configuration = DatabaseConfiguration.getConfiguration();
        DatabaseManagerFactory factory = configuration.apply(settings);
        return factory.apply("database");
    }

    void close(@Disposes @Database(DatabaseType.COLUMN) DatabaseManager manager) {
        manager.close();
    }
}
----

`@Database(DatabaseType.COLUMN)` is the CDI qualifier that identifies the Wide-Column manager. Eclipse JNoSQL's default Wide-Column producer carries the same qualifier, and the Wide-Column integration looks up a `DatabaseManager` with it, so the custom producer must declare the same qualifier, and the disposer must match the producer's type and qualifiers. Changing only the qualifier does not switch database models: each model needs its own manager type, configuration API, provider, and qualifier.

[cols="1,1,1"]
|===
| |Key-Value |Wide-Column

|Manager
|`BucketManager`
|`DatabaseManager`

|Configuration
|`KeyValueConfiguration`
|`DatabaseConfiguration`

|Factory
|`BucketManagerFactory`
|`DatabaseManagerFactory`

|CDI selection
|Unqualified (default) producer
|`@Database(DatabaseType.COLUMN)`
|===

The two examples apply the same extension mechanism to different models; neither supersedes the other. Both customize the communication-level manager only: the Jakarta NoSQL `Template` and the Jakarta Data repositories are used exactly as before. To keep several managers of one model, add a provider name, as in `@Database(value = DatabaseType.COLUMN, provider = "databaseA")`, and inject the template with the same qualifier.

==== Database Model Qualifiers

`@Database` is a CDI qualifier (`org.eclipse.jnosql.mapping.Database`) that JNoSQL uses to distinguish database integrations. Its `value` is a `DatabaseType` constant and its optional `provider` names a specific configured integration. The qualifier names the model, while the manager interface depends on the model:

[cols="1,2,2"]
|===
|Database Model |Qualifier |Default manager producer

|Key-Value
|`@Database(DatabaseType.KEY_VALUE)` on templates and repositories
|`BucketManager`, unqualified

|Document
|`@Database(DatabaseType.DOCUMENT)`
|`DatabaseManager`, qualified

|Wide-Column
|`@Database(DatabaseType.COLUMN)`
|`DatabaseManager`, qualified

|Graph
|`@Database(DatabaseType.GRAPH)` on templates and repositories
|`GraphDatabaseManager`, unqualified

|Time Series
|`@Database(DatabaseType.TIME_SERIES)`
|`DatabaseManager`, qualified

|Vector
|`@Database(DatabaseType.VECTOR)` (main branch only)
|`VectorManager`, qualified (`@Default` as well)
|===

The qualifier selects the CDI bean; the manager interface is a separate concern. Key-Value and Graph manager producers are unqualified in this repository, so a custom producer for them is unqualified too, as in the Key-Value example above.

The qualifier on an injection point must match the qualifier of the produced bean, so the same type can be injected once per model:

[source,java]
----
@Inject
@Database(DatabaseType.COLUMN)
DatabaseManager columnManager;

@Inject
@Database(DatabaseType.DOCUMENT)
DatabaseManager documentManager;
----

Producer and disposer must carry identical qualifiers, otherwise CDI will not pair them and the manager will not be closed:

[source,java]
----
@Produces
@Database(DatabaseType.COLUMN)
public DatabaseManager get() { /* programmatic configuration */ }

public void close(@Disposes @Database(DatabaseType.COLUMN) DatabaseManager manager) {
    manager.close();
}
----

A Document producer and disposer use `@Database(DatabaseType.DOCUMENT)` in both places. When adapting an example to another model, change together: the manager type, the configuration and factory APIs, the provider implementation, the producer qualifier, the disposer qualifier and any `provider` name, and keep them consistent with the injection points. Changing only the `@Database` value is not enough.

See the https://www.jnosql.org/docs/key-value.html[Key-Value] and https://www.jnosql.org/docs/column.html[Wide-Column] guides and the https://github.com/eclipse-jnosql/jnosql-databases[database integrations] for provider-specific settings.

== Requirements and Runtime Integration

The latest stable release line (1.x.x) requires:

* Java 17 or higher
* https://jakarta.ee/specifications/cdi/4.1/[Jakarta CDI 4.1]
* https://jakarta.ee/specifications/jsonb/3.0/[Jakarta JSON Binding 3.0]
* https://jakarta.ee/specifications/jsonp/2.1/[Jakarta JSON Processing 2.1]
* https://microprofile.io/microprofile-config/[MicroProfile Config 3.1]

The development branch (`2.0.0-SNAPSHOT`) and the 1.2.0 milestones require Java 21. See the https://www.jnosql.org/docs/minimum.html[minimum requirements guide] for details.

* *Java SE:* the application provides a compatible CDI implementation and bootstraps the CDI container explicitly.
* *Jakarta EE:* a compatible runtime manages CDI initialization and the application components.

The supporting services, the JNoSQL modules, and the database drivers needed depend on the selected runtime and provider.

== Documentation and Examples

* https://www.jnosql.org/[Official website] and the https://www.jnosql.org/docs/introduction.html[introduction guide]
* https://www.jnosql.org/docs/minimum.html[Minimum requirements]
* https://www.jnosql.org/docs/annotations.html[Mapping annotations]
* https://github.com/eclipse-jnosql/jnosql-databases[Database integrations]
* https://github.com/JNOSQL/demos-se[Java SE demos]
* https://jakarta.ee/specifications/nosql/[Jakarta NoSQL], https://jakarta.ee/specifications/data/[Jakarta Data] and https://github.com/jakartaee/query[Jakarta Query] specifications
* link:MAPPING.adoc[Mapping API] and the communication layer notes in this repository
* link:MIGRATION.adoc[Migration guide], for upgrading from 1.0.0-b6 (Jakarta EE 9 package names and the reduced scope of Jakarta NoSQL)
* link:CHANGELOG.adoc[Changelog]

== Building from Source

You do not need to build from source to use the project. To build it, use Maven and Java 21 or higher (the compiler source and target are 21). This is separate from the minimum Java runtime of the published 1.1.x artifacts.

[source,bash]
----
mvn clean install
----

This compiles all modules and runs their unit tests. The database drivers and their integration tests live in the https://github.com/eclipse-jnosql/jnosql-databases[JNoSQL databases project].

== Contributing and Community

* https://github.com/eclipse-jnosql/jnosql/issues[Open an issue] to report bugs or recommend improvements.
* Open a pull request after reading the link:CONTRIBUTING.adoc[contributing guide], which covers the development process and the Eclipse Contributor Agreement.
* Eclipse JNoSQL's testing practices are described in the link:TESTING-GUIDELINE.adoc[testing guideline].
* Ask questions and share ideas in the https://github.com/eclipse-jnosql/jnosql/discussions[GitHub discussions].
* Issues about the https://github.com/eclipse-jnosql/jnosql-databases[databases project] or the https://github.com/eclipse-jnosql/jnosql-extensions[extensions project] belong in those repositories.
* Report security vulnerabilities as described in link:SECURITY.adoc[SECURITY].
* The project is an https://projects.eclipse.org/projects/technology.jnosql[Eclipse Foundation project].
* This project is governed by the Eclipse Foundation Code of Conduct (link:CODE_OF_CONDUCT.md[CODE_OF_CONDUCT]). Please report unacceptable behavior to mailto:codeofconduct@eclipse.org[codeofconduct@eclipse.org].

Project badges:
[%autowidth,cols="a,a,a,a", frame=none, grid=none, role=stretch ]
|===
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=sqale_rating[ link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent, window=_blank, target=_blank]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=code_smells[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=ncloc[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=coverage[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=sqale_index[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=alert_status[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=reliability_rating[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=duplicated_lines_density[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=vulnerabilities[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=bugs[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
| image::https://sonarcloud.io/api/project_badges/measure?project=org.eclipse.jnosql%3Ajakarta-nosql-parent&metric=security_rating[window=_blank, link=https://sonarcloud.io/summary/new_code?id=org.eclipse.jnosql%3Ajakarta-nosql-parent]
|===

== Compatibility and Versioning

Eclipse JNoSQL uses https://semver.org/[Semantic Versioning] (`MAJOR.MINOR.PATCH`) combined with Jakarta EE generations:

* *MAJOR* reflects the Jakarta EE generation of the release line. A new generation requires a new major version and may require a newer Java version.
* *MINOR* adds backward-compatible features within the same Jakarta EE generation.
* *PATCH* delivers backward-compatible fixes and maintenance.

[cols="1,2,2,3"]
|===
|Release line|Jakarta EE|Status|Notes

|`1.x.x`
|11
|Stable (latest: 1.2.0)
|Java 17 or higher.

|`2.x.x`
|12
|Planned (development branch is `2.0.0-SNAPSHOT`)
|Compatibility baseline for the next Jakarta EE generation; not a published-release guarantee.
|===

Upgrading within a major version keeps the same Jakarta EE generation. Upgrading between major versions requires managing the Jakarta EE platform transition, checking the required Java version, and handling incompatible API changes.

== License

Eclipse JNoSQL is dual-licensed under the https://www.eclipse.org/legal/epl-2.0[Eclipse Public License 2.0] and the https://www.apache.org/licenses/LICENSE-2.0[Apache License 2.0]. See the link:LICENSE[LICENSE] and link:NOTICE.adoc[NOTICE] files.
