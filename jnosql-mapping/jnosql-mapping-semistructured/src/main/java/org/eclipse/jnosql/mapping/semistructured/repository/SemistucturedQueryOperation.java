package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ApplicationScoped
class SemistucturedQueryOperation implements QueryOperation {

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        return null;
    }
}
