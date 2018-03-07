<?xml version="1.0"?>
<!--
Copyright (c) 2016-2018 Zerocracy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to read
the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="2.0">
  <xsl:output method="text" encoding="UTF-8"/>
  <xsl:template match="footprint">
    <xsl:apply-templates select="claims"/>
  </xsl:template>
  <xsl:template match="claims">
    <xsl:text>[</xsl:text>
    <xsl:text>
</xsl:text>
    <xsl:apply-templates select="claim"/>
    <xsl:text>
</xsl:text>
    <xsl:text>]</xsl:text>
    <xsl:text>
</xsl:text>
  </xsl:template>
  <xsl:template match="claim">
    <xsl:if test="position() &gt; 1">
      <xsl:text>,
</xsl:text>
    </xsl:if>
    <xsl:text>{
  "cid": </xsl:text>
    <xsl:value-of select="cid"/>
    <xsl:text>,
  "ago": "</xsl:text>
    <xsl:value-of select="ago"/>
    <xsl:text>",
  "type": "</xsl:text>
    <xsl:value-of select="type"/>
    <xsl:text>",
  "version": "</xsl:text>
    <xsl:value-of select="version"/>
    <xsl:text>",
  "params": {
</xsl:text>
    <xsl:for-each select="*[not(name() = 'type') and not(name() = 'version') and not(name() = 'created') and not(name() = '_id') and not(name() = 'cid') and not(name() = 'project') and not(name() = 'closed') and not(name() = 'cause') and not(name() = 'ago')]">
      <xsl:if test="position() &gt; 1">
        <xsl:text>,
</xsl:text>
      </xsl:if>
      <xsl:text>    "</xsl:text>
      <xsl:value-of select="name()"/>
      <xsl:text>": "</xsl:text>
      <xsl:value-of select="replace(.,'&quot;', '\&quot;')"/>
      <xsl:text>"</xsl:text>
    </xsl:for-each>
    <xsl:text>}
  }</xsl:text>
  </xsl:template>
</xsl:stylesheet>
