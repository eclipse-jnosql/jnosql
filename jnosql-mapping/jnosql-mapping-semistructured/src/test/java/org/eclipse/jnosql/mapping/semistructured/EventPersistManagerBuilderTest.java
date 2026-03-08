/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class EventPersistManagerBuilderTest {

    static class Jedi {
        final String name;

        Jedi(String name) {
            this.name = name;
        }
    }

    @Test
    @DisplayName("builder() should return a PrePersistStep instance")
    void shouldReturnPrePersistStepFromBuilder() {
        EventPersistManagerBuilder builder = EventPersistManagerBuilder.builder();
        assertThat(builder).isInstanceOf(EventPersistManagerBuilder.PrePersistStep.class);
    }

    @Test
    @DisplayName("build() from PrePersistStep should return a non-null EventPersistManager")
    void shouldBuildFromPrePersistStep() {
        EventPersistManager manager = EventPersistManagerBuilder.builder().build();
        assertThat(manager).isNotNull();
    }

    @Test
    @DisplayName("build() without consumers should not throw when firePreEntity is called")
    void shouldNotThrowWhenFirePreEntityWithNoOp() {
        EventPersistManager manager = EventPersistManagerBuilder.builder().build();
        org.assertj.core.api.Assertions.assertThatCode(() -> manager.firePreEntity(new Jedi("Luke")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("build() without consumers should not throw when firePostEntity is called")
    void shouldNotThrowWhenFirePostEntityWithNoOp() {
        EventPersistManager manager = EventPersistManagerBuilder.builder().build();
        org.assertj.core.api.Assertions.assertThatCode(() -> manager.firePostEntity(new Jedi("Luke")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("firePreEntity should invoke the pre-persist consumer with the correct entity")
    void shouldInvokePrePersistConsumer() {
        List<EntityPrePersist> captured = new ArrayList<>();
        Jedi jedi = new Jedi("Luke");

        EventPersistManager manager = EventPersistManagerBuilder.builder()
                .withPrePersist(captured::add)
                .build();

        manager.firePreEntity(jedi);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(captured).as("pre-persist consumer should have been called once").hasSize(1);
            softly.assertThat(captured.get(0).get()).as("captured entity should match").isSameAs(jedi);
        });
    }

    @Test
    @DisplayName("firePostEntity should invoke the post-persist consumer with the correct entity")
    void shouldInvokePostPersistConsumer() {
        List<EntityPostPersist> captured = new ArrayList<>();
        Jedi jedi = new Jedi("Luke");

        EventPersistManager manager = EventPersistManagerBuilder.builder()
                .withPostPersist(captured::add)
                .build();

        manager.firePostEntity(jedi);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(captured).as("post-persist consumer should have been called once").hasSize(1);
            softly.assertThat(captured.get(0).get()).as("captured entity should match").isSameAs(jedi);
        });
    }

    @Test
    @DisplayName("withPostPersist() from PrePersistStep should advance to PostPersistStep")
    void shouldAdvanceToPostPersistStep() {
        EventPersistManagerBuilder.PrePersistStep prePersistStep = EventPersistManagerBuilder.builder();
        EventPersistManagerBuilder next = prePersistStep.withPostPersist(e -> {});
        assertThat(next).isInstanceOf(EventPersistManagerBuilder.PostPersistStep.class);
    }

    @Test
    @DisplayName("build() from PostPersistStep should invoke both consumers")
    void shouldInvokeBothConsumersFromPostPersistStep() {
        List<EntityPrePersist> preCaptured = new ArrayList<>();
        List<EntityPostPersist> postCaptured = new ArrayList<>();
        Jedi jedi = new Jedi("Yoda");

        EventPersistManager manager = EventPersistManagerBuilder.builder()
                .withPrePersist(preCaptured::add)
                .withPostPersist(postCaptured::add)
                .build();

        manager.firePreEntity(jedi);
        manager.firePostEntity(jedi);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(preCaptured).as("pre-persist consumer should have been called once").hasSize(1);
            softly.assertThat(preCaptured.get(0).get()).as("pre-persist captured entity should match").isSameAs(jedi);
            softly.assertThat(postCaptured).as("post-persist consumer should have been called once").hasSize(1);
            softly.assertThat(postCaptured.get(0).get()).as("post-persist captured entity should match").isSameAs(jedi);
        });
    }

    @Test
    @DisplayName("withPrePersist() on PrePersistStep should return a new step without modifying the original")
    void shouldBeImmutableOnPrePersistStep() {
        Consumer<EntityPrePersist> firstConsumer = e -> {};
        Consumer<EntityPrePersist> secondConsumer = e -> {};

        EventPersistManagerBuilder.PrePersistStep original = EventPersistManagerBuilder.builder()
                .withPrePersist(firstConsumer);
        EventPersistManagerBuilder.PrePersistStep updated = original.withPrePersist(secondConsumer);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(original.prePersist())
                    .as("original step should still hold the first consumer")
                    .isSameAs(firstConsumer);
            softly.assertThat(updated.prePersist())
                    .as("updated step should hold the second consumer")
                    .isSameAs(secondConsumer);
            softly.assertThat(original).as("original and updated should be different instances").isNotSameAs(updated);
        });
    }

    @Test
    @DisplayName("withPrePersist() on PostPersistStep should return a new step without modifying the original")
    void shouldBeImmutableOnPostPersistStepPrePersist() {
        Consumer<EntityPrePersist> firstConsumer = e -> {};
        Consumer<EntityPrePersist> secondConsumer = e -> {};
        Consumer<EntityPostPersist> postConsumer = e -> {};

        EventPersistManagerBuilder.PostPersistStep original = EventPersistManagerBuilder.builder()
                .withPrePersist(firstConsumer)
                .withPostPersist(postConsumer);
        EventPersistManagerBuilder.PostPersistStep updated = original.withPrePersist(secondConsumer);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(original.prePersist())
                    .as("original step should still hold the first pre-persist consumer")
                    .isSameAs(firstConsumer);
            softly.assertThat(updated.prePersist())
                    .as("updated step should hold the second pre-persist consumer")
                    .isSameAs(secondConsumer);
            softly.assertThat(original.postPersist())
                    .as("post-persist consumer should be unchanged in both")
                    .isSameAs(updated.postPersist());
        });
    }

    @Test
    @DisplayName("withPostPersist() on PostPersistStep should return a new step without modifying the original")
    void shouldBeImmutableOnPostPersistStepPostPersist() {
        Consumer<EntityPostPersist> firstPostConsumer = e -> {};
        Consumer<EntityPostPersist> secondPostConsumer = e -> {};

        EventPersistManagerBuilder.PostPersistStep original = EventPersistManagerBuilder.builder()
                .withPostPersist(firstPostConsumer);
        EventPersistManagerBuilder.PostPersistStep updated = original.withPostPersist(secondPostConsumer);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(original.postPersist())
                    .as("original step should still hold the first post-persist consumer")
                    .isSameAs(firstPostConsumer);
            softly.assertThat(updated.postPersist())
                    .as("updated step should hold the second post-persist consumer")
                    .isSameAs(secondPostConsumer);
            softly.assertThat(original).as("original and updated should be different instances").isNotSameAs(updated);
        });
    }
}
