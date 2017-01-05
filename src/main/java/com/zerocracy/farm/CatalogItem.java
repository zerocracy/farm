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

import com.jcabi.s3.Ocket;
import com.jcabi.xml.FileSources;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.pm.Xocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.xembly.Directives;

/**
 * Catalog item.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class CatalogItem implements Item {

    /**
     * XSL to apply changes.
     */
    private static final XSL EXTRA = XSLDocument.make(
        CatalogItem.class.getResourceAsStream("extra.xsl")
    );

    /**
     * Item.
     */
    private final Item item;

    /**
     * XPath query.
     */
    private final String xpath;

    /**
     * Temp catalog.
     */
    private final Item temp;

    /**
     * Ctor.
     * @param itm Item
     * @param path XPath of projects to DELETE
     */
    CatalogItem(final Item itm, final String path) {
        this.item = itm;
        this.xpath = path;
        this.temp = new S3Item(new Ocket.Empty());
    }

    @Override
    public Path path() throws IOException {
        final Path path = this.item.path();
        if (this.temp.path().toFile().length() == 0L) {
            Files.copy(
                path, this.temp.path(),
                StandardCopyOption.REPLACE_EXISTING
            );
            new Xocument(path).modify(
                new Directives().xpath(this.xpath).remove()
            );
        }
        return path;
    }

    @Override
    public void close() throws IOException {
        new Xocument(this.temp.path()).apply(
            CatalogItem.EXTRA
                .with(new FileSources())
                .with("path", this.item.path().toAbsolutePath())
        );
        Files.copy(
            this.temp.path(), this.item.path(),
            StandardCopyOption.REPLACE_EXISTING
        );
        this.item.close();
        this.temp.close();
    }

}
