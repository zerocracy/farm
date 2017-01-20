/**
 * Copyright (c) 2016 Zerocracy
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
        return new ClaimOut()
            .type("notify")
            .token(this.token())
            .param("message", msg);
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
     * Get param by name.
     * @param name Param name
     * @return Param value
     */
    public String param(final String name) {
        return this.xml.xpath(
            String.format(
                "params/param[@name='%s']/text()",
                name
            )
        ).get(0);
    }

}
