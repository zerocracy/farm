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
  <xsl:include href="/xsl/inner-layout.xsl"/>
  <xsl:template match="page" mode="head">
    <title>
      <xsl:value-of select="title"/>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <xsl:text>Project: </xsl:text>
      <code>
        <xsl:value-of select="title"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="project"/>
      </code>
      <xsl:apply-templates select="roles"/>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:apply-templates select="project_links"/>
    </p>
    <p>
      <xsl:text>Scope: </xsl:text>
      <a href="/a/{project}?a=pm/scope/wbs">
        <xsl:text>WBS</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Time: </xsl:text>
      <!--
      <a href="/a/{project}?a=pm/time/schedule">
        <xsl:text>Schedule</xsl:text>
      </a>
      -->
      <a href="/a/{project}?a=pm/time/reminders">
        <xsl:text>Reminders</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Cost: </xsl:text>
      <!--
      <a href="/a/{project}?a=pm/cost/budget">
        <xsl:text>Budget</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      -->
      <a href="/a/{project}?a=pm/cost/rates">
        <xsl:text>Rates</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/cost/estimates">
        <xsl:text>Estimates</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Integration: </xsl:text>
      <a href="/report/{project}">
        <xsl:text>Reports</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/footprint/{project}">
        <xsl:text>Footprint</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/claims">
        <xsl:text>Claims</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/in/orders">
        <xsl:text>Orders</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Staff: </xsl:text>
      <a href="/a/{project}?a=pm/staff/bans">
        <xsl:text>Bans</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/staff/roles">
        <xsl:text>Roles</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/staff/elections">
        <xsl:text>Elections</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="roles[role]">
    <xsl:text> (your roles: </xsl:text>
    <xsl:for-each select="role">
      <xsl:if test="position() &gt; 1">
        <xsl:text>, </xsl:text>
      </xsl:if>
      <xsl:value-of select="."/>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:template>
  <xsl:template match="project_links[not(link)]">
    <xsl:text>The project has no links.</xsl:text>
  </xsl:template>
  <xsl:template match="project_links[link]">
    <xsl:value-of select="count(link)"/>
    <xsl:text> link</xsl:text>
    <xsl:if test="count(link) &gt; 1">
      <xsl:text>s</xsl:text>
    </xsl:if>
    <xsl:text>: </xsl:text>
    <xsl:for-each select="link">
      <xsl:if test="position() &gt; 1">
        <xsl:text>, </xsl:text>
      </xsl:if>
      <code>
        <xsl:value-of select="."/>
      </code>
    </xsl:for-each>
    <xsl:text>.</xsl:text>
  </xsl:template>
</xsl:stylesheet>
