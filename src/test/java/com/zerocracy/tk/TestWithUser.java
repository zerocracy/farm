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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import org.junit.Before;

/**
 * Base test case with initialized User.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class TestWithUser {

    /**
     * Farm to use.
     * @checkstyle VisibilityModifierCheck (2 lines)
     */
    protected final Farm farm;

    protected TestWithUser() {
        this.farm = new PropsFarm(new FkFarm());
    }

    @Before
    public final void init() throws Exception {
        final String uid = "yegor256";
        final Catalog catalog = new Catalog(new Pmo(this.farm)).bootstrap();
        final String pid = "C00000000";
        catalog.add(pid, String.format("2017/07/%s/", pid));
        catalog.link(pid, "github", "test/test");
        new Roles(
            this.farm.find(String.format("@id='%s'", pid)).iterator().next()
        ).bootstrap().assign(uid, "PO");
        new People(new Pmo(this.farm)).bootstrap().invite(uid, "mentor");
    }
}
