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
<!--
@todo #1193:30min When https://github.com/zerocracy/datum/issues/375 is done we should refactor
 this template to display debts table with Job/Project | Added | Amount. Where job
 should be a job number with a link to the issue/PR, and amount should include both cash value and
 minutes (as close as possible to agenda table).
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="2.0">
  <xsl:output method="html" doctype-system="about:legacy-compat" encoding="UTF-8" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:include href="/xsl/inner-layout.xsl"/>
  <xsl:variable name="mine" select="/page/details"/>
  <xsl:template match="page" mode="head">
    <title>
      <xsl:text>@</xsl:text>
      <xsl:value-of select="owner"/>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <a href="https://github.com/{owner}">
        <img src="https://socatar.com/github/{owner}/192-192" style="width:64px;height:64px;border-radius:5px;"/>
      </a>
    </p>
    <xsl:if test="not($mine)">
      <p>
        <xsl:text>This is the profile of </xsl:text>
        <a href="https://github.com/{owner}">
          <xsl:text>@</xsl:text>
          <xsl:value-of select="owner"/>
        </a>
        <xsl:text> (</xsl:text>
        <a href="http://www.zerocracy.com/terms.html#kyc">
          <xsl:choose>
            <xsl:when test="identified='true'">
              <xsl:text>identified</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>not identified yet</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </a>
        <xsl:text>).</xsl:text>
      </p>
    </xsl:if>
    <xsl:apply-templates select="rate"/>
    <xsl:apply-templates select="details"/>
    <xsl:apply-templates select="awards"/>
    <xsl:apply-templates select="agenda"/>
    <xsl:apply-templates select="mentor"/>
    <xsl:apply-templates select="students"/>
    <xsl:if test="identity/login = 'yegor256'">
      <form action="/kyc/{owner}" method="post" autocomplete="off">
        <input tabindex="1" type="text" name="details" size="50" placeholder="e.g. JEFF LEBOWSKY 23-12-1976 @EMAIL"/>
        <button tabindex="2" type="submit">
          <xsl:text>Identify</xsl:text>
        </button>
      </form>
    </xsl:if>
  </xsl:template>
  <xsl:template match="rate">
    <p>
      <xsl:choose>
        <xsl:when test="$mine">
          <xsl:text>Your</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>The</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text> hourly </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#16">
        <xsl:text>rate</xsl:text>
      </a>
      <xsl:text> is </xsl:text>
      <xsl:choose>
        <xsl:when test=".='0'">
          <xsl:text>not defined</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <span style="color:darkgreen">
            <xsl:value-of select="."/>
          </span>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="awards">
    <p>
      <xsl:choose>
        <xsl:when test="$mine">
          <xsl:text>Your</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>The</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text> current </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#18">
        <xsl:text>reputation</xsl:text>
      </a>
      <xsl:text>: </xsl:text>
      <xsl:choose>
        <xsl:when test=".=0">
          <span style="color:darkred">
            <xsl:text>none</xsl:text>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="total">
            <xsl:if test=". &gt;= 0">
              <xsl:text>+</xsl:text>
            </xsl:if>
            <xsl:value-of select="."/>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="$mine">
              <a href="/u/{/page/owner}/awards">
                <xsl:value-of select="$total"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$total"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="agenda">
    <p>
      <xsl:choose>
        <xsl:when test="$mine">
          <xsl:text>Your</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>User's</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text> currently open job</xsl:text>
      <xsl:if test=". &gt; 1">
        <xsl:text>s</xsl:text>
      </xsl:if>
      <xsl:text>: </xsl:text>
      <xsl:choose>
        <xsl:when test=".=0">
          <span style="color:darkred">
            <xsl:text>none</xsl:text>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="$mine">
              <a href="/u/{/page/owner}/agenda">
                <xsl:value-of select="."/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text> (max </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#3">
        <xsl:text>allowed</xsl:text>
      </a>
      <xsl:text>: </xsl:text>
      <xsl:choose>
        <xsl:when test="/page/awards &lt; 512">
          <xsl:text>3</xsl:text>
        </xsl:when>
        <xsl:when test="/page/awards &lt; 2048">
          <xsl:text>8</xsl:text>
        </xsl:when>
        <xsl:when test="/page/awards &lt; 4096">
          <xsl:text>16</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>24</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>).</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="details">
    <xsl:apply-templates select="vacation"/>
    <xsl:if test="count(links/link) &lt; 2">
      <p>
        <span style="color:red">
          <xsl:text>ATTENTION</xsl:text>
        </span>
        <xsl:text>: </xsl:text>
        <xsl:text>You should start talking to our bot</xsl:text>
        <xsl:text> through one of our supported media, like</xsl:text>
        <xsl:text> Telegram or Slack. More details you can find in </xsl:text>
        <a href="http://www.zerocracy.com/policy.html">
          <xsl:text>our policy</xsl:text>
        </a>
        <xsl:text>.</xsl:text>
      </p>
    </xsl:if>
    <xsl:apply-templates select="identification"/>
    <xsl:apply-templates select="links"/>
    <xsl:apply-templates select="wallet"/>
    <xsl:apply-templates select="debt"/>
    <xsl:apply-templates select="projects"/>
    <xsl:apply-templates select="skills"/>
  </xsl:template>
  <xsl:template match="vacation">
    <xsl:if test=". = 'true'">
      <p>
        <xsl:text>On vacation</xsl:text>
      </p>
    </xsl:if>
  </xsl:template>
  <xsl:template match="wallet">
    <p>
      <xsl:text>The </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#20">
        <xsl:text>wallet</xsl:text>
      </a>
      <xsl:text> is </xsl:text>
      <xsl:choose>
        <xsl:when test="info=''">
          <span style="color:darkred">
            <xsl:text>absent</xsl:text>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <code>
            <xsl:value-of select="info"/>
          </code>
          <xsl:text> at </xsl:text>
          <xsl:value-of select="bank"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="projects[project]">
    <p>
      <xsl:value-of select="count(project)"/>
      <xsl:text> project</xsl:text>
      <xsl:if test="count(project) &gt; 1">
        <xsl:text>s</xsl:text>
      </xsl:if>
      <xsl:text>: </xsl:text>
      <xsl:for-each select="project">
        <xsl:sort select="@title"/>
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <a href="/p/{.}">
          <xsl:value-of select="@title"/>
        </a>
      </xsl:for-each>
      <xsl:text> (</xsl:text>
      <a href="/board">
        <xsl:text>apply</xsl:text>
      </a>
      <xsl:text> for more).</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="projects[not(project)]">
    <p>
      <xsl:text>You're in no projects yet, </xsl:text>
      <a href="/board">
        <xsl:text>apply</xsl:text>
      </a>
      <xsl:text> to some of them.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="links[link]">
    <p>
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
    </p>
  </xsl:template>
  <xsl:template match="links[not(link)]">
    <p>
      <xsl:text>It's weird, no links?!</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="identification[.='']">
    <p>
      <xsl:text>We don't really know </xsl:text>
      <span style="color:darkred;">
        <xsl:text>who you are</xsl:text>
      </span>
      <xsl:text>; please, </xsl:text>
      <a href="/identify">
        <xsl:text>identify</xsl:text>
      </a>
      <xsl:text> yourself.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="identification[.!='']">
    <p>
      <xsl:text>We </xsl:text>
      <a href="/identify">
        <xsl:text>know</xsl:text>
      </a>
      <xsl:text> you as: </xsl:text>
      <code>
        <xsl:value-of select="."/>
      </code>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="mentor[.='0crat']">
    <p>
      <xsl:choose>
        <xsl:when test="$mine">
          <xsl:text>You've been graduated, you don't pay the </xsl:text>
          <a href="http://www.zerocracy.com/policy.html#45">
            <xsl:text>tuition fee</xsl:text>
          </a>
          <xsl:text> anymore.</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>The user has </xsl:text>
          <a href="http://www.zerocracy.com/policy.html#45">
            <xsl:text>graduated</xsl:text>
          </a>
          <xsl:text> already.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>
  <xsl:template match="mentor[.!='0crat']">
    <p>
      <xsl:choose>
        <xsl:when test="$mine">
          <xsl:text>Your mentor is </xsl:text>
          <a href="/u/{.}">
            <xsl:text>@</xsl:text>
            <xsl:value-of select="text()"/>
          </a>
          <xsl:text> (</xsl:text>
          <a href="http://www.zerocracy.com/policy.html#45">
            <xsl:text>tuition fee</xsl:text>
          </a>
          <xsl:text> goes there).</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>The </xsl:text>
          <a href="http://www.zerocracy.com/policy.html#45">
            <xsl:text>mentor</xsl:text>
          </a>
          <xsl:text> is </xsl:text>
          <a href="/u/{.}">
            <xsl:text>@</xsl:text>
            <xsl:value-of select="text()"/>
          </a>
          <xsl:text>.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>
  <xsl:template match="students[not(student)]">
    <p>
      <xsl:choose>
        <xsl:when test="$mine">
          <xsl:text>You don't have any </xsl:text>
          <a href="http://www.zerocracy.com/policy.html#1">
            <xsl:text>students</xsl:text>
          </a>
          <xsl:text> yet.</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>The user has no students.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>
  <xsl:template match="students[student]">
    <p>
      <xsl:choose>
        <xsl:when test="$mine">
          <xsl:text>Your </xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>The user has </xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="count(student)"/>
      <xsl:text> </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#1">
        <xsl:text>student</xsl:text>
        <xsl:if test="count(student) &gt; 1">
          <xsl:text>s</xsl:text>
        </xsl:if>
      </a>
      <xsl:text>: </xsl:text>
      <xsl:for-each select="student">
        <xsl:sort select="."/>
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <a href="/u/{.}">
          <xsl:text>@</xsl:text>
          <xsl:value-of select="text()"/>
        </a>
      </xsl:for-each>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="debt[item]">
    <p>
      <xsl:text>We </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#20">
        <xsl:text>owe you</xsl:text>
      </a>
      <xsl:text> these </xsl:text>
      <xsl:value-of select="count(item)"/>
      <xsl:text> payment</xsl:text>
      <xsl:if test="count(item) &gt; 1">
        <xsl:text>s (</xsl:text>
        <xsl:value-of select="@total"/>
        <xsl:text> total)</xsl:text>
      </xsl:if>
      <xsl:if test="@failed">
        <xsl:text> (</xsl:text>
        <xsl:value-of select="@failed"/>
        <xsl:text>)</xsl:text>
      </xsl:if>
      <table data-sortable="true">
        <thead>
          <th>
            <xsl:text>Added</xsl:text>
          </th>
          <th>
            <xsl:text>Amount</xsl:text>
          </th>
          <th>
            <xsl:text>Details</xsl:text>
          </th>
        </thead>
        <tbody>
          <xsl:for-each select="item">
            <tr>
              <td>
                <xsl:value-of select="ago"/>
              </td>
              <td>
                <xsl:value-of select="amount"/>
              </td>
              <td>
                <xsl:value-of select="details_html" disable-output-escaping="yes"/>
              </td>
            </tr>
          </xsl:for-each>
        </tbody>
      </table>
    </p>
  </xsl:template>
</xsl:stylesheet>
