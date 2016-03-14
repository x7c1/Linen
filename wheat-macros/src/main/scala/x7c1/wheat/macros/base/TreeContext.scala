package x7c1.wheat.macros.base

import scala.reflect.macros.blackbox

trait TreeContext {
  val context: blackbox.Context
  import context.universe._

  def createTermNames(names: String*): Seq[TermName] = {
    names map (x => TermName(context freshName x))
  }
}
