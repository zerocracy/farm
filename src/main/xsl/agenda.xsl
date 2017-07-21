<?xml version="1.0"?>
<!--
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
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" version="1.0">
    <xsl:output method="html" doctype-system="about:legacy-compat"
        encoding="UTF-8" indent="yes" />
    <xsl:strip-space elements="*"/>
    <xsl:include href="/xsl/layout.xsl"/>
    <xsl:template match="page" mode="head">
        <title>
            <xsl:text>agenda</xsl:text>
        </title>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <p>
            <xsl:text>@</xsl:text>
            <xsl:value-of select="identity/login"/>
        </p>
        <xsl:if test="agenda/job">
            <p>
                <xsl:text>There are </xsl:text>
                <xsl:value-of select="count(agenda/job)"/>
                <xsl:text> jobs assigned to you at the moment:</xsl:text>
            </p>
            <pre>
                <xsl:for-each select="agenda/job">
                    <xsl:if test="position() &gt; 1">
                        <xsl:text>&#10;</xsl:text>
                    </xsl:if>
                    <xsl:value-of select="."/>
                </xsl:for-each>
            </pre>
        </xsl:if>
        <xsl:if test="not(agenda/job)">
            <p>
                <xsl:text>There are no jobs for you at the moment.</xsl:text>
            </p>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
