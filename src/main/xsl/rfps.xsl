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
      <xsl:text>RFPs</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <h1>
      <xsl:text>RFPs</xsl:text>
    </h1>
    <xsl:apply-templates select="rfps"/>
  </xsl:template>
  <xsl:template match="rfps[not(rfp)]">
    <p>
      <xsl:text>There are no new RFPs, sorry, see </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#40">
        <xsl:text>&#xA7;40</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="rfps[rfp]">
    <p>
      <xsl:text>This is a full list of RFPs available for purchase now, see </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#40">
        <xsl:text>&#xA7;40</xsl:text>
      </a>
      <xsl:text>:</xsl:text>
    </p>
    <table data-sortable="true">
      <thead>
        <tr>
          <th>
            <xsl:text>ID</xsl:text>
          </th>
          <th>
            <xsl:text>Created</xsl:text>
          </th>
          <th>
            <xsl:text>Statement of Work</xsl:text>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="rfp"/>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template match="rfp">
    <tr>
      <td>
        <code>
          <xsl:value-of select="id"/>
        </code>
      </td>
      <td title="{created}">
        <xsl:value-of select="ago"/>
        <xsl:text> ago</xsl:text>
      </td>
      <td>
        <xsl:value-of select="sow"/>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
