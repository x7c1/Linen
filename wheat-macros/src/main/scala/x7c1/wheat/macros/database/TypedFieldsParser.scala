package x7c1.wheat.macros.database

import scala.reflect.macros.blackbox

trait TypedFieldsParser {
  val context: blackbox.Context
  import context.universe._

  def extractArgs: Tree => Option[(Tree, Tree)] = {
    case q"$x($arg)" => (x, arg) match {
      case (x: Tree, right: Tree) => extractLeft(x) map (_ -> right)
      case _ => None
    }
    case t => t.children.collectFirst{ case x => extractArgs(x) }.flatten
  }
  def extractLeft: Tree => Option[Tree] = {
    case q"$x($arg)" => (x, arg) match {
      case (_, left: Tree) => Some(left)
      case _ => None
    }
    case t =>  t.children.collectFirst { case x => extractLeft(x) }.flatten
  }
  def getColumn: Tree => String = {
    case Select(_, name) => name.toString
    case t => throw new IllegalArgumentException(s"invalid form of tree: $t")
  }
  def getJavaType: Type => Option[Type] = {
    case x if x =:= typeOf[Int] => Some(typeOf[java.lang.Integer])
    case x if x =:= typeOf[Long] => Some(typeOf[java.lang.Long])
    case x if x =:= typeOf[Option[Long]] => Some(typeOf[java.lang.Long])
    case x if x =:= typeOf[Double] => Some(typeOf[java.lang.Double])
    case x if x =:= typeOf[String] => None
    case x => throw new IllegalArgumentException(s"unsupported type: $x")
  }
}

object TypedFieldsParser {
  def toSelectionArgs[A: c.WeakTypeTag](c: blackbox.Context)(pairs: c.Tree*): c.Tree = {
    import c.universe._
    val parser = new TypedFieldsParser {
      override val context: c.type = c
    }
    def forPrimitive(left: Tree, right: Tree) = {
      q"(${parser.getColumn(left)}, $right.toString)"
    }
    def forConvertible(left: Tree, right: Tree) = left.tpe.typeArgs match {
      case Seq(from, to) if to =:= right.tpe =>
        val convertible = appliedType(
          typeOf[FieldConvertible[_, _]].typeConstructor,
          from,
          to
        )
        val value = q"implicitly[$convertible].unwrap($right).toString"
        q"(${parser.getColumn(left)}, $value)"
    }
    def toStringPairs: ((Tree, Tree)) => Tree = {
      case (left, right) if left.tpe.widen =:= right.tpe.widen =>
        forPrimitive(left, right)
      case (left, right) if left.tpe <:< typeOf[FieldTransform[_, _]] =>
        forConvertible(left, right)
      case (left, right) =>
        throw new IllegalArgumentException(
          s"type inconsistent: ${parser.getColumn(left)}:[${left.tpe}] != $right:[${right.tpe}]")
    }
    val stringPairs = pairs map parser.extractArgs map {
      case Some(pair) => toStringPairs(pair)
      case None => throw new IllegalArgumentException("invalid form of expression")
    }
    val tree = q"Seq(..$stringPairs)"
//    println(tree)
    tree
  }
}
