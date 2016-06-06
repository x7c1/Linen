package x7c1.wheat.macros.reify

import x7c1.wheat.macros.base.{MethodComparator, PublicFieldsFinder, TreeContext}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox


trait HasConstructor[A]{
  def newInstance: A
}
object HasConstructor {
  implicit def reify[A]: HasConstructor[A] = macro ReificationImpl.infer[A]

  def apply[A](x: A): HasConstructor[A] = new HasConstructor[A] {
    override val newInstance: A = x
  }
}

object ReificationImpl {
  def infer[A: c.WeakTypeTag](c: blackbox.Context) = {
    val target = c.weakTypeOf[A]

    val factory = new ReificationFactory {
      override val context: c.type = c
    }
    factory.inspect(target)

    ???
  }
}

trait ReificationFactory extends TreeContext with PublicFieldsFinder with MethodComparator {
  import context.universe._

  def inspect(tpe: Type): Tree = {
    val Some(target) = tpe.typeArgs.lastOption
    println(target, target.typeSymbol.isAbstract)

    val constructor = findConstructorsOf(target) find { constructor =>
      hasSameParameterTypes(constructor, target)
    } getOrElse {
      throw new IllegalArgumentException(s"constructor not found in $target")
    }

    ???
  }
}
