package org.eclipse.jnosql.communication.graph;

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;

import java.util.Map;

public interface CommunicationEdge {

    Object id();

    CommunicationEntity source();

    CommunicationEntity target();

    String label();

    Map<String, Object> properties();
}
