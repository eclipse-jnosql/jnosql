/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.util.List;
import java.util.Map;

final class JsonObjects {

    private static final Jsonb JSON = JsonbBuilder.create();

    private JsonObjects() {
    }

    static List<Element> getColumns(JsonObject jsonObject) {
        Map<String, Object> map = JSON.fromJson(jsonObject.toString(), Map.class);
        return Elements.of(map);
    }

}
