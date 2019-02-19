<?xml version="1.0"?>
<!--
Copyright (c) 2016-2019 Zerocracy

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
  <xsl:template match="/guts">
    <html>
      <head>
        <title>
          <xsl:text>guts</xsl:text>
        </title>
      </head>
      <body style="font-family:monospace">
        <p>
          <img src="/svg/logo.svg" style="height:64px"/>
        </p>
        <p style="color:red">
          <xsl:text>Restricted area, be careful!</xsl:text>
        </p>
        <xsl:apply-templates select="jvm"/>
        <xsl:for-each select="farm">
          <xsl:sort select="@id" order="ascending" data-type="text"/>
          <p style="margin-top:2em">
            <strong>
              <xsl:value-of select="@id"/>
            </strong>
            <xsl:text>:</xsl:text>
          </p>
          <xsl:apply-templates select="."/>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="jvm">
    <p>
      <xsl:text>JVM: </xsl:text>
      <xsl:for-each select="attrs/attr">
        <xsl:sort select="@id"/>
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="@id"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="."/>
      </xsl:for-each>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Threads (</xsl:text>
      <xsl:value-of select="count(threads/thread)"/>
      <xsl:text>): </xsl:text>
      <xsl:for-each select="threads/thread">
        <xsl:sort select="@id"/>
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="@id"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="@state"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="@daemon"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="@alive"/>
      </xsl:for-each>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="farm[@id='PropsFarm']">
    <p>
      <xsl:text>See XML.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="farm[@id='ExtFarm']">
    <p>
      <xsl:text>Quota: </xsl:text>
      <xsl:value-of select="quota"/>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="farm[@id='ClaimsFarm']">
    <p>
      <xsl:text>Claim queues: </xsl:text>
      <ul>
        <xsl:for-each select="queues/queue">
          <li>
            <xsl:for-each select="message">
              <xsl:if test="position() &gt; 1">
                <xsl:text>, </xsl:text>
              </xsl:if>
              <xsl:text>[</xsl:text>
              <xsl:value-of select="@id"/>
              <xsl:text> claim={</xsl:text>
              <xsl:value-of select="claim"/>
              <xsl:text>}, pkt={</xsl:text>
              <xsl:value-of select="project"/>
              <xsl:text>}, received={</xsl:text>
              <xsl:value-of select="received"/>
              <xsl:if test="expires">
                <xsl:text>}, expires={</xsl:text>
                <xsl:value-of select="expires"/>
              </xsl:if>
              <xsl:text>}, priority={</xsl:text>
              <xsl:value-of select="priority"/>
              <xsl:text>}]</xsl:text>
            </xsl:for-each>
          </li>
        </xsl:for-each>
      </ul>
    </p>
  </xsl:template>
  <xsl:template match="farm[@id='RvFarm']">
    <p>
      <xsl:text>Alive (</xsl:text>
      <xsl:value-of select="sum(alive/count)"/>
      <xsl:text>): </xsl:text>
      <xsl:if test="not(alive/count)">
        <xsl:text>-</xsl:text>
      </xsl:if>
      <xsl:for-each select="alive/count">
        <xsl:sort select="@pid"/>
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="@pid"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="."/>
      </xsl:for-each>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Locks: </xsl:text>
      <xsl:if test="not(locks/lock)">
        <xsl:text>-</xsl:text>
      </xsl:if>
      <xsl:for-each select="locks/lock">
        <xsl:sort select="@pid"/>
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="@pid"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="."/>
      </xsl:for-each>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="farm[@id='SyncFarm']">
    <p>
      <xsl:text>Locks: </xsl:text>
      <xsl:if test="not(locks/lock)">
        <xsl:text>-</xsl:text>
      </xsl:if>
      <xsl:for-each select="locks/lock">
        <xsl:sort select="@pid"/>
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="@pid"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="@label"/>
      </xsl:for-each>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Terminator killers: </xsl:text>
      <xsl:if test="not(terminator/killer)">
        <xsl:text>-</xsl:text>
      </xsl:if>
      <xsl:for-each select="terminator/killer">
        <xsl:sort select="@pid"/>
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="@pid"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="."/>
      </xsl:for-each>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
</xsl:stylesheet>
