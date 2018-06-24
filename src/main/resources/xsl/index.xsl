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
        <a href="{links/link[@rel='home']/@href}">
          <img src="/svg/zerocrat.svg" style="height:128px;"/>
        </a>
      </p>
      <p>
        <xsl:text>Zerocrat is a project manager that </xsl:text>
        <a href="http://www.yegor256.com/2018/03/21/zerocracy-announcement.html">
          <xsl:text>never sleeps</xsl:text>
        </a>
        <xsl:text>.</xsl:text>
      </p>
      <nav>
        <ul>
          <li>
            <xsl:choose>
              <xsl:when test="identity">
                <a href="/u/{identity/login}">
                  <span title="GitHub user currently logged in">
                    <xsl:text>@</xsl:text>
                    <xsl:value-of select="identity/login"/>
                  </span>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <a href="{links/link[@rel='takes:github']/@href}" title="Log in using your GitHub account">
                  <img src="/svg/github.svg" style="height:1em;vertical-align:middle"/>
                  <span style="vertical-align:middle;margin-left:.4em;">
                    <xsl:text>Login</xsl:text>
                  </span>
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </li>
          <li>
            <a href="http://www.zerocracy.com/policy.html">
              <xsl:text>Policy</xsl:text>
            </a>
          </li>
          <li>
            <a href="http://www.zerocracy.com/terms.html">
              <xsl:text>Terms</xsl:text>
            </a>
          </li>
          <xsl:if test="identity">
            <li>
              <form action="{links/link[@rel='takes:logout']/@href}" method="post" class="form-as-link">
                <button type="submit" class="link">
                  <xsl:text>Exit</xsl:text>
                </button>
              </form>
            </li>
          </xsl:if>
        </ul>
      </nav>
      <footer>
        <nav>
          <ul>
            <li>
              <xsl:choose>
                <xsl:when test="contains(version/name,'SNAPSHOT')">
                  <span title="The bot was deployed manually, without any specific version">
                    <xsl:text>&#x26A1;</xsl:text>
                  </span>
                </xsl:when>
                <xsl:otherwise>
                  <span title="Current version of the bot">
                    <xsl:text>v</xsl:text>
                    <xsl:value-of select="version/name"/>
                  </span>
                </xsl:otherwise>
              </xsl:choose>
            </li>
            <li title="The time since the last restart">
              <xsl:value-of select="alive"/>
            </li>
            <li>
              <xsl:call-template name="takes_millis">
                <xsl:with-param name="millis" select="millis"/>
              </xsl:call-template>
            </li>
            <li>
              <xsl:call-template name="takes_sla">
                <xsl:with-param name="sla" select="@sla"/>
              </xsl:call-template>
            </li>
            <li>
              <xsl:call-template name="takes_memory">
                <xsl:with-param name="memory" select="memory"/>
              </xsl:call-template>
            </li>
          </ul>
        </nav>
      </footer>
    </div>
  </xsl:template>
</xsl:stylesheet>
