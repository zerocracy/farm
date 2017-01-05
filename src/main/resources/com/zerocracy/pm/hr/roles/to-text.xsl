<?xml version="1.0" encoding="UTF-8"?>
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
    <xsl:output method="text"/>
    <xsl:template match="/roles[not(person)]">
        <xsl:text>No roles assigned.</xsl:text>
    </xsl:template>
    <xsl:template match="/roles[person]">
        <xsl:apply-templates select="person"/>
    </xsl:template>
    <xsl:template match="person">
        <xsl:if test="position() &gt; 1">
            <xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:value-of select="@id"/>
        <xsl:text>: </xsl:text>
        <xsl:apply-templates select="role"/>
    </xsl:template>
    <xsl:template match="role">
        <xsl:if test="position() &gt; 1">
            <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="."/>
    </xsl:template>
</xsl:stylesheet>
