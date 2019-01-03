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

import com.zerocracy.Xocument
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

def itemType(String name) {
  if (["claims"].contains(name)) {
    return 'pm'
  }
  if (["releases", "milestones", "precedences", "reminders"].contains(name)) {
    return 'pm/time'
  }
  if (["bans", "elections", "roles"].contains(name)) {
    return "pm/staff"
  }
  if (["archive", "wbs"].contains(name)) {
    return "pm/scope"
  }
  if (["reviews"].contains(name)) {
    return "pm/qa"
  }
  if (["orders", "impediments"].contains(name)) {
    return "pm/in"
  }
  if (["boosts", "budget", "equity", "estimates", "ledger", "rates", "vesting"].contains(name)) {
    return "pm/cost"
  }
  if (["agenda", "awards", "blanks", "bots", "catalog", "debts", "negligence", "options", "people", "projects", "resumes", "rfps", "speed", "vacancies", "verbosity"].contains(name)) {
    return "pmo"
  }
}

def bundlesDir = new File('src/test/resources/com/zerocracy/bundles').absoluteFile
def bundles = bundlesDir.list().toList().stream().map({
  new File(bundlesDir, it).toPath()
}).collect(Collectors.toList())
bundles.each { Path bundle ->
  def items = items(bundle)
  items.each { bootstrap(it as Path) }
}

""

def bootstrap(Path res) {
  def name = res.fileName.toString()
  name = name.replaceAll("pmo_", "").replaceAll(".xml", "")
  def type = itemType(name)
  if (type == null) {
    return
  }
  println("bootstrapping item=${res} as '$type/$name'")
  new Xocument(res).bootstrap("$type/$name")
}

static def items(Path path) {
  Files.find(path, 4, { target, attr ->
    def file = target.fileName
    file.toString().endsWith(".xml") && !file.toString().startsWith("_")
  }).collect(Collectors.toList())
}
