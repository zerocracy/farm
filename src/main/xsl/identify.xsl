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
      <xsl:text>Identify yourself</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <p>
      <a href="https://github.com/{identity/login}">
        <img src="https://socatar.com/github/{identity/login}/192-192" style="width:64px;height:64px;border-radius:5px;"/>
      </a>
    </p>
    <xsl:apply-templates select="identification"/>
    <p>
      <xsl:text>According to our </xsl:text>
      <a href="http://www.zerocracy.com/terms.html#kyc">
        <xsl:text>Terms</xsl:text>
      </a>
      <xsl:text> we identify each project contributor, if they work for money.</xsl:text>
      <xsl:text> We use a number of third-party ID verification providers:</xsl:text>
    </p>
    <p>
      <img src="https://www.yoti.com/images/logo.svg" style="height:32px;"/>
    </p>
  </xsl:template>
  <xsl:template match="identification[.='']">
    <p>
      <xsl:text>We don't really know </xsl:text>
      <span style="color:darkred;">
        <xsl:text>who you are</xsl:text>
      </span>
      <xsl:text>; please, </xsl:text>
      <a href="https://www.yoti.com/connect/{/page/yoti_app_id}">
        <xsl:text>identify</xsl:text>
      </a>
      <xsl:text> yourself, as required </xsl:text>
      <a href="http://www.zerocracy.com/terms.html#kyc">
        <xsl:text>here</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>If you, for some reason, can't identify yourself via our providers,</xsl:text>
      <xsl:text> you can email us a photocopy of your passport or any other ID document to </xsl:text>
      <a href="mailto:identity@zerocracy.com">
        <xsl:text>identity@zerocracy.com</xsl:text>
      </a>
      <xsl:text>. In the email you will have to explain why you can't use third-party services</xsl:text>
      <xsl:text> and mention your GitHub nickname.</xsl:text>
      <xsl:text> Sometimes this may happen, if your ID is too old or your country is not in the list of supported countries.</xsl:text>
      <xsl:text> We can only accept your ID by email as an exception.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="identification[.!='']">
    <p>
      <xsl:text>We already know you as: </xsl:text>
      <code>
        <xsl:value-of select="."/>
      </code>
      <xsl:text>.</xsl:text>
    </p>
  </xsl:template>
</xsl:stylesheet>
