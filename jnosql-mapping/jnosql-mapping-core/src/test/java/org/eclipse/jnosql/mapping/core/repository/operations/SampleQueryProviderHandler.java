package org.eclipse.jnosql.mapping.core.repository.operations;


import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.ProviderQuery;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryAnnotation;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Map;

@ProviderQuery("sample-query-provider")
@ApplicationScoped
public class SampleQueryProviderHandler implements ProviderQueryHandler {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {

        RepositoryMethod method = context.method();
        Object[] parameters = context.parameters();
        var sampleQueryProvider = method.annotations().stream()
                .filter(annotation -> SampleQueryProvider.class.equals(annotation.annotation()))
                .findFirst().orElseThrow();
        Map<String, Object> attributes = sampleQueryProvider.attributes();
        String value = (String) attributes.get("value");

        return (T) (value + " " + parameters[0]);
    }
}
