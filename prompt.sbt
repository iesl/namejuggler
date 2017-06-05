
def colorPrompt = { s: State =>
  val c = scala.Console
  val blue = c.RESET + c.CYAN + c.BOLD
  val white = c.RESET + c.BOLD
  val projectName = Project.extract(s).currentProject.id

  "[" + blue + projectName + white + "]>> " + c.RESET
}

shellPrompt in ThisBuild := colorPrompt
