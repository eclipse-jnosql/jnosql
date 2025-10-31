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
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

@Default
@Database(DatabaseType.KEY_VALUE)
@ApplicationScoped
class DefaultKeyValueTemplate extends AbstractKeyValueTemplate {

    private KeyValueEntityConverter converter;

    private Instance<BucketManager> manager;

    private KeyValueEventPersistManager eventManager;

    @Inject
    DefaultKeyValueTemplate(KeyValueEntityConverter converter,
                            Instance<BucketManager> manager,
                            KeyValueEventPersistManager eventManager) {
        this.converter = converter;
        this.manager = manager;
        this.eventManager = eventManager;
    }

    DefaultKeyValueTemplate() {
    }

    @Override
    protected KeyValueEntityConverter getConverter() {
        return converter;
    }

    @Override
    protected BucketManager getManager() {
        return manager.get();
    }

    @Override
    protected KeyValueEventPersistManager getEventManager() {
        return eventManager;
    }
}
