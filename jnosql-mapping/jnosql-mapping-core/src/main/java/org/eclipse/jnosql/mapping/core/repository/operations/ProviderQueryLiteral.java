/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.enterprise.util.AnnotationLiteral;
import org.eclipse.jnosql.mapping.ProviderQuery;

final class ProviderQueryLiteral extends AnnotationLiteral<ProviderQuery>
        implements ProviderQuery {

    private final String value;

    private ProviderQueryLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    static ProviderQueryLiteral of(String value) {
        return new ProviderQueryLiteral(value);
    }
}