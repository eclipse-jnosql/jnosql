/*
 *
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
 *
 */
module org.eclipse.jnosql.communication.core {
    requires microprofile.config.api;
    requires jakarta.json;
    exports org.eclipse.jnosql.communication;
    opens org.eclipse.jnosql.communication;
    opens org.eclipse.jnosql.communication.reader;
    opens org.eclipse.jnosql.communication.writer;
    uses org.eclipse.jnosql.communication.TypeReferenceReader;
    uses org.eclipse.jnosql.communication.ValueReader;
    uses org.eclipse.jnosql.communication.ValueWriter;
    provides org.eclipse.jnosql.communication.TypeReferenceReader with org.eclipse.jnosql.communication.reader.ListTypeReferenceReader,
            org.eclipse.jnosql.communication.reader.SetTypeReferenceReader,
            org.eclipse.jnosql.communication.reader.MapTypeReferenceReader,
            org.eclipse.jnosql.communication.reader.StreamTypeReferenceReader,
            org.eclipse.jnosql.communication.reader.OptionalTypeReferenceReader,
            org.eclipse.jnosql.communication.reader.QueueTypeReferenceReader,
            org.eclipse.jnosql.communication.reader.NavigableSetTypeReferenceReader;
    provides org.eclipse.jnosql.communication.ValueReader with org.eclipse.jnosql.communication.reader.AtomicIntegerReader,
            org.eclipse.jnosql.communication.reader.AtomicLongReader,
            org.eclipse.jnosql.communication.reader.BigDecimalReader,
            org.eclipse.jnosql.communication.reader.BigIntegerReader,
            org.eclipse.jnosql.communication.reader.BooleanReader,
            org.eclipse.jnosql.communication.reader.ByteReader,
            org.eclipse.jnosql.communication.reader.CalendarReader,
            org.eclipse.jnosql.communication.reader.CharacterReader,
            org.eclipse.jnosql.communication.reader.DoubleReader,
            org.eclipse.jnosql.communication.reader.EnumReader,
            org.eclipse.jnosql.communication.reader.FloatReader,
            org.eclipse.jnosql.communication.reader.IntegerReader,
            org.eclipse.jnosql.communication.reader.LocalDateReader,
            org.eclipse.jnosql.communication.reader.LocalDateTimeReader,
            org.eclipse.jnosql.communication.reader.LongReader,
            org.eclipse.jnosql.communication.reader.NumberReader,
            org.eclipse.jnosql.communication.reader.OptionalReader,
            org.eclipse.jnosql.communication.reader.ShortReader,
            org.eclipse.jnosql.communication.reader.StringReader,
            org.eclipse.jnosql.communication.reader.YearMonthReader,
            org.eclipse.jnosql.communication.reader.YearReader,
            org.eclipse.jnosql.communication.reader.ZonedDateTimeReader,
            org.eclipse.jnosql.communication.reader.LocalTimeReader,
            org.eclipse.jnosql.communication.reader.OffsetDateTimeReader,
            org.eclipse.jnosql.communication.reader.OffsetTimeReader,
            org.eclipse.jnosql.communication.reader.ArrayReader;
    provides org.eclipse.jnosql.communication.ValueWriter with org.eclipse.jnosql.communication.writer.EnumValueWriter,
            org.eclipse.jnosql.communication.writer.OptionalValueWriter,
            org.eclipse.jnosql.communication.writer.TemporalValueWriter;


}