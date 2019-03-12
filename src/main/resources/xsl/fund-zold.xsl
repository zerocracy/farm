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
  <xsl:include href="/xsl/inner-layout.xsl"/>
  <xsl:template match="page" mode="head">
    <title>
      <xsl:text>Fund with Zold</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <h1>Fund project with Zold</h1>
    <p>
      <xsl:text>To fund project </xsl:text>
      <code>
        <xsl:value-of select="project"/>
      </code>
      <xsl:text> with </xsl:text>
      <a href="https://wts.zold.io/">Zold</a>
      <xsl:text> send any amount of </xsl:text>
      <code>ZLD</code>
      <xsl:text> from your wallet to the invoice: </xsl:text>
      <code>
        <xsl:value-of select="invoice"/>
      </code>
      <xsl:text> including this code in payment details: </xsl:text>
      <code>
        <xsl:value-of select="code"/>
      </code>
      <xsl:text>. Current exchange rate of </xsl:text>
      <code>ZLD:USD</code>
      <xsl:text> is </xsl:text>
      <code>
        <xsl:value-of select="rate"/>
      </code>
      <xsl:text>.</xsl:text>
      <br/>
      <xsl:text>Your payment id is </xsl:text>
      <code>
        <xsl:value-of select="callback"/>
      </code>
      <xsl:text>, please save it to be able to say us this number in case of issues.</xsl:text>
      <br/>
      <xsl:text>
        WARNING! This is one-time invoice address, do not send ZLD to this address multiple times,
        we will accept only first payment, all following payments will be lost.
      </xsl:text>
      <br/>
      <xsl:text>
        WARNING! This address will be valid for 24 hours, do not send ZLD after 24 hours - it will be lost.
      </xsl:text>
    </p>
  </xsl:template>
</xsl:stylesheet>
