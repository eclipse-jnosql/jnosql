package org.eclipse.jnosql.mapping.graph;

import org.eclipse.jnosql.communication.graph.CommunicationEdge;
import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class AbstractGraphTemplate extends AbstractSemiStructuredTemplate implements GraphTemplate {

    private static final Logger LOGGER = Logger.getLogger(AbstractGraphTemplate.class.getName());

    protected abstract GraphDatabaseManager manager();

    @Override
    public <T, E> Edge<T, E> edge(T source, Supplier<String> label, E target, Map<String, Object> properties) {
        Objects.requireNonNull(label, "label is required");
        LOGGER.fine(() -> "Creating edge for " + label);
        return edge(source, label.get(), target, properties);
    }

    @Override
    public <T, E> Edge<T, E> edge(Edge<T, E> edge) {
        Objects.requireNonNull(edge, "edge is required");
        return edge(edge.source(), edge.label(), edge.target(), edge.properties());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, E> Edge<T, E> edge(T source, String label, E target, Map<String, Object> properties) {
        Objects.requireNonNull(source, "source is required");
        Objects.requireNonNull(label, "label is required");
        Objects.requireNonNull(target, "target is required");
        Objects.requireNonNull(properties, "properties is required");
        LOGGER.fine(() -> "Creating edge for " + label + " between " + source + " and " + target);
        var sourceCommunication = converter().toCommunication(source);
        var targetCommunication = converter().toCommunication(target);

        var communicationEdge = manager().edge(sourceCommunication, label, targetCommunication, properties);
        LOGGER.fine(() -> "Created edge for " + label + " between " + source + " and " + target + " with id: " + communicationEdge.id());
        T updatedSource = (T) converter().toCommunication(communicationEdge.source());
        E updatedTarget = (E) converter().toCommunication(communicationEdge.target());

        return new DefaultEdge<>(updatedSource, updatedTarget, label,properties, communicationEdge.id());
    }

    @Override
    public <T, E> void delete(Edge<T, E> edge) {
        Objects.requireNonNull(edge, "edge is required");
        var id = edge.id().orElseThrow( () -> new IllegalArgumentException("The edge does not have an id"));
        LOGGER.fine(() -> "Deleting edge for " + edge.label() + " between " + edge.source() + " and " + edge.target() + " with id: " + id);
        this.deleteEdge(id);
    }

    @Override
    public <K> void deleteEdge(K id) {
        Objects.requireNonNull(id, "id is required");
        LOGGER.fine(() -> "Deleting edge for " + id);
        manager().deleteEdge(id);
    }

    @Override
    public <K, T, E> Optional<Edge<T, E>> findEdgeById(K id) {
        Objects.requireNonNull(id, "id is required");
        LOGGER.fine(() -> "Finding edge for " + id);
        Optional<CommunicationEdge> edge = manager().findEdgeById(id);
        return Optional.empty();
    }

}
