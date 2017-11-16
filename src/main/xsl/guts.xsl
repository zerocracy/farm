<?xml version="1.0"?>
<!--
Copyright (c) 2016-2017 Zerocracy

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
  <xsl:template match="/guts">
    <html>
      <head>
        <title>
          <xsl:text>guts</xsl:text>
        </title>
      </head>
      <body style="font-family:monospace">
        <xsl:for-each select="farm">
          <xsl:sort select="@id" order="ascending" data-type="text"/>
          <p>
            <strong>
              <xsl:value-of select="@id"/>
            </strong>
            <xsl:text>:</xsl:text>
          </p>
          <p>
            <xsl:apply-templates select="."/>
          </p>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="farm[@id='PropsFarm']">
    <xsl:text>See XML.</xsl:text>
  </xsl:template>
  <xsl:template match="farm[@id='RvFarm']">
    <xsl:text>Alive: </xsl:text>
    <xsl:value-of select="alive"/>
  </xsl:template>
</xsl:stylesheet>
