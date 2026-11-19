# AGENTS.md – Instructions for AI Coding Agents

Eclipse JNoSQL is the reference implementation of [Jakarta NoSQL](https://jakarta.ee/specifications/nosql/) and a provider of [Jakarta Data](https://jakarta.ee/specifications/data/) repositories for NoSQL databases. Java, Maven, CDI, MicroProfile Config.

## 1. Repository overview

- **Communication layer** (`jnosql-communication/*`): database-independent contracts (`DatabaseManager`, `BucketManager`, queries, conditions, value readers/writers). No entities, annotations, or CDI.
- **Mapping layer** (`jnosql-mapping/*`): entity metadata, entity ↔ communication conversion, templates, Jakarta Data repositories, CDI integration. Depends on Communication, never the reverse.
- **Specifications**: Jakarta NoSQL and Jakarta Data APIs are standardized; framework features beyond them are JNoSQL-specific. Jakarta Query integration is not complete.
- **Drivers** live in the separate `jnosql-databases` repository. Do not add database drivers or vendor-specific dependencies here.

## 2. Read before changing code

| Change | Read |
|---|---|
| Every change | [`development-guides/common.md`](development-guides/common.md), [`TESTING-GUIDELINE.adoc`](TESTING-GUIDELINE.adoc), [`CONTRIBUTING.adoc`](CONTRIBUTING.adoc) |
| Communication | + [`development-guides/communication.md`](development-guides/communication.md) |
| Mapping | + [`development-guides/mapping.md`](development-guides/mapping.md) |
| Both layers | + both specialized guides |

The guides are authoritative; do not restate them in code comments or new docs.

## 3. Workflow

1. Identify the affected module and its public API contracts.
2. Read the applicable guides.
3. Inspect the current implementation and tests.
4. Understand the existing behavior before altering it.
5. Identify compatibility risks.
6. Define the smallest appropriate change.
7. Implement with semantic names and clear responsibilities.
8. Add or update tests.
9. Validate the affected modules (and dependent modules for shared code).
10. Review the full diff before committing.

## 4. Principles

- Semantic, intent-revealing code; small cohesive classes and methods.
- Pragmatic SOLID; composition over unnecessary inheritance; no speculative abstractions.
- Strategy for real behavioral variation; avoid growing `if`/`switch` chains (simple guard clauses are fine).
- Clear API contracts: predictable nulls, consistent exceptions, minimal public surface.
- Correct UUID and converter semantics; no invented conversion guarantees.
- Preserve backward compatibility and Jakarta specification behavior.
- Keep Communication and Mapping separate; no unnecessary vendor-specific dependencies.

## 5. Public API documentation

Developer-facing public APIs need concise Javadoc (`@param`, `@return`, `@throws`, limitations). Important methods get a short, verified Java example. Do not document internal or package-private details, and a `public` class is not automatically developer-facing. See `common.md` §6.

## 6. Testing

Follow [`TESTING-GUIDELINE.adoc`](TESTING-GUIDELINE.adoc). Run the tests of the affected modules (e.g. `mvn -pl <module> -am verify`). **Never claim tests or builds pass without running them**; if you could not run them, say so.

## 7. Git

- Work on a dedicated branch per task.
- Use Conventional Commits (`docs:`, `feat:`, `fix:`, `test:`, …).
- Documentation tasks: exactly one file per commit.
- Stage files explicitly (no `git add -A` / `.`).
- Commit with `git commit -s` (DCO); keep the configured human author.
- Never add AI agents or automated tools as authors or co-authors; no AI-related `Co-authored-by:` trailers.
- Do not push or open a PR unless explicitly instructed.
