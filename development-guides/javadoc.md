# Eclipse JNoSQL – Public API Javadoc Guide

Detailed rules for Javadoc on developer-facing APIs. It expands [`common.md`](common.md) §6; shared principles are not repeated. Layer specifics: [`communication.md`](communication.md), [`mapping.md`](mapping.md). Compatibility rules: `common.md` §8 (a dedicated `development-guides/compatibility.md` does not exist on this branch; link it here once it does).

---

## 1. Purpose

Javadoc is part of the public Java API contract. Developers read it in the IDE and in generated API docs, so each documented API must answer quickly:

- **What** it does and **when** to use it.
- **How** to use it (an example for important operations).
- **What it returns**, including empty and null cases.
- **Limitations**: exceptions, unsupported operations, provider-dependent behavior.

If code and Javadoc disagree, the Javadoc is a bug. Describe what the code does, never what you hope it does.

## 2. Scope

| Requirement | Applies to |
|---|---|
| MUST have meaningful Javadoc | Developer-facing public interfaces and classes; their public methods and constructors; relevant public constants. Examples: `KeyValueTemplate`, `SemiStructuredTemplate`, `DatabaseManager`, `BucketManager`, `ValueReader`, `DatabaseQualifier`, `MappingConfigurations`. |
| MAY have short Javadoc | Public classes that exist only for wiring or SPI discovery (`*Extension`, `*Bean`, `*Supplier`, `*Producer`, `Default*`, `*Utils`, `Core*Operation`). Say what they are and that they are not API; no examples. |
| NOT required | Private, protected (unless a documented extension point such as `AbstractGraphTemplate`) and package-private members. |

`public` does not mean supported API. Decide with the rules in `common.md` §5/§6 and `mapping.md` §8. Do not add Javadoc to an internal class that makes it look supported.

Overriding methods inherit documentation. Use `{@inheritDoc}` (or no comment) only when the contract is identical; document the override when behavior is narrowed (for example `UUIDValueReader.read`, which returns `null` for unsupported input types).

## 3. Writing conventions

- First sentence: a complete, self-contained summary (it appears in method lists). Start with a verb for methods ("Returns…", "Saves…"), a noun phrase for types.
- Short, clear, didactic, technically accurate, written for a Java developer; one idea per sentence. Prefer two short paragraphs to one long one.
- Describe **observable behavior** (inputs, outputs, side effects, exceptions), not the algorithm or private collaborators.
- Do not repeat the method name ("Gets the name" on `getName()` adds nothing) and do not use marketing words ("powerful", "seamless").
- Do not promise what is not implemented or tested: ordering, thread-safety, atomicity, transactionality, idempotence. If it is provider-dependent, say so (as `KeyValueTemplate` does for query syntax).
- Mark JNoSQL-specific behavior as such; never describe it as a Jakarta specification feature (`common.md` §8).
- One example per important operation; no redundant examples for overloads.

## 4. Examples

Prioritize, in this order: Communication managers and factories, Mapping templates, repository-related APIs, configuration (`MappingConfigurations`, `Settings`), query construction and execution, conversion APIs (`ValueReader`/`ValueWriter`, converters), specialized model operations (edges, time series, vector search).

Syntax depends on the **branch's Java baseline** (verify in the root `pom.xml`, property `maven.compiler.source`):

| Branch | Baseline | Example syntax |
|---|---|---|
| `main` | Java 21 | `<pre>{@code … }</pre>` (used throughout the code base, e.g. `QueryPath`, `CriteriaCondition`). `{@snippet}` (JDK 18+) is available but is **not used anywhere in the repository today**; do not introduce it casually, and never mix styles inside one class. |
| `1.1.x`, `1.x.x` | Java 17 | `<pre>{@code … }</pre>` only. **`{@snippet}` is not supported** and breaks Javadoc generation. |

Rules:

- Inline code: `{@code value}`. Multi-line: `<pre>{@code … }</pre>`. Escape `@` at line start inside examples as `{@literal @}`, or avoid annotations in examples when a plain statement suffices.
- Keep examples to roughly 3–8 lines, with realistic names and only the imports a reader cannot guess omitted.
- Every example MUST compile against the real API or be copied from a passing test, and the surrounding behavior MUST be true. Name the test or source you based it on in your PR description.
- Don't show vendor-specific setup in framework Javadoc; the driver lives in `jnosql-databases`.

Pattern (shape only; check signatures before reuse):

```java
/**
 * Saves an entity with a specified time to live (TTL).
 *
 * <pre>{@code
 * User saved = template.put(user, Duration.ofMinutes(10));
 * }</pre>
 *
 * @param entity the entity to be inserted
 * @param ttl    the time to live
 * @param <T>    the entity type
 * @return the saved entity
 * @throws NullPointerException          when the entity is null
 * @throws UnsupportedOperationException when expired time is not supported
 */
<T> T put(T entity, Duration ttl);
```

This is the existing `KeyValueTemplate.put(T, Duration)` contract, plus an example line.

## 5. Method documentation

| Tag | Rule |
|---|---|
| `@param` | Every parameter and type parameter. State constraints and null policy ("must not be null", "may be empty"). |
| `@return` | Meaning, not type. State empty/absent cases: `Optional.empty()`, empty collection, `null` (only when the method is documented to return it). |
| `@throws` | Each exception a caller can reasonably handle or must avoid: `NullPointerException` for null arguments, `IllegalArgumentException`, `UnsupportedOperationException` for optional operations, and the module's own exceptions (`CommunicationException`, `IdNotFoundException`, `jakarta.data.exceptions.MappingException`, …). Only document what the code actually throws. |
| `{@link}` | First reference to a type or member in the text. Fully resolvable; link to the API type, not to an implementation. |
| `@see` | Related contracts and alternatives (for example the Jakarta specification interface being extended). |
| `{@inheritDoc}` | Only when the inherited text is fully accurate (see §2). |
| `@since` | Use only if the class already uses it consistently; do not guess versions. |

Always state, when relevant: null handling, empty results, ordering (only if guaranteed), and operations a provider may not support.

## 6. UUID and converters

When an API reads, writes, or maps values, document only what the code and tests guarantee. Facts as of this branch (verify before relying on them):

- `UUIDValueReader.read(Class, Object)` returns the value as-is for a `UUID`, parses a `CharSequence` via `UUID.fromString`, throws `CommunicationException` for a malformed string, and returns `null` for any other input type; `test(Class)` is true only for `UUID.class` (`UUIDValueReaderTest`).
- There is no UUID writer: a `UUID` without a matching writer is passed through unchanged (`communication.md` §6). Document "stored as the driver's representation", not "stored as a string".
- Mapping has no UUID-specific code; `UUID` fields rely on the reader or on an `AttributeConverter` (`mapping.md` §3).

For any conversion API document: accepted Java types, the representation produced, behavior for `null`, invalid values and the exception type, and the round trip **only if a test proves it**. Do not write "round-trip safe" otherwise.

## 7. Compatibility

- Javadoc states contractual behavior. Changing it changes the contract; follow `common.md` §8 and treat tightened or removed guarantees as breaking.
- Javadoc on a release branch (`1.1.x`, `1.x.x`, `*-release`) must describe that branch. Do not document newer functionality there, do not use syntax newer than its baseline (see §4), and do not use `{@link}` to types that branch lacks.
- When a behavior is deprecated, use `@Deprecated` and `{@deprecated}` with the replacement (the legacy `core.query` and `semistructured.query` packages are examples).
- Forward-merge or backport Javadoc with the code; never one without the other.

## 8. Validation

The root `pom.xml` configures `maven-javadoc-plugin` (execution `attach-javadocs`, goal `jar`, `source` = `maven.compiler.source`) with default doclint settings. Therefore:

1. Generate Javadoc for each changed module: `mvn -pl <module> -am javadoc:javadoc` (or `mvn -pl <module> verify`, which runs the `jar` goal). Read the full output.
2. Fix every new warning or error in files you touched: broken `{@link}`/`@see`, unknown or misspelled tags, missing `@param`/`@return`/`@throws`, malformed HTML (unclosed `<p>`, stray `<`, `>`, `&` — use `{@code}` or `&lt;`).
3. Check signatures: parameters in `@param` match the declaration; `@throws` matches the code; overloads are documented individually.
4. Validate examples: compile them in a scratch class or test, or copy from an existing test. Run that test.
5. Use only standard Javadoc tags available on the branch's JDK baseline (§4). Do not add custom tags or taglets without a build change.
6. Compare warnings with a baseline run when the module already has some; do not fix unrelated ones in the same commit.

Do not claim Javadoc generation passed unless you ran it.

## 9. Definition of Done

- [ ] Every new or changed developer-facing public type, method, constructor, and constant has accurate Javadoc; internal public classes are not advertised.
- [ ] First sentences are standalone summaries; no filler, name repetition, or unsupported guarantees.
- [ ] `@param`, `@return`, `@throws` are complete and match the code, including null, empty, and unsupported cases.
- [ ] Important operations have a short example that compiles or comes from a passing test.
- [ ] UUID/converter semantics (types, representation, invalid values, null, round trip) are documented only as far as tests prove.
- [ ] `{@link}`/`@see` targets resolve; `{@inheritDoc}` is used only where identical.
- [ ] Syntax matches the branch's Java baseline (no `{@snippet}` on Java 17 branches); no new custom tags.
- [ ] Javadoc generated for the affected modules with no new warnings or errors.
- [ ] Javadoc is consistent with `common.md` §8 compatibility rules and the branch being changed.
