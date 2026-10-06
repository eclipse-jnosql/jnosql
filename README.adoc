= Eclipse JNoSQL
:toc: auto

== Introduction

Eclipse JNoSQL is a compatible implementation of the https://jakarta.ee/specifications/nosql/[Jakarta NoSQL] and https://jakarta.ee/specifications/data/[Jakarta Data] specifications, a Java framework that streamlines the integration of Java applications with NoSQL databases.

== Goals

* Increase productivity performing common NoSQL operations
* Rich Object Mapping integrated with Contexts and Dependency Injection (CDI)
* Java-based Query and Fluent-API
* Persistence lifecycle events
* Low-level mapping using Standard NoSQL APIs
* Specific template API to each NoSQL category
* Annotation-oriented using JPA-like naming when it makes sense
* Extensible to explore the particular behavior of a NoSQL database
* Explore the popularity of Apache TinkerPop in Graph API
* Jakarta NoSQL and Data implementations

== Why Eclipse JNoSQL?

Eclipse JNoSQL is a Java framework that unifies NoSQL access across different database types, including Key-Value, Column Family, Document, Graph, and Time Series. Built on top of the Jakarta NoSQL and Jakarta Data specifications, it simplifies development through standard annotations, fluent APIs, and full compatibility with Jakarta EE and MicroProfile runtimes.

Use Eclipse JNoSQL to:

* Increase productivity with annotation-based object mapping
* Write expressive queries using a fluent or method-name DSL
* Reduce boilerplate through CDI-managed components and lifecycle events
* Switch NoSQL vendors with minimal code changes
* Align with modern practices like Domain-Driven Design (DDD)
* Rely on a standards-based solution compatible with Jakarta EE and MicroProfile servers

== One Mapping API to Multiples NoSQL Databases

Eclipse JNoSQL provides one API for each NoSQL database type. However, it incorporates the same annotations from the https://jakarta.ee/specifications/persistence/[Jakarta Persistence] specification and inherits from the Java Persistence Java Persistence API (JPA) to map Java objects. Therefore, with just these annotations that look like JPA, there is support for more than twenty NoSQL databases.

[source,java]
----
@Entity
public class Car {

    @Id
    private Long id;
    @Column
    private String name;
    @Column
    private CarType type;
 //...
}
----

These annotations from the Mapping API will look familiar to the Jakarta Persistence/JPA developer:

[cols="Annotation description"]
|===
|Annotation|Description

|`@jakarta.nosql.Entity`
|Specifies that the class is an entity. This annotation is applied to the entity class.

|`@jakarta.nosql.Id`
|Specifies the primary key of an entity.

|`@jakarta.nosql.Column`
|Specify the mapped column for a persistent property or field.

|`@jakarta.nosql.Embeddable`
|Specifies a class whose instances are stored as an intrinsic part of an owning entity and share the entity's identity.

|`@jakarta.nosql.Convert`
|Specifies the conversion of a Basic field or property.

|`@org.eclipse.jnosql.mapping.MappedSuperclass`
|Designates a class whose mapping information is applied to the entities that inherit from it. A mapped superclass has no separate table defined for it.

|`@jakarta.nosql.Inheritance`
|Specifies the inheritance strategy to be used for an entity class hierarchy.

|`@jakarta.nosql.DiscriminatorColumn`
|Specifies the discriminator column for the mapping strategy.

|`@jakarta.nosql.DiscriminatorValue`
|Specifies the value of the discriminator column for entities of the given type.

|===

IMPORTANT: Although similar to JPA, Jakarta NoSQL defines persistable fields with either the ```@Id``` or ```@Column``` annotation.

After mapping an entity, you can explore the advantage of using a ```Template``` interface, which can increase productivity on NoSQL operations.

[source,java]
----
@Inject
Template template;
...

Car ferrari = Car.id(1L)
        .name("Ferrari")
        .type(CarType.SPORT);

template.insert(ferrari);
Optional<Car> car = template.find(Car.class, 1L);
template.delete(Car.class, 1L);

List<Car> cars = template.select(Car.class).where("name").eq("Ferrari").result();
template.delete(Car.class).execute();
----

This template has specialization to take advantage of a particular NoSQL database type.

A ``Repository`` interface is also provided for exploring the Domain-Driven Design (DDD) pattern for a higher abstraction.

[source,java]
----
public interface CarRepository extends PageableRepository<Car, String> {

    Optional<Car> findByName(String name);

}

@Inject
CarRepository repository;
...

Car ferrari = Car.id(1L)
        .name("Ferrari")
        .type(CarType.SPORT);

repository.save(ferrari);
Optional<Car> idResult = repository.findById(1L);
Optional<Car> nameResult = repository.findByName("Ferrari");
----

== Getting Started

Eclipse JNoSQL requires these minimum requirements:

* Java 17 (or higher)
* https://jakarta.ee/specifications/cdi/3.0/[Jakarta Contexts & Dependency Injection 3.0] (CDI)
* https://jakarta.ee/specifications/jsonb/2.0/[Jakarta JSON Binding 2.0] (JSON-B)
* https://jakarta.ee/specifications/jsonp/2.2/[Jakarta JSON Processing 2.0] (JSON-P)
* https://microprofile.io/microprofile-config/[MicroProfile Config]

=== NoSQL Database Types

Eclipse JNoSQL provides common annotations and interfaces. Thus, the same annotations and interfaces, ```Template``` and ```Repository```, will work across the supported NoSQL database types.

As a reference implementation for Jakarta NoSQL, Eclipse JNoSQL provides particular behavior for each database type required by the specification and also supports Graph and Time Series databases:

* Key-Value
* Column Family
* Document
* Graph
* Time Series

=== Key-Value

Jakarta NoSQL provides a Key-Value template to explore the specific behavior of this NoSQL type.

Eclipse JNoSQL offers a mapping implementation for Key-Value NoSQL types:

[source,xml]
----
<dependency>
    <groupId>org.eclipse.jnosql.mapping</groupId>
    <artifactId>jnosql-mapping-key-value</artifactId>
    <version>1.1.19</version>
</dependency>
----

Furthermore, check for a Key-Value databases. You can find some implementations in the https://github.com/eclipse/jnosql-databases[JNoSQL Databases].

[source,java]
----
@Inject
KeyValueTemplate template;
...

Car ferrari = Car.id(1L).name("ferrari").city("Rome").type(CarType.SPORT);

template.put(ferrari);
Optional<Car> car = template.get(1L, Car.class);
template.delete(1L);
----

Key-Value is database agnostic. Thus, you can change the database in your application with no or minimal impact on source code.

You can define the database settings using the https://microprofile.io/microprofile-config/[MicroProfile Config] specification, so you can add properties and overwrite it in the environment following the https://12factor.net/config[Twelve-Factor App].

[source,properties]
----
jnosql.keyvalue.database=<DATABASE>
jnosql.keyvalue.provider=<CLASS-DRIVER>
jnosql.provider.host=<HOST>
jnosql.provider.user=<USER>
jnosql.provider.password=<PASSWORD>
----

TIP: The ```jnosql.keyvalue.provider``` property is necessary when you have more than one driver in the classpath. Otherwise, it will take the first one.

These configuration settings are the default behavior. Nevertheless, there is an option to programmatically configure these settings. Create a class that implements the ```Supplier<BucketManager>``` interface and then define it using the ```@Alternative``` and ```@Priority``` annotations.

[source,java]
----
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
@ApplicationScoped
public class ManagerSupplier implements Supplier<BucketManager> {

    @Produces
    public BucketManager get() {
        Settings settings = Settings.builder()
                .put("credential", "value")
                .build();
        KeyValueConfiguration configuration = new NoSQLKeyValueProvider();
        BucketManagerFactory factory = configuration.apply(settings);
        return factory.apply("database");
    }
}
----

You can work with several Key-Value database instances through the CDI qualifier. To identify each database instance, make a ```BucketManager``` visible for CDI by adding the ```@Produces``` and the ```@Database``` annotations in the method.

[source,java]
----
@Inject
@Database(value = DatabaseType.KEY_VALUE, provider = "databaseA")
private KeyValueTemplate templateA;

@Inject
@Database(value = DatabaseType.KEY_VALUE, provider = "databaseB")
private KeyValueTemplate templateB;

// producers methods
@Produces
@Database(value = DatabaseType.KEY_VALUE, provider = "databaseA")
public BucketManager getManagerA() {
    BucketManager manager = // instance;
    return manager;
}

@Produces
@Database(value = DatabaseType.KEY_VALUE, provider = "databaseB")
public BucketManager getManagerB() {
    BucketManager manager = // instance;
    return manager;
}
----


The KeyValue Database module provides a simple way to integrate the `KeyValueDatabase` annotation with CDI, allowing you to inject collections managed by the key-value database. This annotation works seamlessly with various collections, such as List, Set, Queue, and Map.

To inject collections managed by the key-value database, use the `@KeyValueDatabase` annotation in combination with CDI's `@Inject` annotation. Here's how you can use it:

[source,java]
----
import javax.inject.Inject;

// Inject a List<String> instance from the "names" bucket in the key-value database.
@Inject
@KeyValueDatabase("names")
private List<String> names;

// Inject a Set<String> instance from the "fruits" bucket in the key-value database.
@Inject
@KeyValueDatabase("fruits")
private Set<String> fruits;

// Inject a Queue<String> instance from the "orders" bucket in the key-value database.
@Inject
@KeyValueDatabase("orders")
private Queue<String> orders;

// Inject a Map<String, String> instance from the "orders" bucket in the key-value database.
@Inject
@KeyValueDatabase("orders")
private Map<String, String> map;
----

=== Column Family

Jakarta NoSQL provides a Column Family template to explore the specific behavior of this NoSQL type.

Eclipse JNoSQL offers a mapping implementation for Column NoSQL types:
[source,xml]
----
<dependency>
    <groupId>org.eclipse.jnosql.mapping</groupId>
    <artifactId>jnosql-mapping-column</artifactId>
    <version>1.1.19</version>
</dependency>
----

Furthermore, check for a Column Family databases. You can find some implementations in the https://github.com/eclipse/jnosql-databases[JNoSQL Databases].

[source,java]
----
@Inject
ColumnTemplate template;
...

Car ferrari = Car.id(1L)
        .name("ferrari").city("Rome")
        .type(CarType.SPORT);

template.insert(ferrari);
Optional<Car> car = template.find(Car.class, 1L);

template.delete(Car.class).where("id").eq(1L).execute();

Optional<Car> result = template.singleResult("FROM Car WHERE _id = 1");
----

Column Family is database agnostic. Thus, you can change the database in your application with no or minimal impact on source code.

You can define the database settings using the https://microprofile.io/microprofile-config/[MicroProfile Config] specification, so you can add properties and overwrite it in the environment following the https://12factor.net/config[Twelve-Factor App].

[source,properties]
----
jnosql.column.database=<DATABASE>
jnosql.column.provider=<CLASS-DRIVER>
jnosql.provider.host=<HOST>
jnosql.provider.user=<USER>
jnosql.provider.password=<PASSWORD>
----

TIP: The ```jnosql.column.provider``` property is necessary when you have more than one driver in the classpath. Otherwise, it will take the first one.

These configuration settings are the default behavior. Nevertheless, there is an option to programmatically configure these settings. Create a class that implements the ```Supplier<ColumnManager>``` interface, then define it using the ```@Alternative``` and ```@Priority``` annotations.

[source,java]
----
@Alternative
@Priority(Interceptor.Priority.APPLICrATION)
@ApplicationScoped
public class ManagerSupplier implements Supplier<DatabaseManager> {

    @Produces
    @Database(DatabaseType.COLUMN)
    public DatabaseManager get() {
        Settings settings = Settings.builder()
                .put("credential", "value")
                .build();
        DatabaseConfiguration configuration = new NoSQLColumnProvider();
        DatabaseManagerFactory factory = configuration.apply(settings);
        return factory.apply("database");
    }
}
----

You can work with several column database instances through CDI qualifier. To identify each database instance, make a ``ColumnManager`` visible for CDI by putting the ```@Produces``` and the ```@Database``` annotations in the method.

[source,java]
----
@Inject
@Database(value = DatabaseType.COLUMN, provider = "databaseA")
private ColumnTemplate templateA;

@Inject
@Database(value = DatabaseType.COLUMN, provider = "databaseB")
private ColumnTemplate templateB;

// producers methods
@Produces
@Database(value = DatabaseType.COLUMN, provider = "databaseA")
public ColumnManager getManagerA() {
    return manager;
}

@Produces
@Database(value = DatabaseType.COLUMN, provider = "databaseB")
public ColumnManager getManagerB() {
    return manager;
}
----

=== Document

Jakarta NoSQL provides a Document template to explore the specific behavior of this NoSQL type.

Eclipse JNoSQL offers a mapping implementation for Document NoSQL types:

[source,xml]
----
<dependency>
    <groupId>org.eclipse.jnosql.mapping</groupId>
    <artifactId>jnosql-mapping-document</artifactId>
    <version>1.1.19</version>
</dependency>
----

Furthermore, check for a Document databases. You can find some implementations in the https://github.com/eclipse/jnosql-databases[JNoSQL Databases].

[source,java]
----
@Inject
DocumentTemplate template;
...

Car ferrari = Car.id(1L)
        .name("ferrari")
        .city("Rome")
        .type(CarType.SPORT);

template.insert(ferrari);
Optional<Car> car = template.find(Car.class, 1L);

template.delete(Car.class).where("id").eq(1L).execute();

Optional<Car> result = template.singleResult("FROM Car WHERE _id = 1");
----

Document is database agnostic. Thus, you can change the database in your application with no or minimal impact on source code.

You can define the database settings using the https://microprofile.io/microprofile-config/[MicroProfile Config] specification, so you can add properties and overwrite it in the environment following the https://12factor.net/config[Twelve-Factor App].

[source,properties]
----
jnosql.document.database=<DATABASE>
jnosql.document.provider=<CLASS-DRIVER>
jnosql.provider.host=<HOST>
jnosql.provider.user=<USER>
jnosql.provider.password=<PASSWORD>
----

TIP: The ```jnosql.document.provider``` property is necessary when you have more than one driver in the classpath. Otherwise, it will take the first one.

These configuration settings are the default behavior. Nevertheless, there is an option to programmatically configure these settings. Create a class that implements the ```Supplier<DocumentManager>```, then define it using the ```@Alternative``` and ```@Priority``` annotations.

[source,java]
----
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
@ApplicationScoped
public class ManagerSupplier implements Supplier<DatabaseManager> {

    @Produces
    @Database(DatabaseType.DOCUMENT)
    public DatabaseManager get() {
        Settings settings = Settings.builder()
                .put("credential", "value")
                .build();
        DatabaseConfiguration configuration = new NoSQLDocumentProvider();
        DatabaseManagerFactory factory = configuration.apply(settings);
        return factory.apply("database");
    }
}
----

You can work with several document database instances through CDI qualifier. To identify each database instance, make a ```DocumentManager``` visible for CDI by putting the ```@Produces``` and the ```@Database``` annotations in the method.

[source,java]
----
@Inject
@Database(value = DatabaseType.DOCUMENT, provider = "databaseA")
private DocumentTemplate templateA;

@Inject
@Database(value = DatabaseType.DOCUMENT, provider = "databaseB")
private DocumentTemplate templateB;

// producers methods
@Produces
@Database(value = DatabaseType.DOCUMENT, provider = "databaseA")
public DocumentManager getManagerA() {
    return manager;
}

@Produces
@Database(value = DatabaseType.DOCUMENT, provider = "databaseB")
public DocumentManager getManagerB() {
    return manager;
}
----

=== Time Series

Use the Time Series Mapping module to store, query, and manage Java entities in a time-series database.

Add the module to your project:

[source,xml]
----
<dependency>
    <groupId>org.eclipse.jnosql.mapping</groupId>
    <artifactId>jnosql-mapping-timeseries</artifactId>
    <version>1.1.19</version>
</dependency>
----

Map a time-series record as a Jakarta NoSQL entity:

[source,java]
----
@Entity
public class SensorReading {

    @Id
    private Instant timestamp;

    @Column
    private String sensor;

    @Column
    private double value;

    // constructors, getters, and setters
}
----

Inject `TimeSeriesTemplate` to perform persistence and query operations:

[source,java]
----
@Inject
private TimeSeriesTemplate template;

SensorReading reading = new SensorReading(
        Instant.now(),
        "sensor-1",
        21.5);

template.insert(reading);

Optional<SensorReading> result =
        template.find(SensorReading.class, reading.getTimestamp());

List<SensorReading> readings = template.select(SensorReading.class)
        .where("sensor").eq("sensor-1")
        .result();
----

You can also use Jakarta Data repositories:

[source,java]
----
@Repository
public interface SensorReadingRepository
        extends BasicRepository<SensorReading, Instant> {

    List<SensorReading> findBySensor(String sensor);
}

@Inject
@Database(DatabaseType.TIME_SERIES)
private SensorReadingRepository repository;
----

Configure the database name and provider using MicroProfile Config:

[source,properties]
----
jnosql.timeseries.database=<DATABASE>
jnosql.timeseries.provider=<CLASS-DRIVER>
jnosql.provider.host=<HOST>
jnosql.provider.user=<USER>
jnosql.provider.******
----

TIP: The `jnosql.timeseries.provider` property is necessary when more than one time-series database provider is available in the classpath.

Applications that create the database manager programmatically can expose it through CDI:

[source,java]
----
@Produces
@Database(DatabaseType.TIME_SERIES)
public DatabaseManager getTimeSeriesManager() {
    return manager;
}
----

Use named providers when the application connects to multiple time-series databases:

[source,java]
----
@Inject
@Database(value = DatabaseType.TIME_SERIES, provider = "metrics")
private TimeSeriesTemplate metrics;

@Inject
@Database(value = DatabaseType.TIME_SERIES, provider = "audit")
private TimeSeriesTemplate audit;
----

=== Vector Database

Use the Vector Mapping module to store and query Java entities in a vector database.

Add the module to your project:

[source,xml]
----
<dependency>
    <groupId>org.eclipse.jnosql.mapping</groupId>
    <artifactId>jnosql-mapping-vector</artifactId>
    <version>1.1.19</version>
</dependency>
----

A regular Jakarta NoSQL entity can be persisted in a vector database by declaring one `@Column` attribute whose type implements `Vector`.

[source,java]
----
@Entity
public class Article {

    @Id
    private String id;

    @Column
    private String content;

    @Column
    private String author;

    @Column
    private int year;

    @Column
    private DenseVector embedding;

    // constructors, getters, and setters
}
----

For a vector database, the entity is interpreted conceptually as:

[source,text]
----
Article
├── id        -> identifier
├── embedding -> vector
└── payload
    ├── content
    ├── author
    └── year
----

The vector attribute does not require a predefined name such as `embedding`. It is identified by the `Vector` type hierarchy. All remaining persisted attributes are available to the provider as payload or metadata.

The initial supported representation is `DenseVector`, which represents an ordered sequence of floating-point values:

[source,java]
----
DenseVector vector = DenseVector.of(
        0.12F,
        0.45F,
        0.78F
);
----

The number of dimensions corresponds to the number of values in the vector.

Vector generation is outside the scope of Eclipse JNoSQL. A vector may be produced by an embedding model, recommendation model, image or audio encoder, feature extractor, or another application-level mechanism. Eclipse JNoSQL is responsible for persisting and querying the resulting vector.

Inject `VectorTemplate` to use vector-native search operations:

[source,java]
----
@Inject
private VectorTemplate template;

Vector queryVector = DenseVector.of(
        0.10F,
        0.42F,
        0.80F
);

List<Article> articles = template.searchNearestNeighbors(
        Article.class,
        queryVector,
        Limit.of(10)
);
----

`VectorTemplate` extends `Template`, so regular persistence operations such as insert, update, find by identifier, and delete remain available:

[source,java]
----
Article article = new Article(
        "article-123",
        "Jakarta NoSQL and vector databases",
        "Otavio Santana",
        2026,
        vector
);

template.insert(article);

Optional<Article> result =
        template.find(Article.class, "article-123");
----

Vector databases primarily provide similarity-based search rather than traditional lexical or exact-value query operations. Therefore, some regular query operations inherited from `Template` may not be supported by a particular vector database provider and may result in `UnsupportedOperationException`.

Payload attributes can also be used to restrict nearest-neighbor searches:

[source,java]
----
List<Article> articles = template.searchNearestNeighbors(
        Article.class,
        queryVector,
        Map.of(
                "author", "Otavio Santana",
                "year", 2026
        ),
        Limit.of(10)
);
----

In the initial API, each entry in the filter map represents an equality predicate, and multiple entries are combined using logical `AND`.

Threshold-based vector searches are also available:

[source,java]
----
List<Article> articles = template.searchWithinThreshold(
        Article.class,
        queryVector,
        0.85F
);
----

The meaning and valid range of a threshold depend on the similarity or distance metric configured by the underlying vector database.

=== Graph

Eclipse JNoSQL provides a Graph API that simplifies working with graph databases such as Neo4j and Apache TinkerPop. This API enables seamless integration with graph databases while following the Jakarta NoSQL specifications.

To start using graph databases with Eclipse JNoSQL, add the required dependency:

[source,xml]
----
<dependency>
    <groupId>org.eclipse.jnosql.mapping</groupId>
    <artifactId>jnosql-mapping-graph</artifactId>
    <version>1.1.19</version>
</dependency>
----

==== Using the Relationship on Graph (Edge)

The `EdgeBuilder` provides a fluent API to define edges between entities, including properties.

[source,java]
----

private GraphTemplate template;

Person person = new Person();
Book book = new Book();

Edge<Person, Book> edge = Edge.source(person)
        .label("READS")
        .target(book)
        .property("since", 2019)
        .property("format", "digital")
        .build();

template.edge(edge);
----

==== Configuring a Graph Database

You can configure your graph database using MicroProfile Config properties.

[source,properties]
----
jnosql.graph.database=<DATABASE>
jnosql.graph.provider=<CLASS-DRIVER>
----

You can also configure the database programmatically by providing a `GraphDatabaseManager` implementation.

[source,java]
----
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
@ApplicationScoped
public class GraphManagerSupplier implements Supplier<GraphDatabaseManager> {

    @Produces
    @Database(DatabaseType.GRAPH)
    @Default
    public GraphDatabaseManager get() {
        Settings settings = Settings.builder()
                .put("credential", "value")
                .build();
        GraphConfiguration configuration = new NoSQLGraphProvider();
        GraphDatabaseManagerFactory factory = configuration.apply(settings);
        return factory.apply("database");
    }
}
----

By using the `@Database` annotation, multiple graph database instances can be configured for CDI injection.

[source,java]
----
@Inject
@Database(value = DatabaseType.GRAPH, provider = "graphA")
private GraphTemplate graphA;

@Inject
@Database(value = DatabaseType.GRAPH, provider = "graphB")
private GraphTemplate graphB;
----

=== Jakarta Data

Eclipse JNoSQL as a https://jakarta.ee/specifications/data/1.0/jakarta-data-1.0[Jakarta Data] exploring more the NoSQL capabilities, provides a mapping API that allows you to map Java objects to NoSQL databases.

=== Jakarta NoSQL

Eclipse JNoSQL is a https://jakarta.ee/specifications/nosql/1.0/jakarta-nosql-1.0[Jakarta NoSQL] implementation that provides a standard way to access NoSQL databases in Java applications. It offers a set of annotations and APIs to interact with various NoSQL database types, including Key-Value, Column Family, Document, Graph, and Time Series.

=== More Information

Check the https://www.jnosql.org/spec/[reference documentation] and https://www.jnosql.org/javadoc/[JavaDocs] to learn more.

== Code of Conduct

This project is governed by the Eclipse Foundation Code of Conduct. By participating, you are expected to uphold this code of conduct. Please report unacceptable behavior to mailto:codeofconduct@eclipse.org[codeofconduct@eclipse.org].

== Getting Help

Having trouble with Eclipse JNoSQL? We’d love to help!

Please report any bugs, concerns or questions with Eclipse JNoSQL to https://github.com/eclipse/jnosql[https://github.com/eclipse/jnosql].

If your issue refers to the https://github.com/eclipse/jnosql-databases[JNoSQL databases project] or
the https://github.com/eclipse/jnosql-extensions[JNoSQL extensions project], please, open the issue in this repository following the instructions in the
templates.

== Building from Source

You don’t need to build from source to use the project, but should you be interested in doing so, you can build it using Maven and Java 21 or higher.

[source, Bash]
----
mvn clean install
----

== Contributing

We are very happy you are interested in helping us and there are plenty ways you can do so.

- https://github.com/eclipse/jnosql/issues[**Open an Issue:**]  Recommend improvements, changes and report bugs

- **Open a Pull Request:** If you feel like you can even make changes to our source code and suggest them, just check out our link:CONTRIBUTING.adoc[contributing guide] to learn about the development process, how to suggest bugfixes and improvements.

Here are the badges of this project:
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

== Testing Guideline

This project's testing guideline will help you understand Jakarta Data's testing practices.
Please take a look link:TESTING-GUIDELINE.adoc[at the file].

== Migration

This migration guide explains how to upgrade from Eclipse JNoSQL version 1.0.0-b6 to the latest version, considering two significant changes: upgrading to Jakarta EE 9 and reducing the scope of the Jakarta NoSQL specification to only run on the Mapping. The guide provides instructions on updating package names and annotations to migrate your Eclipse JNoSQL project successfully.

link:MIGRATION.adoc[Migration Guide]

== Compatibility and Innovation Strategy



Eclipse JNoSQL preserves stability and drives innovation by using a versioning strategy based on https://semver.org/[Semantic Versioning] and Jakarta EE platform generations.



The Eclipse JNoSQL version follows the `MAJOR.MINOR.PATCH` format:



[source,text]
----
MAJOR.MINOR.PATCH
----

Where:

* *MAJOR* reflects the Jakarta EE generation supported by the Eclipse JNoSQL release line. Upgrading to a new Jakarta EE generation requires a new major version. Because each Jakarta EE platform version may require a different minimum Java version, applications must verify and adopt the necessary Java version when migrating between major versions.
* *MINOR* introduces backward-compatible features, APIs, and improvements while remaining within the same Jakarta EE generation.
* *PATCH* delivers backward-compatible bug fixes and maintenance updates without adding new features.



For example:



[source,text]
----
1.x.x -> Jakarta EE 11
2.x.x -> Jakarta EE 12
3.x.x -> next Jakarta EE generation
----

A new Jakarta EE generation requires a new Eclipse JNoSQL major version.

Within each major version, minor and patch releases follow Semantic Versioning.

The `1.x.x` release line of Eclipse JNoSQL targets Jakarta EE 11.

Within this release line:

* A *patch release*, such as `1.1.19` to `1.1.20`, contains backward-compatible bug fixes, maintenance updates, and compatible corrections.
* A *minor release*, such as `1.1.x` to `1.2.0`, may add new backward-compatible features, APIs, and improvements while remaining compatible with Jakarta EE 11.
* A *major release*, such as moving from `1.x.x` to `2.0.0`, upgrades Eclipse JNoSQL to the next Jakarta EE generation and establishes a new compatibility baseline. This migration may also require adopting a newer Java version, as specified by the target Jakarta EE platform.



For example:



[source,text]
----
1.1.19 -> Jakarta EE 11, maintenance release
1.1.20 -> Jakarta EE 11, maintenance release
1.2.0  -> Jakarta EE 11, new backward-compatible features
1.3.0  -> Jakarta EE 11, additional backward-compatible features
2.0.0  -> Jakarta EE 12
----



Applications can upgrade within the same major version to stay on the same Jakarta EE generation.



Minor releases add new capabilities without changing the Jakarta EE compatibility baseline. Patch releases provide bug fixes and maintenance updates only.



A new major version adopts a new Jakarta EE generation. For example, Eclipse JNoSQL `2.x.x` targets Jakarta EE 12. Applications upgrading from `1.x.x` to `2.x.x` must manage the Jakarta EE platform transition, verify the required Java version, and deal with any incompatible API changes.



The versioning contract can be summarized as follows:



* *PATCH* releases provide backward-compatible bug fixes and maintenance updates.
* *MINOR* releases introduce backward-compatible features and improvements within the same Jakarta EE generation.
* *MAJOR* releases align Eclipse JNoSQL with a new Jakarta EE generation and may introduce backward-incompatible API changes. Migrating to a major version also requires checking the minimum Java version the target Jakarta EE platform supports.

This strategy combines Semantic Versioning with an explicit Jakarta EE compatibility model:

[source,text]
----
Eclipse JNoSQL 1.x.x -> Jakarta EE 11
Eclipse JNoSQL 2.x.x -> Jakarta EE 12
Eclipse JNoSQL 3.x.x -> next Jakarta EE generation
----

The major version indicates the Jakarta EE compatibility baseline. Minor and patch versions allow Eclipse JNoSQL to evolve within that baseline according to Semantic Versioning. When upgrading between major versions, developers should consider both the Jakarta EE platform version and its minimum Java requirement as part of the compatibility boundary.

== Learn More

If you want to know more about both the communication and mapping layer, there are two complementary files for it each specific topic:

* link:MAPPING.adoc[Mapping API]
