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
  <xsl:output method="html" doctype-system="about:legacy-compat" encoding="UTF-8" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:include href="/xsl/inner-layout.xsl"/>
  <xsl:template match="page" mode="head">
    <title>
      <xsl:text>#</xsl:text>
      <xsl:value-of select="claim/cid"/>
      <xsl:text> @ </xsl:text>
      <xsl:value-of select="project"/>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <xsl:text>Back to </xsl:text>
      <a href="/footprint/{project}">
        <xsl:text>Footprint</xsl:text>
      </a>
      <xsl:text> of </xsl:text>
      <a href="/p/{project}">
        <xsl:value-of select="project"/>
      </a>
    </p>
    <xsl:apply-templates select="claim"/>
    <xsl:apply-templates select="children"/>
  </xsl:template>
  <xsl:template match="claim">
    <h1>
      <xsl:text>Claim #</xsl:text>
      <xsl:value-of select="cid"/>
    </h1>
    <p>
      <xsl:text>type: </xsl:text>
      <code>
        <xsl:value-of select="type"/>
      </code>
    </p>
    <p>
      <xsl:text>created: </xsl:text>
      <xsl:value-of select="created"/>
    </p>
    <p>
      <xsl:text>author: </xsl:text>
      <xsl:choose>
        <xsl:when test="author">
          <a href="/u/{author}">
            <xsl:text>@</xsl:text>
            <xsl:value-of select="author"/>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>&#x2014;</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </p>
    <p>
      <xsl:text>token: </xsl:text>
      <xsl:choose>
        <xsl:when test="token">
          <code>
            <xsl:value-of select="token"/>
          </code>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>&#x2014;</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </p>
    <xsl:if test="cause">
      <p>
        <xsl:text>cause: </xsl:text>
        <a href="/footprint/{/page/project}/{cause}">
          <xsl:value-of select="cause"/>
        </a>
      </p>
    </xsl:if>
    <xsl:for-each select="*[not(name() = 'type') and not(name() = 'token') and not(name() = 'created') and not(name() = '_id') and not(name() = 'cid') and not(name() = 'cause') and not(name() = 'project') and not(name() = 'closed')]">
      <p>
        <xsl:value-of select="name()"/>
        <xsl:text>:</xsl:text>
      </p>
      <pre>
        <xsl:value-of select="."/>
      </pre>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="children">
    <xsl:apply-templates select="child"/>
  </xsl:template>
  <xsl:template match="child">
    <p>
      <a href="/footprint/{/page/project}/{cid}">
        <xsl:value-of select="cid"/>
      </a>
      <xsl:text>: </xsl:text>
      <xsl:value-of select="type"/>
    </p>
  </xsl:template>
</xsl:stylesheet>
