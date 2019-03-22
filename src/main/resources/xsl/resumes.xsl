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
      <xsl:text>Resumes</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <h1>Resumes</h1>
    <p>
      <xsl:choose>
        <xsl:when test="filter = 'all' and inviter = 'true'">
          <xsl:text>All Zerocracy resumes</xsl:text>
          <xsl:text> (click </xsl:text>
          <a href="/u/{identity/login}/resumes?filter=my">here</a>
          <xsl:text> to see only your resumes):</xsl:text>
        </xsl:when>
        <xsl:when test="filter = 'my' and inviter = 'true'">
          <xsl:text>You are examiner for these resumes</xsl:text>
          <xsl:text> (click </xsl:text>
          <a href="/u/{identity/login}/resumes?filter=all">here</a>
          <xsl:text> to see all resumes):</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>You are examiner for these resumes:</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="resumes"/>
    </p>
  </xsl:template>
  <xsl:template match="resumes">
    <ul>
      <xsl:for-each select="./resume">
        <li id="resume-{@login}">
          <xsl:text>Resume by @</xsl:text>
          <xsl:value-of select="@login"/>
          <xsl:value-of select="text/text()"/>
          <xsl:text>Stackoverflow: </xsl:text>
          <a href="https://stackoverflow.com/users/{stackoverflow}">
            <xsl:value-of select="stackoverflow"/>
          </a>
          <xsl:text>Github: </xsl:text>
          <a href="https://github.com/{@login}">
            <xsl:value-of select="@login"/>
          </a>
          <xsl:text>Telegram: </xsl:text>
          <a href="https://t.me/{telegram}">
            <xsl:value-of select="concat('@', telegram)"/>
          </a>
          <xsl:text>Personality: </xsl:text>
          <code>
            <xsl:value-of select="personality"/>
          </code>
          <xsl:text>Examiner: </xsl:text>
          <a href="https://0crat/u/{examiner}">
            <xsl:text>@</xsl:text>
            <xsl:value-of select="examiner"/>
          </a>
          <xsl:text>Submitted: </xsl:text>
          <xsl:value-of select="submitted"/>
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>
</xsl:stylesheet>
