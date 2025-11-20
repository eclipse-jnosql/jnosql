package org.eclipse.jnosql.mapping.metadata.repository;

/**
 * Represents a unique signature of a repository method using its name and
 * the erased parameter types. This signature is used as a stable key for
 * method lookup in both reflection-based and annotation-processor-generated
 * repository implementations.
 * <p>
 *
 * @param name       the simple name of the method (e.g., {@code "findById"})
 * @param parameters the erased parameter types in declaration order
 */
public record MethodSignature(String name, Class<?>[] parameters) {
}
