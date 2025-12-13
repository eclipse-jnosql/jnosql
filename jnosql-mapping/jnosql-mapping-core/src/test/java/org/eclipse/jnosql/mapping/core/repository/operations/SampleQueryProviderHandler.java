package org.eclipse.jnosql.mapping.core.repository.operations;


import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.ProviderQuery;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ProviderQuery("sample-query-provider")
@ApplicationScoped
public class SampleQueryProviderHandler implements ProviderQueryHandler {

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        return null;
    }
}
