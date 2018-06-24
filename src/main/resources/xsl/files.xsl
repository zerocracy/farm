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
      <xsl:text>Files @</xsl:text>
      <xsl:value-of select="project"/>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <strong style="color:darkred">
        <xsl:text>ATTENTION</xsl:text>
      </strong>
      <xsl:text>: Be very careful will this form!</xsl:text>
      <xsl:text> You can easily break things if you don't know what you are doing.</xsl:text>
      <xsl:text> The best way to modify a file is to download it first, make changes locally and upload back.</xsl:text>
    </p>
    <form action="/upload/{project}" method="post" autocomplete="off" enctype="multipart/form-data">
      <label>
        <xsl:text>Upload file: </xsl:text>
      </label>
      <input tabindex="1" type="text" name="artifact" size="20" maxlength="60" placeholder="e.g. vesting.xml"/>
      <input tabindex="2" type="file" name="file"/>
      <button tabindex="3" type="submit">
        <xsl:text>Upload</xsl:text>
      </button>
    </form>
    <p>
      <xsl:text>All files available at </xsl:text>
      <a href="/p/{project}">
        <xsl:value-of select="title"/>
      </a>
      <xsl:text>:</xsl:text>
    </p>
    <table data-sortable="true">
      <thead>
        <tr>
          <th>
            <xsl:text>File</xsl:text>
          </th>
          <th style="text-align:right;">
            <xsl:text>Size</xsl:text>
          </th>
          <th>
            <xsl:text>Modified</xsl:text>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="items"/>
      </tbody>
    </table>
    <p>
      <xsl:text>You can download them all in one </xsl:text>
      <a href="/archive/{project}">
        <xsl:text>ZIP archive</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="item">
    <tr>
      <td>
        <a href="/xml/{/page/project}?file={name}">
          <code>
            <xsl:value-of select="name"/>
          </code>
        </a>
      </td>
      <td style="text-align:right;">
        <xsl:value-of select="size"/>
      </td>
      <td>
        <xsl:value-of select="modified"/>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
