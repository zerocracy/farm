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
package com.zerocracy.claims;

import com.jcabi.xml.XML;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.cactoos.text.UncheckedText;
import org.cactoos.time.DateOf;

/**
 * Claim coming in.
 *
 * <p>This class helps you parse the incoming XML claim and take
 * its keep components out. Use it everywhere. Don't parse the XML
 * manually.</p>
 *
 * @since 1.0
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class ClaimIn {

    /**
     * XML.
     */
    private final XML xml;

    /**
     * Ctor.
     * @param input Input XML
     */
    public ClaimIn(final XML input) {
        this.xml = input;
    }

    @Override
    public String toString() {
        return this.xml.toString();
    }

    /**
     * Reply to it.
     * @param msg The message to reply with
     * @return OutClaim
     */
    public ClaimOut reply(final String msg) {
        final ClaimOut out;
        if (this.hasToken()) {
            out = this.copy()
                .type("Notify")
                .token(this.token())
                .param("message", msg);
        } else {
            out = this.copy()
                .type("Ignore me")
                .param("message", msg);
        }
        return out;
    }

    /**
     * Make a copy.
     * @return OutClaim
     */
    public ClaimOut copy() {
        final ClaimOut out = new ClaimOut();
        out.type(this.type());
        if (this.hasToken()) {
            out.token(this.token());
        }
        out.params(this.params());
        out.param("cause", this.cid());
        out.param(
            "_parent_sig",
            new UncheckedText(new ClaimSignature(this.xml)).asString()
        );
        final StringBuilder flow = new StringBuilder("");
        if (this.params().containsKey("flow")) {
            flow.append(this.param("flow")).append("; ");
        }
        flow.append(this.type());
        out.param("flow", flow.toString());
        return out;
    }

    /**
     * Get ID.
     * @return ID
     */
    public String cid() {
        return this.xml.xpath("@id").get(0);
    }

    /**
     * Created.
     * @return Date when it was created
     */
    public Date created() {
        return new DateOf(
            this.xml.xpath("created/text()").get(0)
        ).value();
    }

    /**
     * Get type.
     * @return Type
     */
    public String type() {
        return this.xml.xpath("type/text()").get(0);
    }

    /**
     * Is this claim error.
     * @return True if error
     */
    public boolean isError() {
        return "Error".equals(this.type());
    }

    /**
     * Get token.
     * @return Token
     */
    public String token() {
        return this.xml.xpath("token/text()").get(0);
    }

    /**
     * Get author.
     * @return Author
     */
    public String author() {
        return this.xml.xpath("author/text()").get(0);
    }

    /**
     * Token exists.
     * @return TRUE if token exists
     */
    public boolean hasToken() {
        return !this.xml.nodes("token").isEmpty();
    }

    /**
     * Author exists.
     * @return TRUE if author exists
     */
    public boolean hasAuthor() {
        return !this.xml.nodes("author").isEmpty();
    }

    /**
     * Has param by name.
     * @param name Param name
     * @return TRUE if exists
     */
    public boolean hasParam(final String name) {
        return this.xml.xpath(
            String.format(
                "params/param[ @name='%s']/text()",
                name
            )
        ).iterator().hasNext();
    }

    /**
     * Get param by name.
     * @param name Param name
     * @return Param value
     */
    public String param(final String name) {
        final Iterator<String> params = this.xml.xpath(
            String.format(
                "params/param[@name='%s']/text()",
                name
            )
        ).iterator();
        if (!params.hasNext()) {
            throw new IllegalArgumentException(
                String.format(
                    "Parameter \"%s\" not found in \"%s\" among: %s",
                    name, this.type(),
                    this.xml.xpath("params/param/@name")
                )
            );
        }
        String value = params.next();
        if ("login".equals(name)) {
            value = value.toLowerCase(Locale.ENGLISH);
        }
        return value;
    }

    /**
     * Get all params.
     * @return All params
     */
    public Map<String, String> params() {
        final Map<String, String> map = new HashMap<>(0);
        for (final XML param : this.xml.nodes("params/param")) {
            map.put(
                param.xpath("@name").get(0),
                param.xpath("text()").get(0)
            );
        }
        return map;
    }

    /**
     * Has unique value.
     *
     * @return TRUE if has
     */
    public boolean isUnique() {
        return !this.xml.nodes("unique").isEmpty();
    }

    /**
     * Get unique source (see {@link ClaimOut#unique(String)}).
     *
     * @return Unique source
     */
    public String unique() {
        return this.xml.xpath("unique/text()").get(0);
    }
}
