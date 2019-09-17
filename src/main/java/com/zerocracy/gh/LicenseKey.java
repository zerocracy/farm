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
package com.zerocracy.gh;

import com.jcabi.github.Repo;
import java.io.IOException;
import org.cactoos.Text;

/**
 * License key from Github repo.
 * <p>
 * See <a href="https://help.github.com/en/articles/licensing-a-repository">
 * License keys</a> for more details.
 * </p>
 *
 * @since 1.0
 */
public final class LicenseKey implements Text {

    /**
     * Github repo.
     */
    private final Repo repo;

    /**
     * Ctor.
     * @param repo Repo
     */
    public LicenseKey(final Repo repo) {
        this.repo = repo;
    }

    @Override
    public String asString() throws IOException {
        return this.repo.json()
            .getJsonObject("license")
            .getString("key");
    }
}
