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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;

import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A cursor page that replaces the content of another cursor page while
 * preserving its pagination metadata and behavior.
 *
 * @param <T> the mapped content type
 */
public final class MappedCursoredPage<T> implements CursoredPage<T> {

    private final List<T> content;

    private final CursoredPage<?> delegate;

    private MappedCursoredPage(List<T> content, CursoredPage<?> delegate) {
        this.content = List.copyOf(requireNonNull(content, "content is required"));
        this.delegate = requireNonNull(delegate, "delegate is required");
    }

    /**
     * Creates a cursor page with mapped content and pagination behavior
     * delegated to the original page.
     *
     * @param content the mapped page content
     * @param delegate the cursor page that provides pagination behavior
     * @param <T> the mapped content type
     * @return the mapped cursor page
     * @throws NullPointerException if content or delegate is {@code null}, or
     *                              if content contains a {@code null} element
     */
    public static <T> CursoredPage<T> of(List<T> content, CursoredPage<?> delegate) {
        return new MappedCursoredPage<>(content, delegate);
    }

    @Override
    public List<T> content() {
        return content;
    }

    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    public int numberOfElements() {
        return content.size();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return delegate.hasPrevious();
    }

    @Override
    public PageRequest pageRequest() {
        return delegate.pageRequest();
    }

    @Override
    public PageRequest nextPageRequest() {
        return delegate.nextPageRequest();
    }

    @Override
    public PageRequest previousPageRequest() {
        return delegate.previousPageRequest();
    }

    @Override
    public boolean hasTotals() {
        return delegate.hasTotals();
    }

    @Override
    public long totalElements() {
        return delegate.totalElements();
    }

    @Override
    public long totalPages() {
        return delegate.totalPages();
    }

    @Override
    public PageRequest.Cursor cursor(int index) {
        return delegate.cursor(index);
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}
