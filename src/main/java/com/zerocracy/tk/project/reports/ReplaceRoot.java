/*
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.tk.project.reports;

import com.mongodb.client.model.Aggregates;
import org.bson.BsonDocument;
import org.bson.BsonElement;
import org.bson.BsonString;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.cactoos.list.Mapped;
import org.cactoos.list.SolidList;

/**
 * ReplaceRoot for Mongo.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReplaceRoot implements Bson {

    /**
     * List of fields.
     */
    private final Iterable<String> fields;

    /**
     * Ctor.
     * @param list List of fields
     */
    public ReplaceRoot(final String... list) {
        this(new SolidList<>(list));
    }

    /**
     * Ctor.
     * @param list List of fields
     */
    public ReplaceRoot(final Iterable<String> list) {
        this.fields = list;
    }

    @Override
    public <T> BsonDocument toBsonDocument(final Class<T> type,
        final CodecRegistry reg) {
        return Aggregates.replaceRoot(
            new BsonDocument(
                new Mapped<String, BsonElement>(
                    field -> new BsonElement(
                        field,
                        new BsonString(String.format("$%s", field))
                    ),
                    new SolidList<>(this.fields)
                )
            )
        ).toBsonDocument(type, reg);
    }
}
