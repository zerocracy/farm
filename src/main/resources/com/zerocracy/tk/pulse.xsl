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
  <xsl:output method="xml" omit-xml-declaration="yes"/>
  <xsl:template match="pulse">
    <xsl:variable name="height" select="5"/>
    <xsl:variable name="width" select="3600000"/>
    <svg xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none" version="1.1" width="100%" height="100%">
      <xsl:attribute name="viewBox">
        <xsl:value-of select="-$width"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="0"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$width"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$height"/>
      </xsl:attribute>
      <defs>
        <style type="text/css">
          text {
            font-size: 1.5;
            font-family: monospace;
          }
        </style>
      </defs>
      <line x1="{-$width}" y1="{$height}" x2="0" y2="{$height}" stroke="lightgray" stroke-width="4px" vector-effect="non-scaling-stroke"/>
      <xsl:for-each select="tick">
        <rect height="{@total + 0.5}" x="{@start}" y="{$height - @total - 0.5}" fill="#348C62">
          <xsl:attribute name="width">
            <xsl:choose>
              <xsl:when test="@msec &lt; 5000">5000</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="@msec"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </rect>
      </xsl:for-each>
      <xsl:variable name="age" select="-number(tick[last()]/@start) div 1000"/>
      <text x="0" y="0" style="text-anchor:middle;" transform="scale(46000,1) translate(-39,1.5)">
        <xsl:choose>
          <xsl:when test="not($age) or $age &gt; 600">
            <tspan style="fill:red">
              <xsl:text>system outage :( click here</xsl:text>
            </tspan>
          </xsl:when>
          <xsl:when test="$age &gt; 240">
            <tspan style="fill:orange">
              <xsl:text>temporary out of service</xsl:text>
            </tspan>
          </xsl:when>
          <xsl:otherwise>
            <tspan style="fill:#348C62">
              <xsl:text>all systems work fine</xsl:text>
            </tspan>
          </xsl:otherwise>
        </xsl:choose>
      </text>
      <text x="0" y="0" style="text-anchor:end;" transform="scale(46000,1) translate(0,1.5)">
        <xsl:value-of select="format-number($age,'0')"/>
        <xsl:text> sec</xsl:text>
      </text>
    </svg>
  </xsl:template>
</xsl:stylesheet>
