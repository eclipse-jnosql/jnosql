/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

import java.util.Map;
import java.util.Objects;

@Embeddable
public class Program {

    @Id
    private String name;

    @Column
    private Map<String, String> socialMedia;

    public String getName() {
        return name;
    }

    public Map<String, String> getSocialMedia() {
        return socialMedia;
    }

    private Program(String name, Map<String, String> socialMedia) {
        this.name = name;
        this.socialMedia = socialMedia;
    }


    @Deprecated
    Program() {
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Program program = (Program) o;
        return Objects.equals(name, program.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "Program{" +
                "name='" + name + '\'' +
                ", socialMedia=" + socialMedia +
                '}';
    }

    public static Program of(String name, Map<String, String> socialMedia) {
        return new Program(name, socialMedia);
    }
}