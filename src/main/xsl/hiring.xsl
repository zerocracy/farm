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
      <xsl:text>Hiring</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="inner">
    <h1>
      <xsl:text>We are hiring</xsl:text>
    </h1>
    <p>
      <xsl:text>Fill the form and we will send this</xsl:text>
      <xsl:text> announcement to all users with enough reputation to </xsl:text>
      <a href="http://www.zerocracy.com/policy.html#2">
        <xsl:text>apply</xsl:text>
      </a>
      <xsl:text> to your project </xsl:text>
      <code>
        <xsl:value-of select="project"/>
      </code>
      <xsl:text>. Some of them</xsl:text>
      <xsl:text> may be interested and will apply.</xsl:text>
      <xsl:text> Of course, there is no guarantee.</xsl:text>
      <xsl:text> Please, make sure your text is short and straight to the point.</xsl:text>
    </p>
    <form action="/hiring-send/{project}" method="post">
      <fieldset>
        <label>
          <xsl:text>The text for our users:</xsl:text>
        </label>
        <textarea tabindex="1" name="text" style="width:100%;height:5em;">
          <xsl:text>Don't forget to mention:</xsl:text>
          <xsl:text> 1) How much you pay per hour?</xsl:text>
          <xsl:text> 2) How big is your total budget?</xsl:text>
          <xsl:text> 3) What is your tech stack?</xsl:text>
        </textarea>
        <button tabindex="2" type="submit">
          <xsl:text>Submit</xsl:text>
        </button>
      </fieldset>
    </form>
  </xsl:template>
</xsl:stylesheet>
