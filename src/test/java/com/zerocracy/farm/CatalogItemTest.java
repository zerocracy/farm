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
package com.zerocracy.farm;

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.fake.FkItem;
import com.zerocracy.pm.Xocument;
import com.zerocracy.pmo.Catalog;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link CatalogItem}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class CatalogItemTest {

    /**
     * CatalogItem can modify a project.
     * @throws Exception If some problem inside
     */
    @Test
    public void modifiesProject() throws Exception {
        final Item item = new FkItem();
        final String pid = "34EA54SZZ";
        try (final Catalog catalog = new Catalog(item)) {
            catalog.bootstrap();
            catalog.add("34EA54SW9");
            catalog.add("34EA54S87");
            catalog.add(pid);
            catalog.add("34EA54S8F");
        }
        try (final Item citem = new CatalogItem(
            item, String.format("//project[@id!='%s']", pid)
        )) {
            new Xocument(citem.path()).modify(
                new Directives()
                    .xpath("/catalog/project")
                    .add("link")
                    .attr("rel", "github")
                    .attr("href", "yegor256/pdd")
            );
        }
        try (final Item citem = new CatalogItem(
            item, String.format("//project[@id!='%s' ]", pid)
        )) {
            MatcherAssert.assertThat(
                new Xocument(citem.path()).xpath(
                    "//project/link[@rel]/@href"
                ),
                Matchers.not(Matchers.emptyIterable())
            );
        }
        MatcherAssert.assertThat(
            new Xocument(item.path()).xpath(
                "//project[not(link)]/@id"
            ),
            Matchers.not(Matchers.emptyIterable())
        );
    }

}
