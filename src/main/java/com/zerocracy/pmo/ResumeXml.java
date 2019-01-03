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
package com.zerocracy.pmo;

import com.jcabi.xml.XML;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Resume stored as xml.
 *
 * <p>This class helps you parse the resume XML and take.
 * Use it everywhere. Don't parse the XML manually.</p>
 *
 * @since 1.0
 */
public final class ResumeXml implements Resume {

    /**
     * XML.
     */
    private final XML xml;

    /**
     * Ctor.
     * @param input Input XML
     */
    public ResumeXml(final XML input) {
        this.xml = input;
    }

    @Override
    public String toString() {
        return this.xml.toString();
    }

    @Override
    public Instant submitted() {
        return LocalDateTime.parse(
            this.xml.xpath("submitted/text()").get(0),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        ).toInstant(ZoneOffset.UTC);
    }

    @Override
    public String login() {
        return this.xml.xpath("@login").get(0);
    }

    @Override
    public String text() {
        return this.xml.xpath("text/text()").get(0);
    }

    @Override
    public String personality() {
        return this.xml.xpath("personality/text()").get(0);
    }

    @Override
    public long soid() {
        return Long.parseLong(this.xml.xpath("stackoverflow/text()").get(0));
    }

    @Override
    public String telegram() {
        return this.xml.xpath("telegram/text()").get(0);
    }
}
