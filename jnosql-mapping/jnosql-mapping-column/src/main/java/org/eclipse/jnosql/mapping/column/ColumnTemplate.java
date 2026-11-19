/*
 *
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *
 */
package org.eclipse.jnosql.mapping.column;



import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;


/**
 * {@link SemiStructuredTemplate} specialization for wide-column databases.
 * Inject it to insert, update, find, query and delete entities mapped with Jakarta NoSQL annotations.
 * It adds no operations of its own; its contract is inherited from {@link SemiStructuredTemplate}.
 *
 * <pre>{@code
 * @Inject
 * ColumnTemplate template;
 *
 * Person saved = template.insert(person);
 * long total = template.count(Person.class);
 * }</pre>
 *
 * @see SemiStructuredTemplate
 */
public interface ColumnTemplate extends SemiStructuredTemplate {


}
