/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.method;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * This class represents a method query tokenizer. It processes query strings,
 * breaking them into tokens based on predefined patterns and applying specific
 * logic to handle the "First" keyword based on its position.
 * The class also caches processed query strings to optimize repeated queries.
 * It implements the Supplier interface to provide the processed query string
 * when needed.
 */
public final class QueryTokenizer implements Supplier<String> {

    /**
     * The limit to the first keyword, after this limit the first keyword will be ignored
     */
    private static final int LIMIT_FIRST = 10;

    private final String value;

    private static final Pattern TOKENIZER_PATTERN = Pattern.compile(
            "countAll|"
                    + "find|delete|count|exists|"
                    + "By(?=[A-Z]|$)|"
                    + "OrderBy|"
                    + "First(?=\\d*By)|First|"
                    + "And|Or(?!der)|"
                    + "IgnoreCase|"
                    + "Null|Not|Equals|"
                    + "GreaterThanEqual|LessThanEqual|"
                    + "GreaterThan|LessThan|"
                    + "Between|In|Like|"
                    + "Contains|StartsWith|EndsWith|"
                    + "True|False|"
                    + "Asc|Desc"
    );
    private static final Map<String, String> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private QueryTokenizer(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryTokenizer that = (QueryTokenizer) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static QueryTokenizer of(String query) {
        Objects.requireNonNull(query, "query is required");
        return new QueryTokenizer(CACHE.computeIfAbsent(query, q -> {
            var tokenized = TOKENIZER_PATTERN.matcher(q).replaceAll(" $0 ").trim().replaceAll("\\s+", " ");
            tokenized = adjustFirstKeywordPosition(tokenized);
            return processOrderBy(tokenized);
        }));
    }

    private static String adjustFirstKeywordPosition(String query) {
        StringBuilder result = new StringBuilder();
        int currentPosition = 0;
        for (String token : query.split(" ")) {
            if (currentPosition >= LIMIT_FIRST && token.equals("First")) {
                result.append(token);
            } else {
                result.append(" ").append(token);
            }
            currentPosition += token.length();
        }
        return result.toString().trim();
    }

    private static String processOrderBy(String query) {
        StringBuilder result = new StringBuilder();
        String[] tokens = query.split(" ");
        boolean afterOrderBy = false;

        for (int index = 0; index < tokens.length; index++) {
            String token = tokens[index];

            if (token.equals("OrderBy")) {
                afterOrderBy = true;
                result.append(token).append(" ");
            } else if (afterOrderBy) {
                // Only separate Asc and Desc, keep everything else intact
                if (token.equals("Asc") || token.equals("Desc")) {
                    result.append(token).append(" ");
                } else {
                    // Combine all tokens until "Asc" or "Desc"
                    while (index < tokens.length && !tokens[index].equals("Asc") && !tokens[index].equals("Desc")) {
                        result.append(tokens[index]);
                        index++;
                    }
                    // Add the final Asc or Desc if present
                    if (index < tokens.length) {
                        result.append(" ").append(tokens[index]).append(" ");
                    }
                }
                afterOrderBy = false; // Processed the relevant tokens after OrderBy
            } else {
                result.append(token).append(" ");
            }
        }

        return result.toString().trim();
    }
}
