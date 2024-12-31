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
 *   Jesse Gallagher
 */
package org.eclipse.jnosql.mapping.reflection.entities;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.nosql.Column;

/**
 * This class is intended to test the behavior of Map- and
 * Collection-compatible members where the type does not
 * directly contain the generic parameters.
 */
public class JsonContainer {
	public interface JsonObjectChild extends JsonObject {}
	public static abstract class JsonArrayChild implements JsonArray {}
	
	@Column
	private JsonObject body;
	@Column
	private JsonArray tags;
	@Column
	private JsonObjectChild childBody;
	@Column
	private JsonArrayChild childTags;
}
