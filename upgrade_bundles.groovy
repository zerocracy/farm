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
