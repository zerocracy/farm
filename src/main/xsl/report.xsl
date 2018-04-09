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
      <xsl:value-of select="project"/>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <xsl:text>Statistical report at </xsl:text>
      <a href="/p/{project}">
        <xsl:value-of select="project"/>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <form action="" method="get">
      <fieldset style="display:inline">
        <label>
          <xsl:text>Report:&#xA0;</xsl:text>
        </label>
        <select tabindex="1" name="report">
          <xsl:for-each select="reports/report">
            <option value="{.}">
              <xsl:if test=".=/page/report">
                <xsl:attribute name="selected">
                  <xsl:text>selected</xsl:text>
                </xsl:attribute>
              </xsl:if>
              <xsl:value-of select="."/>
            </option>
          </xsl:for-each>
        </select>
        <xsl:text> </xsl:text>
        <label>
          <xsl:text>Start:&#xA0;</xsl:text>
        </label>
        <input type="date" name="start" value="{/page/start}"/>
        <xsl:text> </xsl:text>
        <label>
          <xsl:text>End:&#xA0;</xsl:text>
        </label>
        <input type="date" name="end" value="{/page/end}"/>
        <label>
          <xsl:text>&#xA0;</xsl:text>
        </label>
        <button tabindex="2" type="submit">
          <xsl:text>Go</xsl:text>
        </button>
      </fieldset>
    </form>
    <xsl:apply-templates select="rows"/>
  </xsl:template>
  <xsl:template match="rows[not(row)]">
    <p>
      <xsl:text>There is </xsl:text>
      <span style="color:red">
        <xsl:text>no data</xsl:text>
      </span>
      <xsl:text> for your request, sorry.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="rows[row]">
    <p>
      <xsl:value-of select="/page/title" disable-output-escaping="yes"/>
    </p>
    <table data-sortable="true">
      <thead>
        <tr>
          <xsl:for-each select="row[1]/*">
            <th>
              <xsl:value-of select="name()"/>
            </th>
          </xsl:for-each>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="row"/>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template match="row">
    <tr>
      <xsl:for-each select="*">
        <td>
          <xsl:if test="@type='java.lang.Integer' or @type='java.lang.Long'">
            <xsl:attribute name="style">
              <xsl:text>text-align:right</xsl:text>
            </xsl:attribute>
          </xsl:if>
          <xsl:value-of select="."/>
        </td>
      </xsl:for-each>
    </tr>
  </xsl:template>
</xsl:stylesheet>
