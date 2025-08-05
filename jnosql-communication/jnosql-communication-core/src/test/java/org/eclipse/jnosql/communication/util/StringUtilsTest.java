/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.communication.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class StringUtilsTest {

    @Test
    void shouldIsBlank() {
        Assertions.assertThat(StringUtils.isBlank(null)).isTrue();
        Assertions.assertThat(StringUtils.isBlank("")).isTrue();
        Assertions.assertThat(StringUtils.isBlank("      ")).isTrue();
        Assertions.assertThat(StringUtils.isBlank("bob")).isFalse();
        Assertions.assertThat(StringUtils.isBlank("  bob  ")).isFalse();
    }

}
