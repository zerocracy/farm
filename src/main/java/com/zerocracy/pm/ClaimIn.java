/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.pm;

import com.jcabi.xml.XML;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Claim coming in.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 */
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

    /**
     * Reply to it.
     * @param msg The message to reply with
     * @return OutClaim
     */
    public ClaimOut reply(final String msg) {
        if (!this.hasToken()) {
            throw new IllegalArgumentException(
                String.format(
                    "There is no token in \"%s\", can't reply",
                    this.type()
                )
            );
        }
        return new ClaimOut(
            new ClaimOut.Notify(
                this.token(), msg
            )
        );
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
        if (this.hasAuthor()) {
            out.author(this.author());
        }
        out.params(this.params());
        return out;
    }

    /**
     * Get ID.
     * @return ID
     */
    public long number() {
        return Long.parseLong(this.xml.xpath("@id").get(0));
    }

    /**
     * Get type.
     * @return Type
     */
    public String type() {
        return this.xml.xpath("type/text()").get(0);
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
        return params.next();
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

}
