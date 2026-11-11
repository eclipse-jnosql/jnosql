/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 */
package org.eclipse.jnosql.mapping.reflection.repository;

import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import org.eclipse.jnosql.mapping.reflection.entities.Person;

import java.util.stream.Stream;

/*
 * A Custom Repository (does not extend DataRepository) that references a valid
 * jnosql entity, but is provided by another vendor, so jnosql must ignore it.
 */
@Repository(provider = "Liberty")
public interface OtherProviderCustomRepository {

    @Find
    Stream<Person> all();
}
