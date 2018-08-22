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
  <xsl:include href="/org/takes/facets/flash/flash.xsl"/>
  <xsl:template match="/page">
    <html lang="en">
      <head>
        <meta charset="utf-8"/>
        <meta name="description" content="Zerocrat is a project manager that never sleeps"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <link rel="shortcut icon" href="/png/logo.png"/>
        <link rel="stylesheet" href="/css/main.css"/>
        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/sortable/0.8.0/js/sortable.min.js">&#xA0;</script>
        <xsl:apply-templates select="." mode="head"/>
      </head>
      <body>
        <section>
          <xsl:if test="flash">
            <xsl:call-template name="takes_flash_without_escaping">
              <xsl:with-param name="flash" select="flash"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:apply-templates select="." mode="body"/>
        </section>
      </body>
    </html>
  </xsl:template>
  <xsl:template name="takes_flash_without_escaping">
    <xsl:param name="flash"/>
    <xsl:if test="$flash/message">
      <p>
        <xsl:attribute name="class">
          <xsl:text>flash</xsl:text>
          <xsl:text> flash-</xsl:text>
          <xsl:value-of select="$flash/level"/>
        </xsl:attribute>
        <xsl:value-of select="$flash/message" disable-output-escaping="yes"/>
      </p>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
