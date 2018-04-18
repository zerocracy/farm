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
      <xsl:text>Footprint @ </xsl:text>
      <xsl:value-of select="title"/>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <xsl:text>Footprint at </xsl:text>
      <a href="/p/{project}">
        <xsl:value-of select="title"/>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <form action="" method="get">
      <input tabindex="1" name="q" type="text" style="width:100%">
        <xsl:attribute name="placeholder">
          <xsl:text>{login:'yegor256', type:'User was banned'}</xsl:text>
        </xsl:attribute>
        <xsl:if test="query != '{}'">
          <xsl:attribute name="value">
            <xsl:value-of select="query"/>
          </xsl:attribute>
        </xsl:if>
      </input>
      <label style="font-size:80%;color:gray;">
        <xsl:text>This is JSON to query our MongoDB database of claims, see </xsl:text>
        <a href="https://docs.mongodb.com/manual/tutorial/query-documents/">
          <xsl:text>the manual</xsl:text>
        </a>
        <xsl:text>.</xsl:text>
      </label>
    </form>
    <xsl:if test="skip &gt; 0">
      <p>
        <xsl:text>Starts at claim #</xsl:text>
        <xsl:value-of select="skip + 1"/>
        <xsl:text>.</xsl:text>
      </p>
    </xsl:if>
    <xsl:apply-templates select="claims"/>
    <p>
      <xsl:if test="links/link[@rel='back']">
        <a href="{links/link[@rel='back']/@href}">
          <xsl:text>Back</xsl:text>
        </a>
      </xsl:if>
      <xsl:if test="links/link[@rel='next']">
        <xsl:text> </xsl:text>
        <a href="{links/link[@rel='next']/@href}">
          <xsl:text>Next</xsl:text>
        </a>
      </xsl:if>
    </p>
    <p>
      <xsl:text>Download as </xsl:text>
      <xsl:if test="links/link[@rel='plain']">
        <a href="{links/link[@rel='plain']/@href}">
          <xsl:text>text</xsl:text>
        </a>
      </xsl:if>
      <xsl:if test="links/link[@rel='json']">
        <xsl:text>, </xsl:text>
        <a href="{links/link[@rel='json']/@href}">
          <xsl:text>JSON</xsl:text>
        </a>
      </xsl:if>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="claims">
    <p>
      <xsl:choose>
        <xsl:when test="count(claim) &gt; 1">
          <xsl:text>Found </xsl:text>
          <xsl:value-of select="count(claim)"/>
          <xsl:text> claims (in reverse chronological order)</xsl:text>
        </xsl:when>
        <xsl:when test="count(claim) = 1">
          <xsl:text>Found one claim</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>No claims found</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>:</xsl:text>
    </p>
    <xsl:apply-templates select="claim"/>
  </xsl:template>
  <xsl:template match="claim">
    <p>
      <span style="display:block;">
        <a href="/footprint/{/page/project}/{cid}">
          <xsl:text>#</xsl:text>
          <xsl:value-of select="cid"/>
        </a>
        <xsl:text> </xsl:text>
        <span title="{created}">
          <xsl:value-of select="ago"/>
          <xsl:text> ago</xsl:text>
        </span>
        <xsl:text>: </xsl:text>
        <strong>
          <xsl:if test="type='Error'">
            <xsl:attribute name="style">
              <xsl:text>background-color:darkred;color:white;padding:0 .3em;</xsl:text>
            </xsl:attribute>
          </xsl:if>
          <xsl:value-of select="type"/>
        </strong>
        <xsl:text>/</xsl:text>
        <xsl:choose>
          <xsl:when test="contains(version,'SNAPSHOT')">
            <span title="The bot was deployed manually, without any specific version">
              <xsl:text>&#x26A1;</xsl:text>
            </span>
          </xsl:when>
          <xsl:otherwise>
            <span title="The version of the bot">
              <xsl:value-of select="version"/>
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </span>
      <xsl:for-each select="*[not(name() = 'type') and not(name() = 'version') and not(name() = 'created') and not(name() = '_id') and not(name() = 'cid') and not(name() = 'project') and not(name() = 'closed') and not(name() = 'cause') and not(name() = 'ago')]">
        <xsl:if test="position() &gt; 1">
          <xsl:text> </xsl:text>
        </xsl:if>
        <xsl:value-of select="name()"/>
        <xsl:text>:</xsl:text>
        <code>
          <xsl:choose>
            <xsl:when test="string-length(.) &gt; 64">
              <xsl:text>...</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
        </code>
      </xsl:for-each>
    </p>
  </xsl:template>
</xsl:stylesheet>
