<?xml version="1.0"?>
<!--
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
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" version="1.0">
    <xsl:output method="html" doctype-system="about:legacy-compat"
        encoding="UTF-8" indent="yes" />
    <xsl:strip-space elements="*"/>
    <xsl:include href="/xsl/layout.xsl"/>
    <xsl:template match="page" mode="head">
        <title>
            <xsl:text>0crat</xsl:text>
        </title>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <div class="center">
            <p>
                <img src="http://www.zerocracy.com/logo.svg" class="logo" />
            </p>
            <p>
                <xsl:value-of select="version/name"/>
                <xsl:text> | </xsl:text>
                <xsl:value-of select="alive"/>
            </p>
            <p>
                <xsl:if test="identity">
                    <xsl:text>@</xsl:text>
                    <xsl:value-of select="identity/login"/>
                    <xsl:text> | </xsl:text>
                    <a href="{links/link[@rel='takes:logout']/@href}">
                        <xsl:text>exit</xsl:text>
                    </a>
                </xsl:if>
                <xsl:if test="not(identity)">
                    <a href="{links/link[@rel='takes:github']/@href}">
                        <xsl:text>login</xsl:text>
                    </a>
                </xsl:if>
            </p>
            <p>
                <xsl:call-template name="millis">
                    <xsl:with-param name="millis" select="millis"/>
                </xsl:call-template>
                <xsl:text> | </xsl:text>
                <xsl:value-of select="@sla"/>
            </p>
        </div>
    </xsl:template>
</xsl:stylesheet>
