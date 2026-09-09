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
package org.eclipse.jnosql.mapping.timeseries;



import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;


/**
 * Specializes the mapping template for time-series databases.
 * <p>
 * Use this API to persist mapped measurements or events, retrieve them by identifier, and execute
 * queries over fields such as a source, timestamp, or measured value.
 * </p>
 * <p>
 * The default template can be injected directly:
 * </p>
 * <pre>
 * &#64;Inject
 * &#64;Database(DatabaseType.TIME_SERIES)
 * TimeSeriesTemplate template;
 * </pre>
 * <p>
 * When an application uses multiple time-series databases, select one with
 * {@link org.eclipse.jnosql.mapping.Database#provider()}.
 * </p>
 */
public interface TimeSeriesTemplate extends SemiStructuredTemplate {


}
