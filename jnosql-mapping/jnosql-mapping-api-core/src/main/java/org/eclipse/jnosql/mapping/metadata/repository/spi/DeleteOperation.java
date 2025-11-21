package org.eclipse.jnosql.mapping.metadata.repository.spi;

/**
 * Executes a repository method annotated with {@code @Delete}, removing one or
 * more entities that match the conditions defined by the method parameters.
 */
public interface DeleteOperation extends RepositoryOperation {
}