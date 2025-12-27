/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class DeleteProviderTest {

    @Test
    void shouldDeleteQuery() {
        DeleteProvider provider = DeleteProvider.INSTANCE;

        String query = "DELETE FROM users WHERE id = 1";
        var deleteQuery = provider.apply(query);

        SoftAssertions.assertSoftly(soft-> soft.assertThat(deleteQuery.entity()).isEqualTo("users"));
    }
}