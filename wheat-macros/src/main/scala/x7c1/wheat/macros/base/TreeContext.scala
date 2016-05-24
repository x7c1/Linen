package x7c1.wheat.macros.base

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

trait TreeContext {
  val context: blackbox.Context
  import context.universe._

  def isBuiltInType(x: Type): Boolean = {
    val prefixes = Seq(
      "scala.",
      "java."
    )
    val name = x.typeSymbol.fullName
    prefixes exists name.startsWith
  }
  def isBuiltInSymbol(x: Symbol): Boolean = {
    val prefixes = Seq(
      "scala.",
      "java."
    )
    val name = x.fullName
    prefixes exists name.startsWith
  }
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
  lazy val enclosingMethod: MethodSymbol = {
    @tailrec
    def loop(x: Symbol): Symbol = {
      if (x.isMethod || x.isPackage) x
      else loop(x.owner)
    }
    val ancestor = loop(context.internal.enclosingOwner)
    if (ancestor.isMethod){
      ancestor.asMethod
    } else {
      throw new IllegalStateException("enclosing method not found")
    }
  }
}

case class Enclosing(
  fullName: String,
  line: Int
)
