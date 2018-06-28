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
      <xsl:text>Vacancies</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <h1>
      <xsl:text>Vacancies</xsl:text>
    </h1>
    <xsl:apply-templates select="vacancies"/>
  </xsl:template>
  <xsl:template match="vacancies">
    <p>
      <xsl:text>There are </xsl:text>
      <xsl:value-of select="count(vacancy)"/>
      <xsl:text> vacancies.</xsl:text>
      <xsl:text> To apply you use apply command in 0crat chat bot.</xsl:text>
    </p>
    <table>
      <thead>
        <tr>
          <th>
            <xsl:text>Project ID</xsl:text>
          </th>
          <th>
            <xsl:text>Project name</xsl:text>
          </th>
          <th>
            <xsl:text>Vacancy</xsl:text>
          </th>
          <xsl:text>Added</xsl:text>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="vacancy"/>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template match="vacancy">
    <tr>
      <td>
        <code>
          <xsl:value-of select="project"/>
        </code>
      </td>
      <td>
        <xsl:value-of select="name"/>
      </td>
      <td>
        <xsl:value-of select="text"/>
      </td>
      <td>
        <xsl:value-of select="added"/>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
