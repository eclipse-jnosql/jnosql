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

/**
 *This package contains all objects to use a Document Collection. This API has focus in domain, in other words,
 * ubiquitous language.
 *  A document-oriented database, or document store,
 * is a computer program designed for storing, retrieving, and managing document-oriented information,
 * also known as semi-structured data. Document-oriented databases are one of the main categories of NoSQL databases,
 * and the popularity of the term "document-oriented database" has grown with the use of the term NoSQL itself.
 * XML databases are a subclass of document-oriented databases that are optimized to work with XML documents.
 * Graph databases are similar, but add another layer, the relationship, which allows them to link documents
 * for rapid traversal.
 * Document-oriented databases are inherently a subclass of the key-value store, another NoSQL database concept.
 * The difference lies in the way the data is processed; in a key-value store the data is considered to be inherently
 * opaque to the database, whereas a document-oriented system relies on internal structure in the document
 * in order to extract
 * metadata that the database engine uses for further optimization. Although the difference is often moot due to tools
 * in the systems,
 * conceptually the document-store is designed to offer a richer experience with modern programming techniques.
 * Document databases contrast strongly with the traditional relational database (RDB).
 * Relational databases generally store data in separate tables that are defined by the programmer, and a single object
 * may be spread across several tables.
 * Document databases store all information for a given object in a single instance in the database, and every stored
 * object can be different from every other.
 * This makes mapping objects into the database a simple task, normally eliminating anything similar to an
 * object-relational mapping. These makes document stores attractive
 * for programming web applications, which are subject to continual change in place, and where speed of deployment
 * is an important issue.
 */
package org.eclipse.jnosql.mapping.document;