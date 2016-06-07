package x7c1.wheat.macros.reify

import scala.language.experimental.macros
import scala.reflect.macros.blackbox


trait HasConstructor[A]{
  def newInstance: A
}
object HasConstructor {
  implicit def reify[A]: HasConstructor[A] = macro ReificationImpl.infer[A]

  type HasDefaultConstructor[A] = HasConstructor[() => A]
}

object ReificationImpl {
  def infer[A: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    val factory = new ReificationFactory {
      override val context: c.type = c
    }
    val tree = factory createConstructor c.weakTypeOf[A]
//    println(tree)
    tree
  }
}

trait ReificationFactory {
  val context: blackbox.Context
  import context.universe._

  def createConstructor(target: Type): Tree = {
    if (!target.typeSymbol.fullName.startsWith("scala.Function")){
      val name = typeOf[HasConstructor[_]].typeSymbol.name.encodedName
      val message = s"FunctionN must be applied to $name. current: $target"
      throw new IllegalArgumentException(message)
    }
    val newInstance = target.typeArgs match {
      case xs :+ last =>
        val args = xs map { x =>
          val arg = TermName(context freshName "x")
          q"$arg: $x"
        }
        q"(..$args) => new $last(..$args)"
      case x =>
        throw new IllegalArgumentException(s"unknown type: $x")
    }
    val constructor = appliedType(
      typeOf[HasConstructor[_]].typeConstructor,
      target
    )
    q"""new $constructor {
      override def newInstance = $newInstance
    }"""

  }
}
