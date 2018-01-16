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
      <xsl:text>Gang</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <xsl:apply-templates select="people"/>
  </xsl:template>
  <xsl:template match="people">
    <p>
      <xsl:text>To join us you have</xsl:text>
      <xsl:text> to apply, by asking Zerocrat in a chat,</xsl:text>
      <xsl:text> see </xsl:text>
      <a href="http://datum.zerocracy.com/pages/policy.html#2">
        <xsl:text>&#xA7;2</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <table>
      <thead>
        <tr>
          <th>
            <xsl:text>User</xsl:text>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="user">
          <xsl:sort select="login"/>
        </xsl:apply-templates>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template match="user">
    <tr>
      <td>
        <a href="https://github.com/{login}">
          <xsl:text>@</xsl:text>
          <xsl:value-of select="login"/>
        </a>
        <xsl:text>/</xsl:text>
        <a href="/u/{login}">
          <xsl:text>z</xsl:text>
        </a>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
