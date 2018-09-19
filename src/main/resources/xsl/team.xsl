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
      <xsl:text>Team</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <h1>
      <xsl:text>Team</xsl:text>
    </h1>
    <xsl:apply-templates select="people"/>
  </xsl:template>
  <xsl:template match="people">
    <p>
      <xsl:text>There are </xsl:text>
      <xsl:value-of select="count(user)"/>
      <xsl:text> users already registered with us.</xsl:text>
      <xsl:text> To join us too you have to apply, see </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#2">
        <xsl:text>&#xA7;2</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
      <xsl:text> If you want these programmers to work with your project,</xsl:text>
      <xsl:text> you have to publish it on the </xsl:text>
      <a href="/board">
        <xsl:text>Board</xsl:text>
      </a>
      <xsl:text>, as explained in </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#26">
        <xsl:text>&#xA7;26</xsl:text>
      </a>
      <xsl:text>; we will automatically notify the best</xsl:text>
      <xsl:text> and the most relevant candidates; they will apply, if interested.</xsl:text>
    </p>
    <p>
      <strong>
        <xsl:value-of select="count(user[awards &gt; 256])"/>
      </strong>
      <xsl:text> </xsl:text>
      <span style="color:darkgreen">
        <xsl:text>active</xsl:text>
      </span>
      <xsl:text> users, +</xsl:text>
      <strong>
        <xsl:value-of select="sum(user/awards)"/>
      </strong>
      <xsl:text> total reputation (</xsl:text>
      <strong>
        <xsl:text>+</xsl:text>
        <xsl:value-of select="format-number(sum(user/awards) div count(user) div 90,'0')"/>
      </strong>
      <xsl:text> per user daily).</xsl:text>
    </p>
    <table data-sortable="true">
      <thead>
        <tr>
          <th>
            <xsl:text>User</xsl:text>
          </th>
          <th>
            <xsl:text>Mentor/</xsl:text>
            <sub>
              <xsl:text>/</xsl:text>
              <a href="http://www.zerocracy.com/policy.html#1">
                <xsl:text>&#xA7;1</xsl:text>
              </a>
            </sub>
          </th>
          <th>
            <xsl:text>Rate</xsl:text>
            <sub>
              <xsl:text>/</xsl:text>
              <a href="http://www.zerocracy.com/policy.html#16">
                <xsl:text>&#xA7;16</xsl:text>
              </a>
            </sub>
          </th>
          <th data-sortable-type="numeric">
            <xsl:text>Rep.</xsl:text>
            <sub>
              <xsl:text>/</xsl:text>
              <a href="http://www.zerocracy.com/policy.html#18">
                <xsl:text>&#xA7;18</xsl:text>
              </a>
            </sub>
          </th>
          <th data-sortable-type="numeric">
            <xsl:text>Speed</xsl:text>
            <sub>
              <xsl:text>/</xsl:text>
              <a href="http://www.zerocracy.com/policy.html#36">
                <xsl:text>&#xA7;36</xsl:text>
              </a>
            </sub>
          </th>
          <th>
            <xsl:text>Jobs</xsl:text>
          </th>
          <th>
            <xsl:text>Projects</xsl:text>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="user">
          <xsl:sort select="awards" order="descending" data-type="number"/>
        </xsl:apply-templates>
      </tbody>
    </table>
    <p>
      <a href="http://www.zerocracy.com/policy.html#18">
        <xsl:text>Reputation</xsl:text>
      </a>
      <xsl:text> legend: </xsl:text>
      <span style="color:darkgreen">
        <xsl:text>trusted</xsl:text>
      </span>
      <xsl:text> (more than 256), </xsl:text>
      <span style="color:orange">
        <xsl:text>sandbox</xsl:text>
      </span>
      <xsl:text> (less than 256), </xsl:text>
      <span style="color:darkred">
        <xsl:text>negative</xsl:text>
      </span>
      <xsl:text> reputation.</xsl:text>
      <xsl:text> Grayed out people are </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#38">
        <xsl:text>on vacation</xsl:text>
      </a>
      <xsl:text> and most likely</xsl:text>
      <xsl:text> won't be interested in working in any projects at the moment.</xsl:text>
      <xsl:text>The list shows only people that received reputation points during the last 90 days.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="user">
    <tr>
      <xsl:attribute name="style">
        <xsl:if test="login = /page/identity/login">
          <xsl:text>background-color:darkseagreen;</xsl:text>
        </xsl:if>
        <xsl:if test="vacation">
          <xsl:text>opacity:0.5;</xsl:text>
        </xsl:if>
      </xsl:attribute>
      <xsl:if test="vacation">
        <xsl:attribute name="title">
          <xsl:text>@</xsl:text>
          <xsl:value-of select="login"/>
          <xsl:text> is on vacation, see &#xA7;38</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <td>
        <img src="https://socatar.com/github/{login}/90-90" style="width:30px;height:30px;border-radius:3px;vertical-align:middle;"/>
        <xsl:text> </xsl:text>
        <a href="https://github.com/{login}">
          <xsl:text>@</xsl:text>
          <xsl:value-of select="login"/>
        </a>
        <sub>
          <xsl:text>/</xsl:text>
          <a href="/u/{login}">
            <xsl:text>z</xsl:text>
          </a>
        </sub>
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="mentor='0crat'">
            <xsl:text>&#x2014;</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <a href="https://github.com/{mentor}">
              <xsl:text>@</xsl:text>
              <xsl:value-of select="mentor"/>
            </a>
            <sub>
              <xsl:text>/</xsl:text>
              <a href="/u/{mentor}">
                <xsl:text>z</xsl:text>
              </a>
            </sub>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td style="text-align:right;">
        <xsl:choose>
          <xsl:when test="rate">
            <xsl:value-of select="rate"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>&#x2014;</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td>
        <xsl:attribute name="style">
          <xsl:text>text-align:right;color:</xsl:text>
          <xsl:choose>
            <xsl:when test="awards &gt; 256">
              <xsl:text>darkgreen</xsl:text>
            </xsl:when>
            <xsl:when test="awards &lt; 0">
              <xsl:text>darkred</xsl:text>
            </xsl:when>
            <xsl:when test="awards = 0">
              <xsl:text>inherit</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>orange</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:choose>
          <xsl:when test="awards = 0">
            <xsl:text>&#x2014;</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="awards &gt; 0">
              <xsl:text>+</xsl:text>
            </xsl:if>
            <xsl:value-of select="awards"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td>
        <xsl:variable name="days" select="speed div (24 * 60)"/>
        <xsl:attribute name="style">
          <xsl:text>text-align:right;color:</xsl:text>
          <xsl:choose>
            <xsl:when test="$days &gt; 10">
              <xsl:text>darkred</xsl:text>
            </xsl:when>
            <xsl:when test="$days &lt; 6">
              <xsl:text>darkgreen</xsl:text>
            </xsl:when>
            <xsl:when test="$days = 0">
              <xsl:text>inherit</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>orange</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:choose>
          <xsl:when test="$days = 0">
            <xsl:text>&#x2014;</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="format-number($days,'0.0')"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td style="text-align:right;">
        <xsl:value-of select="agenda"/>
      </td>
      <td style="text-align:right">
        <xsl:value-of select="projects"/>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
