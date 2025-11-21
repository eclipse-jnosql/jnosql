package org.eclipse.jnosql.mapping.metadata.repository.spi;

/**
 * Executes a repository method annotated with {@code @Find}, deriving the query
 * dynamically from the structure and values of the method parameters rather than
 * from naming conventions or explicit query strings.
 */
public interface ParameterBasedOperation extends RepositoryOperation {
}