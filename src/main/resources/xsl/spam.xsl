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
      <xsl:text>Spam</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <h1>
      <xsl:text>Send spam</xsl:text>
    </h1>
    <p>
      <xsl:text>Be careful, this message will be delivered to all users!</xsl:text>
      <xsl:text> Please, make it short.</xsl:text>
      <xsl:text> The text will go through </xsl:text>
      <code>
        <xsl:text>Par</xsl:text>
      </code>
      <xsl:text>, you can use formatting.</xsl:text>
    </p>
    <form action="/spam-send" method="post">
      <fieldset>
        <label>Text:</label>
        <textarea tabindex="1" name="body" style="width:100%;height:4em;">
          <xsl:text>Text goes here...</xsl:text>
        </textarea>
        <button type="submit" tabindex="2">
          <xsl:text>Send</xsl:text>
        </button>
      </fieldset>
    </form>
  </xsl:template>
</xsl:stylesheet>
