/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.semistructured.IdFieldNameSupplier;
import org.eclipse.jnosql.mapping.metadata.DefaultFieldValue;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldValue;

final class DefaultIdAttributeFieldValue extends AbstractAttributeFieldValue {

    private final IdFieldNameSupplier idFieldNameSupplier;

    private DefaultIdAttributeFieldValue(FieldValue fieldValue, IdFieldNameSupplier idFieldNameSupplier) {
        super(fieldValue);
        this.idFieldNameSupplier = idFieldNameSupplier;
    }

    @Override
    protected String name() {
        return idFieldNameSupplier.defaultIdFieldName().orElseGet(() -> field().name());
    }

    static AttributeFieldValue of(Object value, FieldMetadata field, IdFieldNameSupplier idFieldNameSupplier) {
        return new DefaultIdAttributeFieldValue(new DefaultFieldValue(value, field), idFieldNameSupplier);
    }
}
