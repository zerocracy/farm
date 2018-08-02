/*
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.stk.pm.comm

import com.jcabi.xml.XML
import com.zerocracy.entry.ExtTelegram
import com.zerocracy.farm.Assume
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import org.cactoos.Proc
import org.cactoos.func.RetryFunc
import org.telegram.telegrambots.api.methods.send.SendMessage

def exec(Project project, XML xml) {
  new Assume(project, xml).type('Notify in Telegram')
  ClaimIn claim = new ClaimIn(xml)
  String[] parts = claim.token().split(';')
  if (parts[0] != 'telegram') {
    throw new IllegalArgumentException(
      "Something is wrong with this token: \"${claim.token()}\""
    )
  }
  long chat = Long.parseLong(parts[1])
  Farm farm = binding.variables.farm
  new RetryFunc<>(
    new Proc() {
      @Override
      void exec(final Object input) throws Exception {
        new ExtTelegram(farm).value().post(
          new SendMessage()
            .enableMarkdown(true)
            .setChatId(chat)
            .setText(claim.param('message'))
        )
      }
    }
  ).apply(null)
}
