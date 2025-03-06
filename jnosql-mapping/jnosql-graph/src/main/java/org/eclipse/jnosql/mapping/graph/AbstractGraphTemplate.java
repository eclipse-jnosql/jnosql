package org.eclipse.jnosql.mapping.graph;

import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractGraphTemplate extends AbstractSemiStructuredTemplate implements GraphTemplate {

    protected abstract GraphDatabaseManager manager();

    @Override
    public <T, E> Edge<T, E> edge(T source, String label, E target, Map<String, Object> properties) {
        return null;
    }

    @Override
    public <T, E> Edge<T, E> edge(T source, Supplier<String> label, E target, Map<String, Object> properties) {
        return null;
    }

    @Override
    public <T, E> Edge<T, E> edge(Edge<T, E> edge) {
        return null;
    }

    @Override
    public <T, E> void delete(Edge<T, E> edge) {

    }

    @Override
    public <K> void deleteEdge(K id) {

    }

    @Override
    public <K, T, E> Optional<Edge<T, E>> findEdgeById(K id) {
        return Optional.empty();
    }

}
