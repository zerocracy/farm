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
  <xsl:output method="html" doctype-system="about:legacy-compat" encoding="utf-8" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:include href="/xsl/layout.xsl"/>
  <xsl:template match="page" mode="head">
    <title>
      <xsl:text>join</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="body">
    <div class="center" style="margin-top: 0px">
      <h1>
        Request to join
      </h1>
      <section>
        <p>
          Fill this form to send a join request to all high-ranked users.
          Someone may decide to
          <a href="http://datum.zerocracy.com/pages/policy.html#1">invite</a>
          you and become your mentor.
          Remember, that a mentor will receive financial bonuses from us
          and positive or negative
          <a href="http://datum.zerocracy.com/pages/policy.html#18">points</a>
          depending on your actions, so read this
          <a href="http://www.yegor256.com/2014/10/29/how-much-do-you-cost.html">
            blog post
          </a>
          before sending join request.
        </p>
      </section>
      <form method="post" action="/join">
        <fieldset>
          <legend>
            <h6>
              <xsl:text>Personal info</xsl:text>
            </h6>
          </legend>
          <p>
            <label for="inp-name">
              <xsl:text>Name: </xsl:text>
            </label>
            <input id="inp-name" type="text" required="required" name="name"/>
          </p>
          <p>
            <label for="inp-about">About:</label>
            <textarea id="inp-about" required="required" name="about"/>
          </p>
        </fieldset>
        <fieldset>
          <legend>
            <h6>
              <xsl:text>Social links</xsl:text>
            </h6>
          </legend>
          <p>
            <label for="inp-github">
              <xsl:text>Github: </xsl:text>
            </label>
            <input id="inp-github" type="url" placeholder="https://github.com/username" required="required" name="github" pattern="((http|https):\/\/)?github\.com\/[a-zA-Z0-9]+"/>
          </p>
          <p>
            <label for="inp-stackoverflow">
              <xsl:text>SO: </xsl:text>
            </label>
            <input id="inp-stackoverflow" type="url" placeholder="https://stackoverflow.com/users/1" name="so"/>
          </p>
          <p>
            <label for="inp-telegram">
              <xsl:text>Telegram: </xsl:text>
            </label>
            <input id="inp-telegram" type="text" placeholder="username" name="telegram" required="required"/>
          </p>
        </fieldset>
        <input type="submit"/>
      </form>
    </div>
  </xsl:template>
</xsl:stylesheet>
