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
    <xsl:apply-templates select="." mode="js"/>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <img src="http://www.0crat.com/badge/{project}.svg"/>
    </p>
    <xsl:if test="pause = 'true'">
      <p>
        <strong>
          <xsl:text>Attention</xsl:text>
        </strong>
        <xsl:text>: the project is on pause, see </xsl:text>
        <a href="http://datum.zerocracy.com/pages/policy.html#24">
          <xsl:text>par.24</xsl:text>
        </a>
        <xsl:text>.</xsl:text>
      </p>
    </xsl:if>
    <p>
      <xsl:text>Project (</xsl:text>
      <xsl:if test="pause = 'false'">
        <xsl:text>alive</xsl:text>
      </xsl:if>
      <xsl:text>): </xsl:text>
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
    <xsl:apply-templates select="." mode="artifacts"/>
  </xsl:template>
  <xsl:template match="page[project!='PMO']" mode="artifacts">
    <p>
      <xsl:text>Cash: </xsl:text>
      <a href="/a/{project}?a=pm/cost/ledger">
        <xsl:value-of select="cash"/>
      </a>
      <xsl:text> (</xsl:text>
      <a href="#" class="pay">
        <xsl:text>add more funds</xsl:text>
      </a>
      <xsl:text>), locked: </xsl:text>
      <a href="/a/{project}?a=pm/cost/estimates">
        <xsl:value-of select="estimates"/>
      </a>
      <xsl:text>, fee: </xsl:text>
      <xsl:value-of select="fee"/>
      <xsl:text>.</xsl:text>
    </p>
    <form id="form" style="display:none" action="/pay/{project}" method="post">
      <input name="cents" id="cents" type="hidden"/>
      <input name="token" id="token" type="hidden"/>
      <input name="email" id="email" type="hidden"/>
      <input type="submit"/>
    </form>
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
      <a href="/a/{project}?a=pm/cost/ledger">
        <xsl:text>Ledger</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/cost/rates">
        <xsl:text>Rates</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/cost/boosts">
        <xsl:text>Boosts</xsl:text>
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
  <xsl:template match="page[project='PMO']" mode="artifacts">
    <p>
      <xsl:text>Artifacts: </xsl:text>
      <a href="/report/{project}">
        <xsl:text>Reports</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/footprint/{project}">
        <xsl:text>Footprint</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pmo/bots">
        <xsl:text>Bots</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pmo/catalog">
        <xsl:text>Catalog</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pmo/people">
        <xsl:text>People</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/claims">
        <xsl:text>Claims</xsl:text>
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
  <xsl:template match="page" mode="js">
    <xsl:element name="script">
      <xsl:attribute name="src">
        <xsl:text>https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js</xsl:text>
      </xsl:attribute>
    </xsl:element>
    <xsl:element name="script">
      <xsl:attribute name="src">
        <xsl:text>https://checkout.stripe.com/checkout.js</xsl:text>
      </xsl:attribute>
    </xsl:element>
    <xsl:element name="script">
      <xsl:attribute name="type">
        <xsl:text>text/javascript</xsl:text>
      </xsl:attribute>
      <xsl:text>var stripe_key='</xsl:text>
      <xsl:value-of select="stripe_key"/>
      <xsl:text>';</xsl:text>
      <xsl:text>var stripe_cents=</xsl:text>
      <xsl:value-of select="25600"/>
      <xsl:text>;</xsl:text>
      <xsl:text>var pid='</xsl:text>
      <xsl:value-of select="project"/>
      <xsl:text>';</xsl:text>
    </xsl:element>
    <xsl:element name="script">
      <xsl:attribute name="type">
        <xsl:text>text/javascript</xsl:text>
      </xsl:attribute>
      <xsl:text>
        // <![CDATA[
        $(function() {
          var handler = StripeCheckout.configure({
            key: stripe_key,
            image: 'http://www.zerocracy.com/logo.svg',
            token: function (token) {
              $('#token').val(token.id);
              $('#email').val(token.email);
              $('#form').submit();
            }
          });
          $('a.pay').on('click', function (e) {
            $('#cents').val(stripe_cents);
            handler.open({
              name: 'Add funds',
              description: 'Initial payment to ' + pid,
              amount: stripe_cents
            });
            e.preventDefault();
          });
          $(window).on('popstate', function () {
            handler.close();
          });
        });
        // ]]>
      </xsl:text>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
