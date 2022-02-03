/*
 *  Copyright (c) 2020 Otávio Santana and others
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.mapping.keyvalue.reactive;

import jakarta.nosql.mapping.keyvalue.KeyValueTemplate;

import jakarta.enterprise.inject.Vetoed;
import java.util.Objects;

class DefaultReactiveKeyValueTemplateProducer implements ReactiveKeyValueTemplateProducer {

    @Override
    public ReactiveKeyValueTemplate get(KeyValueTemplate template) {
        Objects.requireNonNull(template, "template is required");
        return new ProducerReactiveKeyValueTemplate(template);
    }

    @Vetoed
    static class ProducerReactiveKeyValueTemplate extends AbstractReactiveKeyValueTemplate {

        private KeyValueTemplate template;

        ProducerReactiveKeyValueTemplate() {
        }

        ProducerReactiveKeyValueTemplate(KeyValueTemplate template) {
            this.template = template;
        }

        @Override
        protected KeyValueTemplate getTemplate() {
            return template;
        }
    }
}
