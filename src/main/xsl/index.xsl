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
  <xsl:include href="/xsl/layout.xsl"/>
  <xsl:include href="/org/takes/rs/xe/millis.xsl"/>
  <xsl:include href="/org/takes/rs/xe/memory.xsl"/>
  <xsl:include href="/org/takes/rs/xe/sla.xsl"/>
  <xsl:template match="page" mode="head">
    <title>
      <xsl:text>0crat</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="body">
    <div class="center">
      <p>
        <a href="/">
          <img src="/images/logo.svg" style="width:128px;height:128px;"/>
        </a>
      </p>
      <p>
        <xsl:text>Zerocrat is a project manager</xsl:text>
        <xsl:text> that never sleeps.</xsl:text>
      </p>
      <p>
        <span title="Current version of the bot">
          <xsl:text>v</xsl:text>
          <xsl:value-of select="version/name"/>
        </span>
        <xsl:text> &#xB7; </xsl:text>
        <span title="Claims processed over the last week">
          <xsl:value-of select="claims"/>
        </span>
        <xsl:text> &#xB7; </xsl:text>
        <span title="The time since the last restart">
          <xsl:value-of select="alive"/>
        </span>
        <xsl:text> &#xB7; </xsl:text>
        <xsl:call-template name="takes_millis">
          <xsl:with-param name="millis" select="millis"/>
        </xsl:call-template>
        <xsl:text> &#xB7; </xsl:text>
        <xsl:call-template name="takes_sla">
          <xsl:with-param name="sla" select="@sla"/>
        </xsl:call-template>
        <xsl:text> &#xB7; </xsl:text>
        <xsl:call-template name="takes_memory">
          <xsl:with-param name="memory" select="memory"/>
        </xsl:call-template>
      </p>
      <p>
        <xsl:if test="identity">
          <a href="/u/{identity/login}">
            <span title="GitHub user currently logged in">
              <xsl:text>@</xsl:text>
              <xsl:value-of select="identity/login"/>
            </span>
          </a>
          <xsl:text> &#xB7; </xsl:text>
        </xsl:if>
        <a href="http://datum.zerocracy.com/pages/policy.html">
          <xsl:text>Policy</xsl:text>
        </a>
        <xsl:text> &#xB7; </xsl:text>
        <a href="http://datum.zerocracy.com/pages/terms.html">
          <xsl:text>Terms</xsl:text>
        </a>
        <xsl:text> &#xB7; </xsl:text>
        <xsl:if test="identity">
          <a href="{links/link[@rel='takes:logout']/@href}" title="Log out">
            <xsl:text>Exit</xsl:text>
          </a>
        </xsl:if>
        <xsl:if test="not(identity)">
          <a href="{links/link[@rel='takes:github']/@href}" title="Log in using your GitHub account">
            <xsl:text>Login</xsl:text>
          </a>
        </xsl:if>
      </p>
      <p>
        <a href="http://www.sixnines.io/h/2b3a">
          <img src="http://www.sixnines.io/b/2b3a?style=flat"/>
        </a>
        <xsl:text> </xsl:text>
        <a href="http://www.rehttp.net/i?u=http%3A%2F%2Fwww.0crat.com%2Fghook">
          <img src="http://www.rehttp.net/b?u=http%3A%2F%2Fwww.0crat.com%2Fghook"/>
        </a>
      </p>
    </div>
  </xsl:template>
</xsl:stylesheet>
