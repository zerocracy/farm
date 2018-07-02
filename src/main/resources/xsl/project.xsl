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
      <xsl:value-of select="title"/>
    </title>
    <xsl:apply-templates select="." mode="js"/>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <xsl:choose>
        <xsl:when test="project = 'PMO'">
          <strong>
            <xsl:text>PMO</xsl:text>
          </strong>
        </xsl:when>
        <xsl:otherwise>
          <img src="/badge/{project}.svg"/>
          <a href="/contrib/{project}">
            <img src="/contrib-badge/{project}.svg" style="margin-left:1em;"/>
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </p>
    <xsl:if test="pause = 'true'">
      <p>
        <strong style="background-color:darkred;color:white;">
          <xsl:text>Attention</xsl:text>
        </strong>
        <xsl:text>: the project is on pause, see </xsl:text>
        <a href="http://www.zerocracy.com/policy.html#24">
          <xsl:text>&#xA7;24</xsl:text>
        </a>
        <xsl:text>.</xsl:text>
      </p>
    </xsl:if>
    <xsl:if test="roles[not(role)]">
      <p>
        <xsl:text>The project is managed by Zerocrat according to this </xsl:text>
        <a href="http://www.zerocracy.com/policy.html">
          <xsl:text>Policy</xsl:text>
        </a>
        <xsl:text>. </xsl:text>
        <xsl:text>If you want to join as a developer, start at </xsl:text>
        <a href="http://www.zerocracy.com/policy.html#2">
          <xsl:text>&#xA7;2</xsl:text>
        </a>
        <xsl:text>. </xsl:text>
        <xsl:text>We also recommend you to join this </xsl:text>
        <a href="https://t.me/joinchat/AAAAAEJFMRzsRTRxM3ec6A">
          <xsl:text>Telegram chat</xsl:text>
        </a>
        <xsl:text> to find someone who can </xsl:text>
        <a href="http://www.zerocracy.com/policy.html#1">
          <xsl:text>invite</xsl:text>
        </a>
        <xsl:text> you and explain how Zerocrat works.</xsl:text>
      </p>
    </xsl:if>
    <p>
      <xsl:text>Project (</xsl:text>
      <a href="http://www.zerocracy.com/policy.html#24">
        <xsl:choose>
          <xsl:when test="pause='false'">
            <xsl:text>alive</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>paused</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/board">
        <xsl:choose>
          <xsl:when test="published='true'">
            <xsl:text>visible</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>invisible</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </a>
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
      <xsl:choose>
        <xsl:when test="rate">
          <xsl:text>Your </xsl:text>
          <a href="http://www.zerocracy.com/policy.html#16">
            <xsl:text>rate</xsl:text>
          </a>
          <xsl:text> here is </xsl:text>
          <span style="color:darkgreen">
            <xsl:value-of select="rate"/>
          </span>
        </xsl:when>
        <xsl:when test="roles/role">
          <xsl:text>You are working in this project </xsl:text>
          <a href="http://www.zerocracy.com/policy.html#16">
            <xsl:text>for free</xsl:text>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>You are not working in this project,</xsl:text>
          <xsl:text> but you can </xsl:text>
          <a href="http://www.zerocracy.com/policy.html#2">
            <xsl:text>apply</xsl:text>
          </a>
          <xsl:text>.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="vesting">
        <xsl:text>; your </xsl:text>
        <a href="http://www.zerocracy.com/policy.html#37">
          <xsl:text>vesting</xsl:text>
        </a>
        <xsl:text> rate is </xsl:text>
        <span style="color:darkgreen">
          <xsl:value-of select="vesting"/>
        </span>
      </xsl:if>
      <xsl:text>.</xsl:text>
    </p>
    <xsl:if test="ownership != ''">
      <p>
        <xsl:text>You own: </xsl:text>
        <code>
          <xsl:value-of select="ownership"/>
        </code>
        <xsl:text> (</xsl:text>
        <a href="/equity/{project}">
          <xsl:text>proof</xsl:text>
        </a>
        <xsl:text>).</xsl:text>
      </p>
    </xsl:if>
    <xsl:apply-templates select="architects"/>
    <xsl:apply-templates select="project_links"/>
    <xsl:if test="project!='PMO' and cash">
      <xsl:apply-templates select="." mode="cash"/>
    </xsl:if>
    <xsl:if test="roles/role='ARC' or roles/role='PO' or project='PMO'">
      <xsl:apply-templates select="." mode="artifacts"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="page" mode="cash">
    <xsl:if test="identity/login = 'yegor256'">
      <form action="/donate/{project}" method="post" autocomplete="off">
        <label>
          <xsl:text>Donate: </xsl:text>
        </label>
        <input tabindex="1" type="text" name="amount" size="15" maxlength="15" placeholder="e.g. $100"/>
        <button tabindex="2" type="submit">
          <xsl:text>Donate</xsl:text>
        </button>
      </form>
    </xsl:if>
    <p>
      <xsl:text>Cash balance: </xsl:text>
      <a href="/a/{project}?a=pm/cost/ledger">
        <xsl:value-of select="cash"/>
      </a>
      <xsl:text> (</xsl:text>
      <xsl:text>add </xsl:text>
      <a href="#" class="pay" data-cents="6400">
        <xsl:text>$64</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="#" class="pay" data-cents="25600">
        <xsl:text>$256</xsl:text>
      </a>
      <xsl:text> or </xsl:text>
      <a href="#" class="pay" data-cents="102400">
        <xsl:text>$1024</xsl:text>
      </a>
      <xsl:text> via </xsl:text>
      <img src="/svg/stripe-logo.svg" style="height:1em;vertical-align:middle;"/>
      <xsl:text>), locked: </xsl:text>
      <a href="/a/{project}?a=pm/cost/estimates">
        <xsl:value-of select="estimates"/>
      </a>
      <xsl:text>, </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#23">
        <xsl:text>fee</xsl:text>
      </a>
      <xsl:text>: </xsl:text>
      <xsl:choose>
        <xsl:when test="fee = '0'">
          <span style="color:darkgreen">
            <xsl:text>waived</xsl:text>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="fee"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>.</xsl:text>
    </p>
    <xsl:if test="deficit = 'true'">
      <p>
        <span style="background-color:darkred;color:white;">
          <xsl:text>ATTENTION</xsl:text>
        </span>
        <xsl:text>: The project is not </xsl:text>
        <a href="http://www.zerocracy.com/policy.html#21">
          <xsl:text>funded</xsl:text>
        </a>
        <xsl:text>, we can't assign any new tasks to anyone.</xsl:text>
      </p>
    </xsl:if>
    <xsl:if test="recharge">
      <p>
        <xsl:text>We </xsl:text>
        <span style="color:darkgreen">
          <xsl:text>automatically</xsl:text>
        </span>
        <xsl:text> </xsl:text>
        <a href="http://www.zerocracy.com/policy.html#22">
          <xsl:text>recharge</xsl:text>
        </a>
        <xsl:text> your card for </xsl:text>
        <xsl:value-of select="recharge"/>
        <xsl:text> when the balance drops below zero.</xsl:text>
        <xsl:text> You can always request a full refund of the residual</xsl:text>
        <xsl:text> project balance, just </xsl:text>
        <a href="mailto:refund@zerocracy.com">
          <xsl:text>email us</xsl:text>
        </a>
        <xsl:text>.</xsl:text>
      </p>
    </xsl:if>
    <form id="form" style="display:none" action="/pay/{project}" method="post">
      <input name="cents" id="cents" type="hidden"/>
      <input name="token" id="token" type="hidden"/>
      <input name="email" id="email" type="hidden"/>
      <input type="submit"/>
    </form>
  </xsl:template>
  <xsl:template match="page[project!='PMO']" mode="artifacts">
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
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/cost/vesting">
        <xsl:text>Vesting</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/cost/equity">
        <xsl:text>Equity</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Integration: </xsl:text>
      <a href="/files/{project}">
        <xsl:text>Files</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
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
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pm/in/impediments">
        <xsl:text>Impediments</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>Quality: </xsl:text>
      <a href="/a/{project}?a=pm/qa/reviews">
        <xsl:text>Reviews</xsl:text>
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
      <xsl:text> (are you </xsl:text>
      <a href="/hiring/{project}">
        <xsl:text>hiring</xsl:text>
      </a>
      <xsl:text>?), </xsl:text>
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
      <xsl:text>, </xsl:text>
      <a href="/a/{project}?a=pmo/debts">
        <xsl:text>Debts</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="architects">
    <p>
      <xsl:text>Architect</xsl:text>
      <xsl:if test="count(architect) &gt; 1">
        <xsl:text>s</xsl:text>
      </xsl:if>
      <xsl:text>: </xsl:text>
      <xsl:for-each select="architect">
        <xsl:if test="position() &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <a href="/u/{.}">
          <xsl:text>@</xsl:text>
          <xsl:value-of select="."/>
        </a>
      </xsl:for-each>
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
  <xsl:template match="project_links">
    <p>
      <xsl:choose>
        <xsl:when test="link">
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
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>The project has no links</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>.</xsl:text>
    </p>
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
            var cents = $(this).attr('data-cents')
            $('#cents').val(cents);
            handler.open({
              name: 'Add funds',
              description: 'Initial payment to ' + pid,
              amount: cents
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
