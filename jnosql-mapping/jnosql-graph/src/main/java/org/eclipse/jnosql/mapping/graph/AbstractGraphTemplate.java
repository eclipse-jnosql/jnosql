package org.eclipse.jnosql.mapping.graph;

import org.eclipse.jnosql.communication.graph.CommunicationEdge;
import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Provides a base implementation of the {@link GraphTemplate} interface using the
 * <b>Template Method Pattern</b>, ensuring consistent interaction with a {@link GraphDatabaseManager}.
 * <p>
 * This abstract class simplifies working with graph databases by handling common operations like:
 * <ul>
 *     <li>Creating edges (relationships) between entities.</li>
 *     <li>Deleting edges by reference or ID.</li>
 *     <li>Finding edges based on their unique identifiers.</li>
 * </ul>
 * <p>
 * Implementations of this class must provide a concrete {@link GraphDatabaseManager} via {@link #manager()},
 * which defines the actual database-specific behavior.
 * </p>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * class MyGraphTemplate extends AbstractGraphTemplate {
 *     private final GraphDatabaseManager manager;
 *
 *     MyGraphTemplate(GraphDatabaseManager manager) {
 *         this.manager = manager;
 *     }
 *
 *     @Override
 *     protected GraphDatabaseManager manager() {
 *         return manager;
 *     }
 * }
 *
 * GraphTemplate template = new MyGraphTemplate(graphManager);
 * Person person = new Person();
 * Book book = new Book();
 *
 * // Creating an edge
 * Edge<Person, Book> edge = template.edge(person, "READS", book, Map.of("since", 2020));
 *
 * // Finding an edge
 * Optional<Edge<Person, Book>> foundEdge = template.findEdgeById(edge.id().orElseThrow());
 *
 * // Deleting an edge
 * template.delete(edge);
 * }</pre>
 *
 * @see GraphTemplate
 * @see GraphDatabaseManager
 */
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
        return edge.map(e -> {
            LOGGER.fine(() -> "Found edge for " + id);
            T source = converter().toEntity(e.source());
            E target = converter().toEntity(e.target());
            return new DefaultEdge<>(source, target, e.label(), e.properties(), e.id());
        });
    }
}
