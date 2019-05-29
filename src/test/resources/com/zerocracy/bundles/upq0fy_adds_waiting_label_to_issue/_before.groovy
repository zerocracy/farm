/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.bundles.adds_waiting_label_to_issue

import com.jcabi.github.Github
import com.jcabi.github.Issue
import com.jcabi.github.Repo
import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.entry.ExtTelegram
import com.zerocracy.pmo.People
import com.zerocracy.radars.telegram.TmZerocrat
import org.cactoos.func.UncheckedFunc
import org.mockito.Mockito
import org.telegram.telegrambots.api.methods.send.SendMessage
import java.lang.reflect.Field
import java.lang.reflect.Modifier

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  Github github = new ExtGithub(farm).value()
  Repo repo = github.repos().create(new Repos.RepoCreate('test', false))
  Issue issue = new Issue.Smart(repo.issues().create('The issue', 'to wait'))
  issue.assign('yegor256')
  new People(farm).bootstrap().link('yegor256', 'telegram', '463943472')

  UncheckedFunc mockedFunc =  Mockito.mock(UncheckedFunc)
  TmZerocrat tmZerocrat = Mockito.mock(TmZerocrat)
  Mockito.when(mockedFunc.apply(Mockito.any(ExtTelegram))).thenReturn(tmZerocrat)
  Mockito.doNothing().when(tmZerocrat).post(Mockito.any(SendMessage))
  Field field = ExtTelegram.class.getDeclaredField("SINGLETON")

  setFinalStatic(field, mockedFunc)
}

static setFinalStatic(Field field, Object newValue) {
  field.setAccessible(true)
  Field modifiersField = Field.class.getDeclaredField("modifiers")
  modifiersField.setAccessible(true)
  Modifier.FINAL
  modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL)
  field.set(null, newValue)
}

