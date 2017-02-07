<?xml version="1.0" encoding="UTF-8"?>
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
    <xsl:output method="text"/>
    <xsl:template match="wbs[not(job)]">
        <xsl:text>It's empty, no jobs.</xsl:text>
    </xsl:template>
    <xsl:template match="wbs[job]">
        <xsl:apply-templates select="job"/>
    </xsl:template>
    <xsl:template match="job">
        <xsl:if test="position() &gt; 1">
            <xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:text>  * </xsl:text>
        <xsl:apply-templates select="@id"/>
        <xsl:if test="performer">
            <xsl:text>: [</xsl:text>
            <xsl:text>@</xsl:text>
            <xsl:value-of select="performer"/>
            <xsl:text>](https://github.com/</xsl:text>
            <xsl:value-of select="performer"/>
            <xsl:text>)</xsl:text>
        </xsl:if>
    </xsl:template>
    <xsl:template match="@id">
        <xsl:choose>
            <xsl:when test="starts-with(.,'gh:')">
                <xsl:variable name="repo" select="substring-before(substring-after(.,'gh:'),'#')"/>
                <xsl:variable name="issue" select="substring-after(.,'#')"/>
                <xsl:text>[</xsl:text>
                <xsl:value-of select="$repo"/>
                <xsl:text>#</xsl:text>
                <xsl:value-of select="$issue"/>
                <xsl:text>](https://github.com/</xsl:text>
                <xsl:value-of select="$repo"/>
                <xsl:text>/issues/</xsl:text>
                <xsl:value-of select="$issue"/>
                <xsl:text>)</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
