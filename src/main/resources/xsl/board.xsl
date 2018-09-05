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
      <xsl:text>Board</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <h1>
      <xsl:text>Board</xsl:text>
    </h1>
    <xsl:apply-templates select="projects"/>
  </xsl:template>
  <xsl:template match="projects[not(project)]">
    <p>
      <xsl:text>There are no projects available at the moment.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="projects[project]">
    <p>
      <xsl:text>To join any of these projects you have</xsl:text>
      <xsl:text> to apply, by asking Zerocrat in a chat,</xsl:text>
      <xsl:text> see </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#2">
        <xsl:text>&#xA7;2</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
      <xsl:text> If you are just starting to work with us, you have to try to join "sandbox" projects, see </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#33">
        <xsl:text>&#xA7;33</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
      <xsl:text> In order to add your project to the board you have to "publish" it, see </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#26">
        <xsl:text>&#xA7;26</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <table data-sortable="true">
      <thead>
        <tr>
          <th>
            <xsl:text>ID</xsl:text>
          </th>
          <th>
            <xsl:text>Title</xsl:text>
          </th>
          <th>
            <xsl:text>ARC</xsl:text>
          </th>
          <th>
            <xsl:text>GitHub Repositories</xsl:text>
          </th>
          <th>
            <xsl:text>Languages</xsl:text>
          </th>
          <th>
            <xsl:text>Members</xsl:text>
          </th>
          <th>
            <xsl:text>Jobs</xsl:text>
            <a href="#1">
              <sup>
                <xsl:text>1</xsl:text>
              </sup>
            </a>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="project"/>
      </tbody>
    </table>
    <p>
      <sup id="1">
        <xsl:text>1</xsl:text>
      </sup>
      <xsl:text>The amount of currently assigned jobs in the project
        and the total amount of jobs; the bigger the difference
        the higher the deficit, if you join you will get jobs immediately.</xsl:text>
      <xsl:if test="project[deficit='true']">
        <sup id="2">
          <xsl:text>2</xsl:text>
        </sup>
        <xsl:text>Crossed-out projects are not properly funded
          at the moment and new jobs are not assigned to programmers;
          the situation may change at any moment.</xsl:text>
      </xsl:if>
    </p>
  </xsl:template>
  <xsl:template match="project">
    <tr>
      <xsl:if test="cash = '0'">
        <xsl:attribute name="style">
          <xsl:text>opacity:0.5;</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <td>
        <a href="/p/{id}">
          <code>
            <xsl:value-of select="id"/>
          </code>
        </a>
        <span style="display:block;font-size:0.8em;line-height:1em;">
          <xsl:choose>
            <xsl:when test="cash = '0'">
              <span style="color:darkred;" title="The project has no funds, you will work for free">
                <xsl:text>no funds</xsl:text>
              </span>
            </xsl:when>
            <xsl:when test="deficit = 'true'">
              <span style="color:darkgreen;" title="The project is not properly funded">
                <xsl:text>no funds</xsl:text>
              </span>
            </xsl:when>
            <xsl:otherwise>
              <span style="color:darkgreen;" title="The project is funded">
                <xsl:value-of select="cash"/>
              </span>
            </xsl:otherwise>
          </xsl:choose>
        </span>
      </td>
      <td>
        <xsl:value-of select="title"/>
        <xsl:if test="sandbox='true'">
          <span style="display:block;font-size:0.8em;color:gray;line-height:1em;">
            <xsl:text>sandbox</xsl:text>
          </span>
        </xsl:if>
      </td>
      <td>
        <xsl:apply-templates select="architects"/>
      </td>
      <td>
        <xsl:apply-templates select="repositories"/>
      </td>
      <td>
        <xsl:value-of select="languages"/>
      </td>
      <td style="text-align:right;">
        <xsl:if test="mine='false'">
          <xsl:value-of select="members"/>
        </xsl:if>
        <xsl:if test="mine='true'">
          <xsl:if test="members = 1">
            <xsl:text>you</xsl:text>
          </xsl:if>
          <xsl:if test="members &gt; 1">
            <xsl:value-of select="members - 1"/>
            <xsl:text>+you</xsl:text>
          </xsl:if>
        </xsl:if>
      </td>
      <td style="text-align:right;">
        <xsl:variable name="txt">
          <xsl:value-of select="orders"/>
          <xsl:text>/</xsl:text>
          <xsl:value-of select="jobs"/>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="deficit = 'true'">
            <span style="text-decoration:line-through;color:darkred;">
              <xsl:value-of select="$txt"/>
            </span>
            <a href="#2">
              <sup>
                <xsl:text>2</xsl:text>
              </sup>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$txt"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="architects">
    <xsl:for-each select="architect">
      <xsl:if test="position() &gt; 1">
        <xsl:text>, </xsl:text>
      </xsl:if>
      <a href="/u/{.}">
        <xsl:text>@</xsl:text>
        <xsl:value-of select="."/>
      </a>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="repositories">
    <xsl:for-each select="repository">
      <xsl:if test="position() &gt; 1">
        <xsl:text>, </xsl:text>
      </xsl:if>
      <a href="https://github.com/{.}">
        <xsl:value-of select="."/>
      </a>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
