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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">
  <xsl:output method="html" doctype-system="about:legacy-compat" encoding="UTF-8" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:include href="/xsl/inner-layout.xsl"/>
  <xsl:template match="page" mode="head">
    <title>
      <xsl:value-of select="project"/>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <xsl:text>Footprint at </xsl:text>
      <a href="/p/{project}">
        <xsl:value-of select="project"/>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <xsl:apply-templates select="claims"/>
  </xsl:template>
  <xsl:template match="claims">
    <p>
      <xsl:text>Recent claims:</xsl:text>
    </p>
    <table>
      <thead>
        <tr>
          <th>
            <xsl:text>Created</xsl:text>
          </th>
          <th>
            <xsl:text>Type</xsl:text>
          </th>
          <th>
            <xsl:text>Details</xsl:text>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="claim"/>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template match="claim">
    <tr>
      <td>
        <xsl:value-of select="created"/>
      </td>
      <td>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="type"/>
        <xsl:text>"</xsl:text>
      </td>
      <td>
        <xsl:for-each select="*[not(name() = 'type') and not(name() = 'created') and not(name() = '_id') and not(name() = 'cid') and not(name() = 'project') and not(name() = 'closed')]">
          <xsl:if test="position() &gt; 1">
            <xsl:text>; </xsl:text>
          </xsl:if>
          <xsl:value-of select="name()"/>
          <xsl:text>:</xsl:text>
          <code>
            <xsl:value-of select="."/>
          </code>
        </xsl:for-each>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
