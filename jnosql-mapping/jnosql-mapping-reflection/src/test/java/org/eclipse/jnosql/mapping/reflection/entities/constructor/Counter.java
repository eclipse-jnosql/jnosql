/*
 *   Copyright (c) 2023 Contributors to the Eclipse Foundation
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    and Apache License v2.0 which accompanies this distribution.
 *    The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *    and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *    You may elect to redistribute this code under either of these licenses.
 *
 *    Contributors:
 *
 *    Jesse Gallagher
 */
package org.eclipse.jnosql.mapping.reflection.entities.constructor;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;

@Entity
public record Counter(@Column int count, @Column boolean active, @Column double ratio, @Column char code) {

}
