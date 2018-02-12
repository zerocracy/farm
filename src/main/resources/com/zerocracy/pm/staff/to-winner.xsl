<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:output method="xml"/>
  <xsl:variable name="election" select="/election"/>
  <xsl:variable name="table">
    <!--
        <user login="yegor256">
          <vote weight="12" points="0.4" score="1.3">text</vote>
          <vote weight="1" points="0.65" score="0.65">text</vote>
        </user>
        -->
    <xsl:for-each select="distinct-values($election/vote/person/@login)">
      <xsl:variable name="login" select="."/>
      <user login="{$login}">
        <xsl:for-each select="$election/vote">
          <vote weight="{@weight}" points="{person[@login=$login]/@points}" score="{person[@login=$login]/@points * @weight}">
            <xsl:value-of select="person[@login=$login]/text()"/>
          </vote>
        </xsl:for-each>
      </user>
    </xsl:for-each>
  </xsl:variable>
  <xsl:variable name="total">
    <!--
        <user login="yegor256" max="1.4" score="0.32"/>
        <user login="jeff" max="1.4" score="0.21"/>
        -->
    <xsl:for-each select="$table/user">
      <user login="{@login}" max="{sum(vote[@weight &gt; 0]/@weight)}" score="{sum(vote/@score)}"/>
    </xsl:for-each>
  </xsl:variable>
  <xsl:variable name="winner">
    <xsl:for-each select="$total/user[@score &gt;= 0]">
      <xsl:sort select="@score" order="descending" data-type="number"/>
      <xsl:if test="position()=1">
        <xsl:value-of select="@login"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>
  <xsl:template match="/election">
    <summary>
      <table>
        <xsl:apply-templates select="$table"/>
      </table>
      <total>
        <xsl:apply-templates select="$total"/>
      </total>
      <xsl:if test="$winner != ''">
        <winner>
          <xsl:value-of select="$winner"/>
        </winner>
        <reason>
          <xsl:for-each select="$table/user">
            <xsl:if test="position() &gt; 1">
              <xsl:text>
</xsl:text>
            </xsl:if>
            <xsl:variable name="login" select="@login"/>
            <xsl:text>@</xsl:text>
            <xsl:value-of select="@login"/>
            <xsl:text> (</xsl:text>
            <xsl:value-of select="format-number($total/user[@login=$login]/@score, '0.00')"/>
            <xsl:text> of </xsl:text>
            <xsl:value-of select="format-number($total/user[@login=$login]/@max, '0')"/>
            <xsl:text>):</xsl:text>
            <xsl:for-each select="vote[number(@weight) != 0]">
              <xsl:text>
</xsl:text>
              <xsl:text>  </xsl:text>
              <xsl:if test="@score &gt; 0">
                <xsl:text>+</xsl:text>
              </xsl:if>
              <xsl:value-of select="format-number(@score, '0.00')"/>
              <xsl:if test="number(@points) != number(@score)">
                <xsl:text>=</xsl:text>
                <xsl:value-of select="format-number(@points, '0.00')"/>
                <xsl:text>x</xsl:text>
                <xsl:value-of select="@weight"/>
              </xsl:if>
              <xsl:text> </xsl:text>
              <xsl:value-of select="text()"/>
            </xsl:for-each>
          </xsl:for-each>
        </reason>
      </xsl:if>
    </summary>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
