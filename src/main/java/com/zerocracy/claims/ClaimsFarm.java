/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.farm.guts.Guts;
import java.io.IOException;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Farm with claim queue details.
 *
 * @since 1.0
 */
public final class ClaimsFarm implements Farm {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Claims.
     */
    private final Iterable<Directive> claims;

    /**
     * Ctor.
     *
     * @param farm Origin farm
     * @param claims Claims details
     */
    public ClaimsFarm(final Farm farm, final Iterable<Directive> claims) {
        this.farm = farm;
        this.claims = claims;
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        return new Guts(
            this.farm,
            () -> this.farm.find(xpath),
            () -> new Directives()
                .xpath("/guts")
                .add("farm")
                .attr("id", this.getClass().getSimpleName())
                .append(this.claims)
        ).apply(xpath);
    }
}
