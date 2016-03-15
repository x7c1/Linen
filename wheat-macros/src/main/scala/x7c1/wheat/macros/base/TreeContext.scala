package x7c1.wheat.macros.base

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

trait TreeContext {
  val context: blackbox.Context
  import context.universe._

  def createTermNames(names: String*): Seq[TermName] = {
    names map (x => TermName(context freshName x))
  }
  def enclosing: Enclosing = {
    @tailrec
    def loop(x: Symbol): Symbol = {
      if (x.isMethod || x.isClass || x.isPackage) x
      else loop(x.owner)
    }
    val ancestor = loop(context.internal.enclosingOwner)
    Enclosing(
      fullName = ancestor.fullName,
      line = context.enclosingPosition.line
    )
  }
}

case class Enclosing(
  fullName: String,
  line: Int
)
