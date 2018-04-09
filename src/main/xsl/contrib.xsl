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
      <img src="/badge/{project}.svg"/>
    </p>
    <p>
      <xsl:text>Project ID is </xsl:text>
      <code>
        <xsl:value-of select="project"/>
      </code>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>The current balance of "</xsl:text>
      <xsl:value-of select="title"/>
      <xsl:text>" (</xsl:text>
      <code>
        <xsl:value-of select="project"/>
      </code>
      <xsl:text>) is </xsl:text>
      <a href="/contrib-ledger/{project}">
        <xsl:value-of select="balance"/>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>We will appreciate if you </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#1">
        <xsl:text>contribute</xsl:text>
      </a>
      <xsl:text>!</xsl:text>
    </p>
    <p>
      <xsl:text>Pick one of those: </xsl:text>
      <a href="#" class="pay" data-cents="1600">
        <xsl:text>$16</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="#" class="pay" data-cents="3200">
        <xsl:text>$32</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="#" class="pay" data-cents="6400">
        <xsl:text>$64</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="#" class="pay" data-cents="12800">
        <xsl:text>$128</xsl:text>
      </a>
      <xsl:text>, </xsl:text>
      <a href="#" class="pay" data-cents="25600">
        <xsl:text>$256</xsl:text>
      </a>
      <xsl:text>, or </xsl:text>
      <a href="#" class="pay" data-cents="51200">
        <xsl:text>$512</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>This donation is non-refundable.</xsl:text>
      <xsl:text> It's a one-time payment, we will not re-charge your card.</xsl:text>
      <xsl:text> If later you decide to contribute again, you will have to get back to this page again.</xsl:text>
    </p>
    <form id="form" style="display:none" action="/contrib-pay/{project}" method="post">
      <input name="cents" id="cents" type="hidden"/>
      <input name="token" id="token" type="hidden"/>
      <input name="email" id="email" type="hidden"/>
      <input type="submit"/>
    </form>
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
              name: 'Contribute',
              description: 'One-time funding of ' + pid,
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
